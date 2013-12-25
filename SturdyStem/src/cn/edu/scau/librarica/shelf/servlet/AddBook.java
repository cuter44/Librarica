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
   bid:long, id
   <del>isbn:string, isbn</del>
   <del>uid:long, 书籍持有人的id</del>

   <strong>例外</strong>
   ownerId不正确时返回 Bad Request(400):{"flag":"!notfound"}

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
