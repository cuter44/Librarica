package cn.edu.scau.librarica.sale.servlet;

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

import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.sale.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** 列出单个交易项的信息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /buy/get

   <strong>参数</strong>
   id:long, 必须, 指定在借项的id

   <strong>响应</strong>
   application/json Object:
   id:long,
   status:byte,
   book:long,
   buyer:long,
   tmStatus:long-timestamp
   qty:int, quantity of the deal

   <strong>例外</strong>
   指定id不存在时返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class GetBuySession extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String BOOK = "book";
    private static final String BUYER = "buyer";
    private static final String TMSTATUS = "tmStatus";
    private static final String QTY = "qty";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    private static JSONObject jsonize(BuySession bs)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bs.getId());
        json.put(STATUS, bs.getStatus());
        json.put(BOOK, bs.getBook().getId());
        json.put(BUYER, bs.getBuyer().getId());
        json.put(TMSTATUS, bs.getTmStatus().getTime());
        json.put(QTY, bs.getQty());

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

            BuySession bs = BuySessionMgr.get(id);
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
