package cn.edu.scau.librarica.sale.servlet;

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
import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.sale.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** ����/�г��������
 * <pre style="font-size:12px">

   <strong>����</strong>
   GET/POST /sale/search

   <strong>����</strong>
   <i>�����������������, ������������, ������,�ָ���or�߼�����, �����and�߼�����, ��ȫƥ��</i>
   bid:long, ָ�����id;
   uid:long, ָ����ӵ����id;
   isbn:string, ָ��isbn;
   <i>����λ������</i>
   pos:geohash-base32, ��ʾ����Χ, �Դ���ǰ��һ��ƥ��.
   <i>��ҳ</i>
   start:int, ���ؽ������ʼ����
   size:int, ���ؽ����������

   <strong>��Ӧ</strong>
   application/json Array of:
   bid:long, bid
   isbn:string, isbn
   uid:long, �鼮�����˵�id
   ps:string, �����˵ĸ���
   pos:string, �������λ��
   price:float, �����������

   <strong>����</strong>

   <strong>����</strong>����
 * </pre>
 *
 */
public class SearchSalable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String ISBN = "isbn";
    private static final String BID = "bid";
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String PS = "ps";
    private static final String POS = "pos";
    private static final String PRICE = "price";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    /** ����������Ϊ DetachedCriteria<BorrawableBook>
     * @param dc ���ŵ�DetachedCriteria, ���贫��һ��Criteria<BorrowableBook>
     * @param req ����������, �������е� bid, uid, isbn, pos ����
     */
    public static DetachedCriteria parseCriteria(DetachedCriteria dc, HttpServletRequest req)
    {
        // criterions SalableBook
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

    private static JSONObject jsonize(SalableBook sb)
    {
        JSONObject json = new JSONObject();

        json.put(BID, sb.getId());
        json.put(POS, sb.getPos());
        json.put(PS, sb.getPs());
        json.put(PRICE, sb.getPrice());
        json.put(ISBN, sb.getBook().getIsbn());
        json.put(UID, sb.getBook().getOwner().getId());

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
                DetachedCriteria.forClass(SalableBook.class),
                req
            );

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<SalableBook> l = (List<SalableBook>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            JSONArray json = new JSONArray();

            Iterator<SalableBook> i = l.iterator();
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
