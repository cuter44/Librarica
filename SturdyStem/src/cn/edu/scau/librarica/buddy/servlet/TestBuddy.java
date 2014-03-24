package cn.edu.scau.librarica.buddy.servlet;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.buddy.dao.*;
import cn.edu.scau.librarica.buddy.core.*;

/** 测试两个用户间的关系
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /buddy/test

   <strong>参数</strong>
   me:long, 必需, uid, 查询的"主语"
   op:long, 必需, uid, 查询的"宾语"

   <strong>响应</strong>
   application/json, array[2] of byte
   元素[0]表示主对象->宾对象的关系谓词.
   元素[1]表示宾对象->主对象的关系谓词.
   其中, 关系谓词是:
   -1(Buddy.HATE), 拉黑
   0(Buddy.NOPE), 没有特别关系
   1(Buddy.LIKE), 关注

   <strong>例外</strong>

   <strong>样例</strong>
   <code>
    curl "http://localhost:8080/librarica/buddy/test?me=1&op=2"

    [1,0]
   </code>

 * </pre>
 *
 */
public class TestBuddy extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ME = "me";
    private static final String OP ="op";

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
            Long me = HttpUtil.getLongParam(req, ME);
            if (me == null)
                throw(new MissingParameterException(ME));

            Long op = HttpUtil.getLongParam(req, OP);
            if (op == null)
                throw(new MissingParameterException(OP));

            JSONArray json = new JSONArray(2);
            Buddy b;

            HiberDao.begin();

            b = BuddyMgr.get(me, op);
            json.add(b!=null ? b.getR() : Buddy.NOPE);

            b = BuddyMgr.get(op, me);
            json.add(b!=null ? b.getR() : Buddy.NOPE);

            HiberDao.commit();

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
