package cn.edu.scau.librarica.buddy.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.buddy.core.*;

/** �ӹ�ע
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /buddy/like

   <strong>����</strong>
   op:long, ����, �Է��� uid
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   �ɹ�ʱ���� OK(200), û����Ӧ����.

   <strong>����</strong>
   op ����ȷ���� Forbidden(403):{"flag":"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class Like extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ME = "uid";
    private static final String OP = "op";

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

            HiberDao.begin();

            BuddyMgr.setLike(me, op);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!notfound\"}");
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
