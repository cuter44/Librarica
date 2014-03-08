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
   GET/POST /lend/filter-bid

   <strong>参数</strong>
   bid:long, 指定书的id, 多个值以,分隔;

   <strong>响应</strong>
   application/json Array of:
   :long, 传入参数中可以被借取的bid

   <strong>例外</strong>

   <strong>样例</strong>
    GET /librarica/lend/filter-bid?bid=1,2,3,4,5,6 HTTP/1.1

    HTTP/1.1 200 OK
    Server Apache-Coyote/1.1 is not blacklisted
    Server: Apache-Coyote/1.1
    Content-Type: application/json;charset=utf-8
    Content-Length: 11
    Date: Sat, 08 Mar 2014 11:21:15 GMT

    [1,2,3,4]

 * </pre>
 *
 */
public class FilterBorrowableId extends HttpServlet
{
    private static final String FLAG = "flag";
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            List<Long> bids = HttpUtil.getLongListParam(req, BID);
            if (bids == null)
                throw(new MissingParameterException(BID));

            HiberDao.begin();

            bids = BorrowableBookMgr.filterBids(bids);

            HiberDao.commit();

            JSONArray json = new JSONArray();
            json.addAll(bids);

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
