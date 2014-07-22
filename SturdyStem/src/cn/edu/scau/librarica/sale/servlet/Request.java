package cn.edu.scau.librarica.sale.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.sale.core.*;

/** 发起订单
 * 只有表示为 SalableBook 的书可以被请求
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /buy/request

   <strong>参数</strong>
   id:long, 必需, 书的id
   uid:long, 必需, 借阅者的uid
   qty:int, 购买数量, 省略时指定为 1, 仅作显示用途, 系统不依据此计算库存
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json 对象:
   id:long, 生成借阅会话的id, 如果有尚未被响应(拒绝或接受), 将会返回上一次的 id
   status:byte, 状态, 等于 BorrowSession.REQUESTED
   book:long, 书的id
   borrower:long, 借阅者的id
   qty:int, 购买数量

   <strong>例外</strong>
   指定的 uid/id 不存在/不在出售时返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Request extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String BOOK = "book";
    private static final String BUYER = "buyer";
    private static final String QTY = "qty";

    private static JSONObject jsonize(BuySession bs)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bs.getId());
        json.put(STATUS, bs.getStatus());
        json.put(BOOK, bs.getBook().getId());
        json.put(BUYER, bs.getBuyer().getId());

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

            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            Integer qty = HttpUtil.getIntParam(req, QTY);
            if (qty == null)
                qty = 1;

            HiberDao.begin();

            BuySession bs = DealProcessor.request(id, uid, qty);

            HiberDao.commit();

            JSONObject json = jsonize(bs);

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
