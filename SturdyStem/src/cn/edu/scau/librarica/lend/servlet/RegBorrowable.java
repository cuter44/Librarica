package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;

/** �Ǽǳ���
 * �Ǽ�����Ĳ���Ϊ�ɳ���
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /lend/reg

   <strong>����</strong>
   id:long, ����, ׼���ϼܵ���id
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key
   <i>���ܶ���Ĳ���, ��μ� /borrowable/update </i>

   <strong>��Ӧ</strong>
   �� /borrowable/update ����

   <strong>����</strong>
   ָ���� id �����ڷ��� Forbidden(403):{"flag":"!notfound"}
   ָ���� id �Ѿ��ǿɽ���״̬ʱ���� Forbidden(403):{"flag":"!duplicated"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class RegBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ID = "id";

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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try
        {
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            BorrowableBook bb = BorrowableBookMgr.create(id);

            HiberDao.commit();

            req.getRequestDispatcher("/lend/update").forward(req, resp);
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!notfound\"}");
        }
        catch (EntityDuplicatedException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!duplicated\"}");
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
