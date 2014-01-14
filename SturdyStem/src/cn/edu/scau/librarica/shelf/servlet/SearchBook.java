package cn.edu.scau.librarica.shelf.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

import org.hibernate.criterion.*;

/** ����/�г�����
 * �ɽ�/�����鼮��������ͨ������Ľӿ��ṩ
 * <pre style="font-size:12px">

   <strong>����</strong>
   GET/POST /book/search

   <strong>����</strong>
   <i>���²�����ѡ���ظ�, ��ͬ������Ϊ��, ��������or����, ���������and����:</i>
   bid:long, ָ�����id;
   uid:long, ָ����ӵ����id;
   isbn:string, ָ��isbn;
   <i>��ҳ</i>
   start:int, ���ؽ������ʼ����
   size:int, ���ؽ����������

   <strong>��Ӧ</strong>
   application/json Array:
   bid:long, bid
   isbn:string, isbn
   uid:long, �鼮�����˵�id

   <strong>����</strong>

   <strong>����</strong>����
 * </pre>
 *
 */
@Deprecated
public class SearchBook extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String ISBN = "isbn";
    private static final String BID = "bid";
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    /** ����������Ϊ Criteria<Book>
     * @param dc ���ŵ�Criteria, ���贫��һ��Criteria<Book>
     * @param req ����������, �������е� bid, uid, isbn ����
     */
    public static DetachedCriteria parseCriteria(DetachedCriteria dc, HttpServletRequest req)
    {
        String[] values;
        // bid
        values = req.getParameterValues(BID);
        if (values != null)
        {
            Disjunction disj = Restrictions.disjunction();
            for (int i=0; i<values.length; i++)
            {
                Long bid = Long.valueOf(values[i]);
                disj.add(Restrictions.eq("id", bid));
            }
            dc.add(disj);
        }

        // isbn
        values = req.getParameterValues(ISBN);
        if (values != null)
        {
            Disjunction disj = Restrictions.disjunction();
            for (int i=0; i<values.length; i++)
                disj.add(Restrictions.eq("isbn", values[i]));
            dc.add(disj);
        }

        // uid
        values = req.getParameterValues(UID);
        if (values != null)
        {
            DetachedCriteria dcOwner = dc.createCriteria("owner");
            Disjunction disj = Restrictions.disjunction();
            for (int i=0; i<values.length; i++)
            {
                Long uid = Long.valueOf(values[i]);
                disj.add(Restrictions.eq("id", uid));
            }
            dcOwner.add(disj);
        }

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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        JSONArray json = new JSONArray();

        try
        {
            DetachedCriteria dc = parseCriteria(
                DetachedCriteria.forClass(Book.class),
                req
            );

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            if (size == null)
                size = defaultPageSize;

            HiberDao.begin();

            List<Book> l = (List<Book>)HiberDao.search(dc, start, size);

            HiberDao.commit();

            for (int i=0; i<l.size(); i++)
                json.add(jsonize(l.get(i)));

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
