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

/** 登记出借
 * 登记自身的藏书为可出借
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /sale/update

   <strong>参数</strong>
   id:long, 必需, 准备上架的书id
   <1>可变更项目</i>
   pos:base32(24), 地理标记
   ps:string, 附言
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json Object:
   id:long, 等于 id;
   pos:base32, 地理标记
   ps:string, 附言

   <strong>例外</strong>
   指定的 id 不存在返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class UpdateSalable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ID = "id";
    private static final String POS = "pos";
    private static final String PRICE = "price";
    private static final String PS = "ps";

    private static JSONObject jsonize(SalableBook sb)
    {
        JSONObject json = new JSONObject();

        json.put(ID, sb.getId());
        json.put(POS, sb.getPos());
        json.put(PS, sb.getPs());
        json.put(PRICE, sb.getPrice());

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
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            SalableBook sb = SalableBookMgr.get(id);
            if (sb == null)
                throw(new EntityNotFoundException("No such SalableBook:"+id));

            String pos = HttpUtil.getParam(req, POS);
            if (pos != null)
                sb.setPos(pos);

            String ps = HttpUtil.getParam(req, PS);
            if (ps != null)
                sb.setPs(ps);

            Float price = HttpUtil.getFloatParam(req, PRICE);
            if (price != null)
                sb.setPrice(price);

            HiberDao.update(sb);

            HiberDao.commit();

            JSONObject json = jsonize(sb);

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
