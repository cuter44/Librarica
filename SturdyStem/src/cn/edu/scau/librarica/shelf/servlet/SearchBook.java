package cn.edu.scau.librarica.shelf.servlet;

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

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

import org.hibernate.criterion.*;

/** 搜索/列出藏书
 * 可借/出售书籍搜索现在通过另外的接口提供
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /book/search

   <strong>参数</strong>
   <i>以下零至多个参数组, 按参数名分组, 组内以,分隔以or逻辑连接, 组间以and逻辑连接, 完全匹配</i>
   bid:long, 指定书的id;
   uid:long, 指定书拥有者id;
   isbn:string, 指定isbn;
   <i>分页</i>
   start:int, 返回结果的起始笔数, 缺省从 1 开始
   size:int, 返回结果的最大笔数, 缺省使用服务器配置

   <strong>响应</strong>
   application/json Array:
   bid:long, bid
   isbn:string, isbn
   uid:long, 书籍持有人的id

   <strong>例外</strong>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SearchBook extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String ISBN = "isbn";
    private static final String BID = "bid";
    private static final String START = "start";
    private static final String SIZE = "size";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    /** 将参数翻译为 Criteria<Book>
     * @param dc 附着的Criteria, 必需传入一个Criteria<Book>
     * @param req 带参数请求, 处理其中的 bid, uid, isbn 参数
     */
    public static DetachedCriteria parseCriteria(DetachedCriteria dc, HttpServletRequest req)
    {
        List<Long> bids = HttpUtil.getLongListParam(req, BID);
        if (bids!=null && bids.size()>0)
            dc.add(Restrictions.in("id", bids));

        List<String> isbns = HttpUtil.getStringListParam(req, ISBN);
        if (isbns!=null && isbns.size()>0)
            dc.add(Restrictions.in("isbn", isbns));

        DetachedCriteria dcOwner = dc.createCriteria("owner");
        List<Long> uids = HttpUtil.getLongListParam(req, UID);
        if (uids!=null && uids.size()>0)
            dcOwner.add(Restrictions.in("id", uids));

        return(dc);
    }

    private static JSONObject jsonize(Book b)
    {
        JSONObject json = new JSONObject();

        json.put(BID, b.getId());
        json.put(ISBN, b.getIsbn());
        json.put(UID, b.getOwner().getId());

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
                DetachedCriteria.forClass(Book.class),
                req
            );

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<Book> l = (List<Book>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            JSONArray json = new JSONArray();

            Iterator<Book> i = l.iterator();
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
