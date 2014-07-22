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
import org.hibernate.criterion.*;

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** 搜索/列出可借藏书
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /lend/search

   <strong>参数</strong>
   <i>以下零至多个参数组, 按参数名分组, 组内以,分隔以or逻辑连接, 组间以and逻辑连接, 完全匹配</i>
   bid:long, 指定书的id;
   uid:long, 指定书拥有者id;
   isbn:string, 指定isbn;
   <i>地理位置限制</i>
   pos:geohash-base32, 表示地理范围, 以此作前端一致匹配.
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json Array of:
   bid:long, bid
   isbn:string, isbn
   uid:long, 书籍持有人的id
   ps:string, 出借人的附言
   pos:string, 书的所在位置

   <strong>例外</strong>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SearchBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String ISBN = "isbn";
    private static final String BID = "bid";
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String PS = "ps";
    private static final String POS = "pos";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    /** 将参数翻译为 DetachedCriteria<BorrawableBook>
     * @param dc 附着的DetachedCriteria, 必需传入一个Criteria<BorrowableBook>
     * @param req 带参数请求, 处理其中的 bid, uid, isbn, pos 参数
     */
    public static DetachedCriteria parseCriteria(DetachedCriteria dc, HttpServletRequest req)
    {
        // criterions BorrowableBook
        List<Long> bids = HttpUtil.getLongListParam(req, BID);
        if (bids!=null && bids.size()>0)
            dc.add(Restrictions.in("id", bids));

        String pos = HttpUtil.getParam(req, POS);
        if (pos!=null)
            dc.add(Restrictions.like("pos", pos, MatchMode.START));

        // criterions Book
        DetachedCriteria dcBook = dc.createCriteria("book");
        List<String> isbns = HttpUtil.getStringListParam(req, ISBN);
        if (isbns!=null && isbns.size()>0)
            dcBook.add(Restrictions.in("isbn", isbns));

        // criteria Owner
        DetachedCriteria dcOwner = dcBook.createCriteria("owner");
        List<Long> uids = HttpUtil.getLongListParam(req, UID);
        if (uids!=null && uids.size()>0)
            dcOwner.add(Restrictions.in("id", uids));

        return(dc);
    }

    private static JSONObject jsonize(BorrowableBook bb)
    {
        JSONObject json = new JSONObject();

        json.put(BID, bb.getId());
        json.put(POS, bb.getPos());
        json.put(PS, bb.getPs());
        json.put(ISBN, bb.getBook().getIsbn());
        json.put(UID, bb.getBook().getOwner().getId());

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
            DetachedCriteria dc = parseCriteria(
                DetachedCriteria.forClass(BorrowableBook.class),
                req
            );

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<BorrowableBook> l = (List<BorrowableBook>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            JSONArray json = new JSONArray();

            Iterator<BorrowableBook> i = l.iterator();
            while (i.hasNext())
                json.add(jsonize(i.next()));

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
