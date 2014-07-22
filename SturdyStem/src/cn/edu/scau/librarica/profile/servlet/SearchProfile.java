package cn.edu.scau.librarica.profile.servlet;

import java.io.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getParam;
import static com.github.cuter44.util.servlet.HttpUtil.getIntParam;
import static com.github.cuter44.util.servlet.HttpUtil.getLongListParam;
import org.hibernate.criterion.*;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.profile.dao.*;
import cn.edu.scau.librarica.profile.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** 搜索用户资料
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /profile/search.api

   <strong>参数</strong>
   <i>以下的其中一种</i>
   <i>精确匹配</i>
   id:long, 指定uid, 多值时以逗号分隔;
   <i>任意匹配</i>
   q:string, 在 mail, uname, dname, tname 上运行包含匹配(%key%)
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json array class=profile.dao.Profile
   @see J#write

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>
 * </pre>
 *
 */
public class SearchProfile extends HttpServlet
{
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String ID = "id";
    private static final String Q = "q";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        req.setCharacterEncoding("utf-8");

        try
        {
            Integer start   = getIntParam(req, START);
            Integer size    = getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<Long> uids = getLongListParam(req, ID);
            if (uids != null)
            {
                DetachedCriteria dc = DetachedCriteria.forClass(Profile.class)
                    .add(Restrictions.in("id", uids));

                List<Profile> l = (List<Profile>)HiberDao.search(dc);

                J.write(l, resp);

                return;
            }

            String q = getParam(req, Q);
            if (q != null && q.length() > 0)
            {
                q = "%"+q+"%";
                List<Profile> l = (List<Profile>)HiberDao.createQuery(
                    "SELECT p FROM Profile p INNER JOIN p.user u "+
                        "WHERE p.dname LIKE :q OR p.tname LIKE :q "+
                        "OR    u.uname LIKE :q OR u.mail LIKE :q")
                    .setString("q", q)
                    .list();

                J.write(l, resp);

                return;
            }

            throw(new MissingParameterException());
        }
        catch (Exception ex)
        {
            req.setAttribute(Constants.KEY_EXCEPTION, ex);
            req.getRequestDispatcher(Constants.URI_ERROR_HANDLER).forward(req, resp);
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
