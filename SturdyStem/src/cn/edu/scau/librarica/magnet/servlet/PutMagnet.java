package cn.edu.scau.librarica.magnet.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.geom.PointLong;
import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.magnet.core.*;

/** �������λ����Ϣ
 * ����λ����Ϣ��һ��ʱ���ڴ��(�뼶��), ��ֻ�ᱻ�����ָ���ĶԷ�.
 * �ڵ�һ����������ӿ�ʱ�Է����յ�֪ͨ(notify)
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /magnet/put

   <strong>����</strong>
   op:long, ����, �Է���uid
   pos:base32-geohash, ����, ������geohsah.
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   �ɹ��򷵻�200(OK), û����Ӧ����
   ��״̬���������������ȷ������Ϣ, ���ǽ���Ϣ���͵��Է��ͻ���

   <strong>����</strong>

   <strong>����</strong>����
 * </pre>
 *
 */
public class PutMagnet extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ME = "uid";
    private static final String OP = "op";
    private static final String POS = "pos";

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
            if (me ==  null)
                throw(new MissingParameterException(ME));

            Long op = HttpUtil.getLongParam(req, OP);
            if (op == null)
                throw(new MissingParameterException(OP));

            String pos = HttpUtil.getParam(req, POS);
            if (pos == null)
                throw(new MissingParameterException(POS));

            // Magnet ����ʹ�����ݿ�, ������������Ҫ���ݿ�
            // �������Ժ󽫱��Ƴ�
            HiberDao.begin();

            MagnetCache.put(new PointLong(me, op), pos);

            HiberDao.commit();
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
        finally {
            HiberDao.close();
        }

        return;
    }
}
