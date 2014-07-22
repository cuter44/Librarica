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

/** 增加藏书
 * 书被以实体形式添加, i.e. 不会根据isbn被判重
 * <br />
 * 所以出借/出售的书也被以实体的形式区分.
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /book/add

   <strong>参数</strong>
   isbn:string, 必需, isbn
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json 对象:
   id:long, 标记书籍对象的id
   isbn:string, isbn
   owner:long, 书籍持有人的id

   <strong>例外</strong>

   <strong>样例</strong>暂无
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
        resp.setContentType("application/json; charset=utf-8");
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
