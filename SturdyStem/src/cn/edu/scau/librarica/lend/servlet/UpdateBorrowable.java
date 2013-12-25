package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;

/** 登记出借
 * 登记自身的藏书为可出借
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /lend/update

   <strong>参数</strong>
   bid:long, 必需, 准备上架的书id
   <1>可变更项目</i>
   geohash:base32(24), 地理标记
   ps:string, 附言
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json Object:
   bid:long, 等于 bid;
   geohash:base32, 地理标记
   ps:string, 附言

   <strong>例外</strong>
   指定的 bid 不存在返回 Bad Request(400):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class UpdateBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String BID = "bid";
    private static final String GEOHASH = "geohash";
    private static final String PS = "ps";

    private static JSONObject jsonize(BorrowableBook bb)
    {
        JSONObject json = new JSONObject();

        json.put(BID, bb.getId());
        json.put(GEOHASH, bb.getGeohash());
        json.put(PS, bb.getPs());

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

        JSONObject json = new JSONObject();

        try
        {
            Long bid = HttpUtil.getLongParam(req, BID);
            if (bid == null)
                throw(new MissingParameterException(BID));

            HiberDao.begin();

            BorrowableBook bb = BorrowableBookMgr.get(bid);

            String geohash = HttpUtil.getParam(req, GEOHASH);
            if (geohash != null)
                bb.setGeohash(geohash);

            String ps = HttpUtil.getParam(req, PS);
            if (ps != null)
                bb.setPs(ps);

            HiberDao.update(bb);

            HiberDao.commit();

            json = jsonize(bb);

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
