package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.lend.core.*;

/** ȷ�Ͻ���
 * �ɽ����ߵ��øýӿ��Գ��Ͻ����鼮
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /borrow/conf-borrow

   <strong>����</strong>
   id:long, ����, ���ĻỰ��id
   <i>��Ȩ</i>
   uid:long, ����, uid
   pass:hex, ����, RSA���ܺ���û�����

   <strong>��Ӧ</strong>
   �ɹ�ʱ���� OK(200), û����Ӧ����.

   <strong>����</strong>
   ָ���� id ������ʱ���� Bad Request(400):{"flag":"!notfound"}
   ָ���� id �Ѳ��ɽ���ʱ���� Bad Request(400):{"flag":"!status"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class ConfirmBorrow extends HttpServlet
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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        JSONObject json = new JSONObject();

        try
        {
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            BorrowProcessor.confirmBorrow(id);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!notfound");
            out.println(json.toJSONString());
        }
        catch (IllegalStateException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!status");
            out.println(json.toJSONString());
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!parameter");
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
