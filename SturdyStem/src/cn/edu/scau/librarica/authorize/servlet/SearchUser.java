package cn.edu.scau.librarica.authorize.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.alibaba.fastjson.*;
import org.hibernate.criterion.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;

/** 搜索用户, 主要用于用户 uid, mail, uname.
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/search

   <strong>参数</strong>
   <i>以下零至多个参数组, 按参数名分组, 组内以,分隔以or逻辑连接, 组间以and逻辑连接, 完全匹配</i>
   uid:long, uid
   mail:string(60), 邮件地址
   uname:string, 用户名字, 不包含显示名
   <i>分页</i>
   start:int, 开始笔数
   limit:int, 最多数据笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json 数组:
   uid:long, 用户id.
   mail:string, 用户邮件地址
   uname:string, 用户名字
   regDate:unix-time-in-millis, 注册日期
   status:byte, 帐户状态

   <strong>例外</strong>
   传入空的参数将返回意外的结果.

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SearchUser extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String UID = "uid";
    private static final String MAIL = "mail";
    private static final String UNAME = "uname";
    private static final String STATUS = "status";
    private static final String REGDATE = "regDate";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    public static DetachedCriteria parseCriteria(DetachedCriteria dc, HttpServletRequest req)
    {
        List<Long> ids = HttpUtil.getLongListParam(req, UID);
        if (ids!=null && ids.size()>0)
            dc.add(Restrictions.in("id", ids));

        List<String> mails = HttpUtil.getStringListParam(req, MAIL);
        if (mails!=null && mails.size()>0)
            dc.add(Restrictions.in("mail", mails));

        List<String> unames = HttpUtil.getStringListParam(req, UNAME);
        if (unames!=null && unames.size()>0)
            dc.add(Restrictions.in("uname", unames));

        return(dc);
    }

    private static JSONObject jsonize(User u)
    {
        JSONObject json = new JSONObject();

        json.put(UID, u.getId());
        json.put(MAIL, u.getMail());
        json.put(UNAME, u.getUname());
        json.put(STATUS, u.getStatus());
        json.put(REGDATE, u.getRegDate());

        return(json);
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            DetachedCriteria dc = parseCriteria(
                DetachedCriteria.forClass(User.class),
                req
            );

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<User> l = (List<User>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            JSONArray json = new JSONArray();
            Iterator<User> i = l.iterator();
            while (i.hasNext())
                json.add(jsonize(i.next()));

            out.println(json.toJSONString());

        }
        catch (Exception ex)
        {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            this.log("", ex);
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
