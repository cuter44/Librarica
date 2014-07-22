package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.criterion.*;
import com.github.cuter44.util.dao.*;
import static com.github.cuter44.util.servlet.HttpUtil.getLongListParam;
import static com.github.cuter44.util.servlet.HttpUtil.getIntParam;
import static com.github.cuter44.util.servlet.HttpUtil.getStringListParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;

/** 搜索用户, 主要用于用户 uid, mail, uname.
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/search.api

   <strong>参数</strong>
   <i>以下零至多个参数组, 按参数名分组, 组内以,分隔以or逻辑连接, 组间以and逻辑连接, 完全匹配</i>
   uid:long, uid
   mail:string(60), 邮件地址
   uname:string, 用户名字, 不包含显示名
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json array class=authorize.dao.User(public)
   @see J#writeUserListPublic

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SearchUser extends HttpServlet
{
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String UID = "uid";
    private static final String MAIL = "mail";
    private static final String UNAME = "uname";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    public static DetachedCriteria parseCriteria(DetachedCriteria dc, HttpServletRequest req)
    {
        List<Long> uids = getLongListParam(req, UID);
        if (uids!=null && uids.size()>0)
            dc.add(Restrictions.in("uid", uids));

        List<String> mails = getStringListParam(req, MAIL);
        if (mails!=null && mails.size()>0)
            dc.add(Restrictions.in("mail", mails));

        List<String> unames = getStringListParam(req, UNAME);
        if (unames!=null && unames.size()>0)
            dc.add(Restrictions.in("uname", unames));

        return(dc);
    }

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
            DetachedCriteria dc = parseCriteria(
                DetachedCriteria.forClass(User.class),
                req
            );

            Integer start   =   (Integer)getIntParam(req, START);
            Integer size    =   (Integer)getIntParam(req, SIZE);
            size            =   size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<User> l = (List<User>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            J.writeUserListPublic(l, resp);
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
