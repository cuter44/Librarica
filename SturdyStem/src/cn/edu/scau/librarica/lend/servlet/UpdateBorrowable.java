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

/** �������Ǽ�
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /lend/update

   <strong>����</strong>
   id:long, ����, ׼���ϼܵ���id
   <1>�ɱ����Ŀ</i>
   pos:base32(24), ������
   ps:string, ����
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json Object:
   id:long, ���� id;
   pos:geohash-base32, ������
   ps:string, ����

   <strong>����</strong>
   ָ���� id �����ڷ��� Forbidden(403):{"flag":"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class UpdateBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ID = "id";
    private static final String POS = "pos";
    private static final String PS = "ps";

    private static JSONObject jsonize(BorrowableBook bb)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bb.getId());
        json.put(POS, bb.getPos());
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            BorrowableBook bb = BorrowableBookMgr.get(id);
            if (bb == null)
                throw(new EntityNotFoundException("No such BorrowableBook:"+id));

            String pos = HttpUtil.getParam(req, POS);
            if (pos != null)
                bb.setPos(pos);

            String ps = HttpUtil.getParam(req, PS);
            if (ps != null)
                bb.setPs(ps);

            HiberDao.update(bb);

            HiberDao.commit();

            JSONObject json = jsonize(bb);

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
