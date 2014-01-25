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
 * �鱻��ʵ����ʽ���, i.e. �������isbn������
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
   id:long, ����鼮�����id
   isbn:string, isbn
   owner:long, �鼮�����˵�id

   <strong>����</strong>

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
    private static final String ID = "id";
    private static final String OWNER = "owner";

    private static JSONObject jsonize(Book b)
    {
        JSONObject json = new JSONObject();

        json.put(ID, b.getId());
        json.put(ISBN, b.getIsbn());
        json.put(OWNER, b.getOwner().getId());

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

            String isbn = HttpUtil.getParam(req, ISBN);
            if (isbn == null)
                throw(new MissingParameterException(ISBN));

            HiberDao.begin();

            Book b = BookMgr.create(isbn, uid);

            HiberDao.commit();

            JSONObject json = jsonize(b);
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
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
