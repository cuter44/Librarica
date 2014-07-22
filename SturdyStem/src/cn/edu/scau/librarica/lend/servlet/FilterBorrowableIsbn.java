package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;
import org.hibernate.criterion.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.lend.core.*;

/** 过滤可借书籍列表
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /lend/filter-isbn

   <strong>参数</strong>
   isbn:string, 指定书的isbn, 多个值以,分隔;

   <strong>响应</strong>
   application/json Array of:
   :string, 传入参数中可以被借取的isbn

   <strong>例外</strong>

   <strong>样例</strong>
    GET /librarica/sale/filter-isbn?isbn=97879787001,97879787002,97878787808,97872043823 HTTP/1.1

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Content-Type: application/json;charset=utf-8
    Content-Length: 31
    Date: Sat, 08 Mar 2014 16:57:28 GMT

    ["97879787001","97879787002"]

 * </pre>
 *
 */
public class FilterBorrowableIsbn extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ISBN = "isbn";

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
            List<String> isbns = HttpUtil.getStringListParam(req, ISBN);
            if (isbns == null)
                throw(new MissingParameterException(ISBN));

            HiberDao.begin();

            isbns = BorrowableBookMgr.filterIsbns(isbns);

            HiberDao.commit();

            JSONArray json = new JSONArray();
            json.addAll(isbns);

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
