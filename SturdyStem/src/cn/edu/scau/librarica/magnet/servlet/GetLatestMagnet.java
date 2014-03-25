package cn.edu.scau.librarica.magnet.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.geom.PointLong;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.magnet.core.*;

/** ȡ�ضԷ����µ�λ����Ϣ
 * "����"��ָû�б� magnet �������µ����� get ����������������.
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /magnet/get-latest

   <strong>����</strong>
   op:long, ����, �Է���uid
   wait:int-second, ��ѡ, �Ⱥ�����ʱ��, ȱʡΪ0, �ܷ�������������Լ
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json object:
   pos:base32-geohash, �����п�������ʱ�����Է����һ�η��͵�geohash, �����ܶԷ�����˽����Ӱ��.

   <strong>����</strong>
   û�п�������ʱ���� No Content(204), û����Ӧ���Ļ���Ҫ������Ӧ����
   �� wait Ϊ����ʱ���� Interal Serveral Error(500)

   <strong>����</strong>����
 * </pre>
 *
 */
public class GetLatestMagnet extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ME = "uid";
    private static final String OP = "op";
    private static final String WAIT = "wait";
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

            Integer waitSecond = HttpUtil.getIntParam(req, WAIT);
            if (waitSecond == null)
                waitSecond = 0;

            String pos = MagnetCache.getLatest(new PointLong(op, me), waitSecond);
            if (pos == null)
            {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            // else
            JSONObject json = new JSONObject();
            json.put(POS, pos);
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

        return;
    }
}
