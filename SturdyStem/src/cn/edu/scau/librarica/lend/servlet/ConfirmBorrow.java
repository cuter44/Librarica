package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.lend.core.*;

/** 确认借入
 * 由借阅者调用该接口以承认接受书籍
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /borrow/conf-borrow

   <strong>参数</strong>
   id:long, 必需, 借阅会话的id
   <i>鉴权</i>
   uid:long, 必需, uid
   pass:hex, 必需, RSA加密后的用户密码

   <strong>响应</strong>
   成功时返回 OK(200), 没有响应正文.

   <strong>例外</strong>
   指定的 id 不存在时返回 Bad Request(400):{"flag":"!notfound"}
   指定的 id 已不可接受时返回 Bad Request(400):{"flag":"!status"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class ConfirmBorrow extends HttpServlet
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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        JSONObject json = new JSONObject();

        try
        {
            Long id = HttpUtil.getLongParam(req, ID);
            if (id == null)
                throw(new MissingParameterException(ID));

            HiberDao.begin();

            BorrowProcessor.confirmBorrow(id);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!notfound");
            out.println(json.toJSONString());
        }
        catch (IllegalStateException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!status");
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
