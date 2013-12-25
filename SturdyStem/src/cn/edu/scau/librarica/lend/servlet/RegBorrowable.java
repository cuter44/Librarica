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
   bid:long, ����, ׼���ϼܵ���id
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key
   <i>���ܶ���Ĳ���, ��μ� /borrowable/update </i>

   <strong>��Ӧ</strong>
   �� /borrowable/update ����

   <strong>����</strong>
   ָ���� bid �����ڷ��� Bad Request(400):{"flag":"!notfound"}
   ָ���� bid �Ѿ��ǿɽ���״̬ʱ���� Bad Reuqest(400):{"flag":"!duplicated"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class RegBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String BID = "bid";

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
            Long bid = HttpUtil.getLongParam(req, BID);
            if (bid == null)
                throw(new MissingParameterException(BID));

            HiberDao.begin();

            BorrowableBook bb = BorrowableBookMgr.create(bid);

            HiberDao.commit();

            req.getRequestDispatcher("/lend/update").forward(req, resp);
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!notfound");
            out.println(json.toJSONString());
        }
        catch (EntityDuplicatedException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!duplicated");
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
