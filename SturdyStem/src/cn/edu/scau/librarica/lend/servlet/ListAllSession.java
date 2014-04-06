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
import org.hibernate.Query;

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** 列出正在借出和正在借入的藏书
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /borrow/list

   <strong>参数</strong>
   uid:long, 必须, 指定关系者的uid
   statusl:byte, 可选, 指定级别下限(左闭合), 缺省为0(REQUESTED)
   statush:byte, 可选, 指定级别上限(右闭合), 缺省为4(RETURNING)
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json Array of:
   id:long,
   status:byte,
   book:long,
   borrower:long,
   tmBorrow:long-timestamp
   tmReturn:long-timestamp
   <i>按日期降序排列</i>

   <strong>例外</strong>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class ListAllSession extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String STATUSL = "statusl";
    private static final String STATUSH = "statush";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String BOOK = "book";
    private static final String BORROWER = "borrower";
    private static final String TMBORROW = "tmBorrow";
    private static final String TMRETURN = "tmReturn";
    private static final String START = "start";
    private static final String SIZE = "size";

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
            Long uid = (Long)HttpUtil.notNull(HttpUtil.getLongParam(req, UID));

            Byte statusl = HttpUtil.getByteParam(req, STATUSL);
            statusl = (statusl!=null ? statusl : BorrowSession.REQUESTED);
            Byte statush = HttpUtil.getByteParam(req, STATUSH);
            statush = (statush!=null ? statush : BorrowSession.RETURNING);

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = (size!=null ? size : defaultPageSize);

            HiberDao.begin();

            Query hql = HiberDao.createQuery(
                    "FROM BorrowSession bs "
                    +"WHERE ((borrower.id=:uid) OR (book.owner.id=:uid)) "
                    +"AND (status>=:statusl) AND (status<=:statush)"
                    +"ORDER BY tmBorrow DESC"
                )
                .setLong("uid", uid)
                .setByte("statusl", statusl)
                .setByte("statush", statush);
            if (start!=null)
                hql.setFirstResult(start);
            hql.setMaxResults(size);

            List<BorrowSession> l = hql.list();

            HiberDao.commit();

            JSONArray json = new JSONArray();

            for (BorrowSession bs : l)
                json.add(jsonize(bs));

            out.println(json.toJSONString());
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
