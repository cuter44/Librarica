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

/** 变更出借登记
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /lend/update

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
   pos:geohash-base32, 地理标记
   ps:string, 附言

   <strong>例外</strong>
   指定的 id 不存在返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class UpdateBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String ID = "id";
    private static final String POS = "pos";
    private static final String PS = "ps";

    private static JSONObject jsonize(BorrowableBook bb)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bb.getId());
        json.put(POS, bb.getPos());
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            BorrowableBook bb = BorrowableBookMgr.get(id);
            if (bb == null)
                throw(new EntityNotFoundException("No such BorrowableBook:"+id));

            String pos = HttpUtil.getParam(req, POS);
            if (pos != null)
                bb.setPos(pos);

            String ps = HttpUtil.getParam(req, PS);
            if (ps != null)
                bb.setPs(ps);

            HiberDao.update(bb);

            HiberDao.commit();

            JSONObject json = jsonize(bb);

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
