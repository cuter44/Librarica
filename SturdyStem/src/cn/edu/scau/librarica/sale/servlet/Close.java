package cn.edu.scau.librarica.sale.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.sale.core.*;

/** ��������
 * ��ҵ�������ӿ��Խ�������
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /buy/close

   <strong>����</strong>
   id:long, ����, ���ĻỰ��id
   <i>��Ȩ</i>
   uid:long, ����, uid
   pass:hex, ����, RSA���ܵ��û�����

   <strong>��Ӧ</strong>
   �ɹ�ʱ���� OK(200), û����Ӧ����.
   <i>SIDE EFFECT</i>
   ��������Ϊ�����û�, �Ὣ�鼮ת�Ƶ��������, �����Ϊ��Ҫ��һ��ϸ��.

   <strong>����</strong>
   ָ���� id ������ʱ���� Forbidden(403):{"flag":"!notfound"}
   ָ���� id �Ѳ���״̬ʱ���� Conflict(409):{"flag":"!status"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class Close extends HttpServlet
{
    private static final String FLAG = "flag";
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            DealProcessor.close(id);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!notfound\"}");
        }
        catch (IllegalStateException ex)
        {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);

            out.println("{\"flag\":\"!status\"}");
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
