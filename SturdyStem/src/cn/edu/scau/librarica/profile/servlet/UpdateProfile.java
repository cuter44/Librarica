package cn.edu.scau.librarica.profile.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.profile.dao.*;
import cn.edu.scau.librarica.profile.core.*;

/** 变更个人资料
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /profile/update

   <strong>参数</strong>
   uid:long, 必需, 变更的资料id
   <1>可变更项目</i>
   dname:string(48), 显示名
   motto:string(255), 签名
   pos:base32(24), 地理标记
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json Object:
   uid:long, uid;
   dname: string(48), 显示名
   motto: string(48), 个性签名
   pos:geohash-base32, 地理标记

   <strong>例外</strong>
   指定的 id 不存在返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class UpdateProfile extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String DNAME = "dname";
    private static final String MOTTO = "motto";
    private static final String POS = "pos";

    private static JSONObject jsonize(Profile p)
    {
        JSONObject json = new JSONObject();

        json.put(UID, p.getId());
        json.put(DNAME, p.getDname());
        json.put(MOTTO, p.getMotto());
        json.put(POS, p.getPos());

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

            HiberDao.begin();

            Profile p = ProfileMgr.get(uid);
            if (p == null)
                throw(new EntityNotFoundException("No such Profile:"+uid));

            String dname = HttpUtil.getParam(req, DNAME);
            if (dname != null)
                p.setDname(dname);

            String motto = HttpUtil.getParam(req, MOTTO);
            if (motto != null)
                p.setMotto(motto);

            String pos = HttpUtil.getParam(req, POS);
            if (pos != null)
                p.setPos(pos);

            HiberDao.update(p);

            HiberDao.commit();

            JSONObject json = jsonize(p);

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
