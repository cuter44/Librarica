package cn.edu.scau.librarica.buddy.servlet;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import org.hibernate.criterion.*;
import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.buddy.dao.*;
import cn.edu.scau.librarica.buddy.core.*;

/** 列出关注/被关注/黑名单/被黑名单
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /buddy/list

   <strong>参数</strong>
   uid:long, 必需, 欲查询的uid
   type:string=(like|liked|hate|hated), 必须, 大小写不敏感, 表示查询的谓词
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json, array of long
   每个元素表示一个uid

   <strong>例外</strong>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class ListBuddy extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String TYPE = "type";
    private static final String START = "start";
    private static final String SIZE = "size";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    private static JSONArray jsonize(List<User> l)
    {
        JSONArray json = new JSONArray();

        Iterator<User> i = l.iterator();
        while (i.hasNext())
            json.add(i.next().getId());

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
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            String type = HttpUtil.getParam(req, TYPE);
            if (type == null)
                throw(new MissingParameterException(TYPE));

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            DetachedCriteria dc = DetachedCriteria.forClass(Buddy.class);

            if ("like".equalsIgnoreCase(type))
            {
                dc.createCriteria("me")
                  .add(Restrictions.eq("id", uid));
                dc.add(Restrictions.eq("r", Buddy.LIKE))
                  .setProjection(Projections.property("op"));
            }
            if ("liked".equalsIgnoreCase(type))
            {
                dc.createCriteria("op")
                  .add(Restrictions.eq("id", uid));
                dc.add(Restrictions.eq("r", Buddy.LIKE))
                  .setProjection(Projections.property("me"));
            }
            if ("hate".equalsIgnoreCase(type))
            {
                dc.createCriteria("me")
                  .add(Restrictions.eq("id", uid));
                dc.add(Restrictions.eq("r", Buddy.HATE))
                  .setProjection(Projections.property("op"));
            }
            if ("hated".equalsIgnoreCase(type))
            {
                dc.createCriteria("op")
                  .add(Restrictions.eq("id", uid));
                dc.add(Restrictions.eq("r", Buddy.HATE))
                  .setProjection(Projections.property("me"));
            }

            HiberDao.begin();

            List<User> l = (List<User>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            JSONArray json = jsonize(l);
            out.println(json.toJSONString());
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"flag\":\"!parameter\"}");
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
