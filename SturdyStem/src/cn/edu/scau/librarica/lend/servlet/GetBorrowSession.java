package cn.edu.scau.librarica.lend.servlet;

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
import org.hibernate.criterion.*;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** 列出单个在借项的信息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /borrow/get

   <strong>参数</strong>
   id:long, 必须, 指定在借项的id

   <strong>响应</strong>
   application/json Object:
   id:long,
   status:byte,
   book:long,
   borrower:long,
   tmBorrow:long-timestamp
   tmReturn:long-timestamp

   <strong>例外</strong>
   指定id不存在时返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class GetBorrowSession extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String BOOK = "book";
    private static final String BORROWER = "borrower";
    private static final String TMBORROW = "tmBorrow";
    private static final String TMRETURN = "tmReturn";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    private static JSONObject jsonize(BorrowSession bs)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bs.getId());
        json.put(STATUS, bs.getStatus());
        json.put(BOOK, bs.getBook().getId());
        json.put(BORROWER, bs.getBorrower().getId());
        if (bs.getTmBorrow() != null)
            json.put(TMBORROW, bs.getTmBorrow().getTime());
        if (bs.getTmReturn() != null)
            json.put(TMRETURN, bs.getTmReturn().getTime());

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
            Long id = (Long)HttpUtil.notNull(HttpUtil.getLongParam(req, ID));

            HiberDao.begin();

            BorrowSession bs = BorrowSessionMgr.get(id);
            if (bs == null)
                throw(new EntityNotFoundException());

            HiberDao.commit();

            out.println(jsonize(bs).toJSONString());
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
