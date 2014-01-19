package cn.edu.scau.librarica.msg.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.msg.dao.*;
import cn.edu.scau.librarica.msg.core.*;

/** ������Ϣ
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /msg/send

   <strong>����</strong>
   uid:long, ��ʾ�ռ���id
   wait:time-in-second, ��ʾ��û����Ϣ����ʱҪ�����ʱ��, �ܷ��������ô�������.
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json:
   ѹ���� json ����, �ṹ��������:
   Map&lt;fromId, Map&lt;timestamp, content&gt;&gt;
   ����,
   fromId:long, ��ʾ������id.
   timestamp:unix-time-in-second, ����ʱ���
   content:string, ��Ϣ����
   ��Ϊ fastjson ������, timestamp:content �Իᱻ���ֵ�����������, ���ڴ󲿷�����²��ṹ������, ���Բ��ᱻ����.

   <strong>����</strong>
   û�п�����Ϣʱ���� OK(200):{}
   uid ����ȷʱ���� Forbidden(403):{"flag":"!notfound"}

   <strong>����</strong>
    GET /librarica/msg/receive?uid=4&s=f00a551d&wait=100

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Content-Type: application/json;charset=utf-8
    Content-Length: 84
    Date: Sun, 19 Jan 2014 15:12:14 GMT

    {
      "1":{
        "1390144276":"blabla3",
        "1390144286":"blabla4"
      },
      "2":{
        "1390144320":"blabla4"
      }
    }


 * </pre>
 *
 */
public class ReceiveMsg extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String T = "uid";
    private static final String WAIT = "wait";

    public static JSONObject jsonize(List<Msg> l)
    {
        JSONObject packed = new JSONObject();

        Iterator<Msg> itr = l.iterator();
        while (itr.hasNext())
        {
            Msg m = itr.next();
            String uid = m.getF().getId().toString();

            JSONObject grouped = packed.getJSONObject(uid);
            if (grouped == null)
            {
                grouped = new JSONObject();
                packed.put(uid, grouped);
            }

            grouped.put(
                String.format("%ts", m.getM()),
                m.getC()
            );
        }

        return(packed);
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
            Long t = HttpUtil.getLongParam(req, T);
            if (t == null)
                throw(new MissingParameterException(T));

            Integer wait = HttpUtil.getIntParam(req, WAIT);
            if (wait == null)
                wait = 0;

            HiberDao.begin();

            List<Msg> ml = MsgRouter.receive(t, wait);

            HiberDao.commit();

            json = jsonize(ml);

            out.println(json.toJSONString());
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

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
