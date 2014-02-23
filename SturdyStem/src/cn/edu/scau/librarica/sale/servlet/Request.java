package cn.edu.scau.librarica.sale.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.sale.core.*;

/** ���𶩵�
 * ֻ�б�ʾΪ SalableBook ������Ա�����
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /buy/request

   <strong>����</strong>
   id:long, ����, ���id
   uid:long, ����, �����ߵ�uid
   qty:int, ��������, ʡ��ʱָ��Ϊ 1, ������ʾ��;, ϵͳ�����ݴ˼�����
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json ����:
   id:long, ���ɽ��ĻỰ��id, �������δ����Ӧ(�ܾ������), ���᷵����һ�ε� id
   status:byte, ״̬, ���� BorrowSession.REQUESTED
   book:long, ���id
   borrower:long, �����ߵ�id
   qty:int, ��������

   <strong>����</strong>
   ָ���� uid/id ������/���ڳ���ʱ���� Forbidden(403):{"flag":"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class Request extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String BOOK = "book";
    private static final String BUYER = "buyer";
    private static final String QTY = "qty";

    private static JSONObject jsonize(BuySession bs)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bs.getId());
        json.put(STATUS, bs.getStatus());
        json.put(BOOK, bs.getBook().getId());
        json.put(BUYER, bs.getBuyer().getId());

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

            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            Integer qty = HttpUtil.getIntParam(req, QTY);
            if (qty == null)
                qty = 1;

            HiberDao.begin();

            BuySession bs = DealProcessor.request(id, uid, qty);

            HiberDao.commit();

            JSONObject json = jsonize(bs);

            out.println(json.toJSONString());
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
