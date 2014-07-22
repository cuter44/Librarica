package cn.edu.scau.librarica.buddy.servlet;

import java.util.List;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getIntParam;
import org.hibernate.criterion.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.buddy.dao.*;
import cn.edu.scau.librarica.buddy.core.*;

/** 列出关注/被关注/黑名单/被黑名单
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /buddy/search.api

   <strong>参数</strong>
   me:long, a-side 的 uid
   op:long, b-side 的 uid
   r:int=(1|-1), 表示like或者hate
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json array class=buddy.dao.Buddy
   @see J#write

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler
    可以在无参状态下执行, 但没有什么意义.

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SearchBuddy extends HttpServlet
{
    private static final String ME = "me";
    private static final String OP = "op";
    private static final String R = "r";
    private static final String START = "start";
    private static final String SIZE = "size";

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
            // fetch params
            Long    me      = getLongParam(req, ME);
            Long    op      = getLongParam(req, OP);
            Integer r       = getIntParam(req, R);

            Integer start   = getIntParam(req, START);
            Integer size    = getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            // build criteria
            DetachedCriteria dc = DetachedCriteria.forClass(Buddy.class);

            if (me != null)
                dc.createCriteria("me")
                  .add(Restrictions.eq("id", me));
            if (op != null)
                dc.createCriteria("op")
                  .add(Restrictions.eq("id", op));
            if (r != null)
                dc.add(Restrictions.eq("r", r));

            HiberDao.begin();

            List<Buddy> l = (List<Buddy>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            J.write(l, resp);
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
