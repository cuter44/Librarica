package cn.edu.scau.librarica.shelf.servlet;

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

/** ���Ӳ���
 * �鱻��ʵ����ʽ����, i.e. �������isbn������
 * <br />
 * ���Գ���/���۵���Ҳ����ʵ�����ʽ����.
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /book/add

   <strong>����</strong>
   isbn:string, ����, isbn
   <i>��Ȩ</i>
   uid:long, ����, uid
   s:hex, ����, session key

   <strong>��Ӧ</strong>
   application/json ����:
   bid:long, id
   <del>isbn:string, isbn</del>
   <del>uid:long, �鼮�����˵�id</del>

   <strong>����</strong>
   ownerId����ȷʱ���� Bad Request(400):{"flag":"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class AddBook extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ISBN = "isbn";
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
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            String isbn = HttpUtil.getParam(req, ISBN);
            if (isbn == null)
                throw(new MissingParameterException(ISBN));

            HiberDao.begin();

            Book b = BookMgr.create(isbn, uid);

            HiberDao.commit();

            json.put(BID, b.getId());
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