package cn.edu.scau.librarica.sale.servlet;

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
import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.sale.core.*;

/** 登记出售
 * 登记自身的藏书为可出售
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /sale/reg

   <strong>参数</strong>
   id:long, 必需, 准备上架的书id
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key
   <i>接受额外的参数, 请参见 /sale/update </i>

   <strong>响应</strong>
   由 /sale/update 生成

   <strong>例外</strong>
   指定的 id 不存在返回 Forbidden(403):{"flag":"!notfound"}
   指定的 id 已经是可借阅状态时返回 Forbidden(403):{"flag":"!duplicated"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class RegSalable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
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

            SalableBook sb = SalableBookMgr.create(id);

            HiberDao.commit();

            req.getRequestDispatcher("/sale/update").forward(req, resp);
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!notfound\"}");
        }
        catch (EntityDuplicatedException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!duplicated\"}");
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
