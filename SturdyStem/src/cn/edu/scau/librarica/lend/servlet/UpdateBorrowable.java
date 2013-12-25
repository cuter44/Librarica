package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;

/** �Ǽǳ���
 * �Ǽ������Ĳ���Ϊ�ɳ���
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /lend/update

   <strong>����</strong>
   bid:long, ����, ׼���ϼܵ���id
   <1>�ɱ����Ŀ</i>
   geohash:base32(24), �������
   ps:string, ����
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json Object:
   bid:long, ���� bid;
   geohash:base32, �������
   ps:string, ����

   <strong>����</strong>
   ָ���� bid �����ڷ��� Bad Request(400):{"flag":"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class UpdateBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String BID = "bid";
    private static final String GEOHASH = "geohash";
    private static final String PS = "ps";

    private static JSONObject jsonize(BorrowableBook bb)
    {
        JSONObject json = new JSONObject();

        json.put(BID, bb.getId());
        json.put(GEOHASH, bb.getGeohash());
        json.put(PS, bb.getPs());

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

            BorrowableBook bb = BorrowableBookMgr.get(bid);

            String geohash = HttpUtil.getParam(req, GEOHASH);
            if (geohash != null)
                bb.setGeohash(geohash);

            String ps = HttpUtil.getParam(req, PS);
            if (ps != null)
                bb.setPs(ps);

            HiberDao.update(bb);

            HiberDao.commit();

            json = jsonize(bb);

            out.println(json.toJSONString());
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!notfound");
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