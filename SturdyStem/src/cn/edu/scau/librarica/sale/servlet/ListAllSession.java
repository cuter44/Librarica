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
import org.hibernate.Query;

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.sale.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** �г�������������ĻỰ
 * <pre style="font-size:12px">

   <strong>����</strong>
   GET/POST /sale/list-both

   <strong>����</strong>
   uid:long, ����, ָ����ϵ�ߵ�uid
   statusl:byte, ��ѡ, ָ����������(��պ�), ȱʡΪ0(REQUESTED)
   statush:byte, ��ѡ, ָ����������(�ұպ�), ȱʡΪ1(ACCEPTED)
   <i>��ҳ</i>
   start:int, ���ؽ������ʼ����, ȱʡ�� 1 ��ʼ
   size:int, ���ؽ����������, ȱʡʹ�÷���������

   <strong>��Ӧ</strong>
   application/json Array of:
   id:long,
   status:byte,
   book:long,
   buyer:long,
   tmStatus:long-timestamp
   qty:int, quantity of the deal
   <i>�����ڽ�������</i>

   <strong>����</strong>

   <strong>����</strong>����
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
    private static final String BUYER = "buyer";
    private static final String TMSTATUS = "tmStatus";
    private static final String QTY = "qty";
    private static final String START = "start";
    private static final String SIZE = "size";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    private static JSONObject jsonize(BuySession bs)
    {
        JSONObject json = new JSONObject();

        json.put(ID, bs.getId());
        json.put(STATUS, bs.getStatus());
        json.put(BOOK, bs.getBook().getId());
        json.put(BUYER, bs.getBuyer().getId());
        json.put(TMSTATUS, bs.getTmStatus().getTime());
        json.put(QTY, bs.getQty());

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
            statusl = (statusl!=null ? statusl : BuySession.REQUESTED);
            Byte statush = HttpUtil.getByteParam(req, STATUSH);
            statush = (statush!=null ? statush : BuySession.ACCEPTED);

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = (size!=null ? size : defaultPageSize);

            HiberDao.begin();

            Query hql = HiberDao.createQuery(
                    "FROM BuySession bs "
                    +"WHERE ((buyer.id=:uid) OR (book.owner.id=:uid)) "
                    +"AND (status>=:statusl) AND (status<=:statush)"
                    +"ORDER BY tmStatus DESC"
                )
                .setLong("uid", uid)
                .setByte("statusl", statusl)
                .setByte("statush", statush);
            if (start != null)
                hql.setFirstResult(start);
            hql.setMaxResults(size);

            List<BuySession> l = hql.list();

            HiberDao.commit();

            JSONArray json = new JSONArray();

            for (BuySession bs : l)
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
