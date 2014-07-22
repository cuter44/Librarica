package cn.edu.scau.librarica.sale.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.sale.core.*;

/** 取消购买
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /buy/abort

   <strong>参数</strong>
   id:long, 必需, 借阅会话的id
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   成功时返回 OK(200), 没有响应正文.

   <strong>例外</strong>
   指定的 id 不存在时返回 Forbidden(403):{"flag":"!notfound"}
   指定的 id 已不可取消时返回 Conflict(409):{"flag":"!status"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Abort extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ID = "id";

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

            DealProcessor.abort(id);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!notfound\"}");
        }
        catch (IllegalStateException ex)
        {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);

            out.println("{\"flag\":\"!status\"}");
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
