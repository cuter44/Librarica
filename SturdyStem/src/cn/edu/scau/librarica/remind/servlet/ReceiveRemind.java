package cn.edu.scau.librarica.remind.servlet;

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

import cn.edu.scau.librarica.remind.dao.*;
import cn.edu.scau.librarica.remind.core.*;

/** ������Ϣ
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /remind/receive

   <strong>����</strong>
   uid:long, ��ʾ�ռ���id
   wait:time-in-second, ��ʾ��û����Ϣ����ʱҪ�����ʱ��, �ܷ��������ô�������.
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json:
   List&lt;type, id&gt;
   ����,
   type:string, �¼�����, ͨ���� POJO ������, ���� BorrowSession, Msg
   value:string-of-decimal, id, ͨ������ POJO �� id, ���� Msg ��Ϊ�ռ��� id

   <strong>����</strong>
   û�п���֪ͨʱ���� OK(200):{}
   uid ����ȷʱ���� Forbidden(403):{"flag":"!notfound"}

   <strong>����</strong>
   �ʤ�
 * </pre>
 *
 */
public class ReceiveRemind extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String WAIT = "wait";

    public static JSONObject jsonize(Remind r)
    {
        JSONObject json = new JSONObject();

        json.put(r.getT(), r.getV());

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

        try
        {
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            Integer wait = HttpUtil.getIntParam(req, WAIT);
            if (wait == null)
                wait = 0;

            HiberDao.begin();

            List<Remind> rl = RemindRouter.receive(uid, wait);

            HiberDao.commit();

            JSONArray json = new JSONArray();
            Iterator<Remind> itr = rl.iterator();
            while (itr.hasNext())
                json.add(jsonize(itr.next()));

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
