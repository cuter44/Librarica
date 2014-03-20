package cn.edu.scau.librarica.profile.servlet;

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

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.profile.dao.*;
import cn.edu.scau.librarica.profile.core.*;
import cn.edu.scau.librarica.util.conf.Configurator;

/** �����û�����
 * <pre style="font-size:12px">

   <strong>����</strong>
   GET/POST /profile/search

   <strong>����</strong>
   <i>���µ�����һ��</i>
   <i>��ȷƥ��</i>
   id:long, ָ��uid, ��ֵʱ�Զ��ŷָ�;
   <i>����ƥ��</i>
   q:string, �� mail, uname, dname, tname �����а���ƥ��(%key%)
   <i>�������</i>
   verbose:boolean, ���Դ�Сд, Ϊ true ʱ���ȫ������(�μ� <strong>��Ӧ</strong>)
   <i>��ҳ</i>
   start:int, ���ؽ������ʼ����
   size:int, ���ؽ����������

   <strong>��Ӧ</strong>
   application/json(array); charset=utf-8:
   <i>non-verbose</i>
   id:long, uid
   dname:string(48), ��ʾ��
   avatar:url(255), ͷ���URL
   motto:string(255), ����ǩ��
   pos:base32-geohash(24), λ����Ϣ
   <i>verbose, ��non-verbose��׷������:</i>
   stored:long, ��������
   borrowing:long, ���ڽ��ĵ������
   borrowed:long, �ۼƽ��ļ���
   lent:long, �ۼƽ������
   bought:long, �ۼƹ������
   sold:long, �ۼƳ��ۼ���
   like:long, ��ע������
   liked:long, ��$(liked)�˹�ע
   hate:long, ���ڵ�����
   hated:long, ��$(hated)������

   mail:rfc533x-address �ʼ���ַ
   uname:uname(?), ��¼��
   tname:string(48), ��֤ʱ�����ı�ʶ��
   <strong>����</strong>
   û�в�ѯ����ʱ���� Bad Request(400):{"flag":"!parameter"}

   <strong>����</strong>
   <i>#1</i>
    curl "http://localhost:8080/librarica/profile/search?id=1"

    [{"dname":"a","id":1,"motto":"blabla","avatar":"http://gravatar.com/blabla","pos":"abcekd"}]

   <i>#2</i>
    curl "http://localhost:8080/librarica/profile/search?q=d&verbose=true"

    [{
        "borrowed":0,
        "borrowing":0,
        "bought":0,
        "dname":"d",
        "hate":0,
        "hated":0,
        "id":4,
        "lent":0,
        "like":0,
        "liked":0,
        "mail":"d@localhost",
        "sold":0,
        "stored":0
    }]
 * </pre>
 *
 */
@Deprecated
public class SearchProfile extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String VERBOSE = "verbose";
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String ID = "id";
    private static final String Q = "q";

    private static final String DNAME = "dname";
    private static final String TNAME = "tname";
    private static final String MOTTO = "motto";
    private static final String AVATAR = "avatar";
    private static final String POS = "pos";

    private static final String STORED = "stored";

    private static final String BORROWING = "borrowing";
    private static final String BORROWED = "borrowed";
    private static final String LENT = "lent";

    private static final String BOUGHT = "bought";
    private static final String SOLD = "sold";

    private static final String LIKE = "like";
    private static final String LIKED = "liked";
    private static final String HATE = "hate";
    private static final String HATED = "hated";

    private static final String MAIL = "mail";
    private static final String UNAME = "uname";

    private static final Integer defaultPageSize = Configurator.getInt("librarica.search.defaultpagesize", 20);

    private static JSONObject jsonize(Profile p, boolean verbose)
    {
        JSONObject j = new JSONObject();

        j.put(ID, p.getId());
        j.put(DNAME, p.getDname());
        j.put(MOTTO, p.getMotto());
        j.put(AVATAR, p.getAvatar());
        j.put(POS, p.getPos());

        if (verbose)
        {
            j.put(TNAME, p.getTname());

            j.put(STORED, p.getStored());

            j.put(BORROWING, p.getBorrowing());
            j.put(BORROWED, p.getBorrowed());
            j.put(LENT, p.getLent());

            j.put(BOUGHT, p.getBought());
            j.put(SOLD, p.getSold());

            j.put(LIKE, p.getLike());
            j.put(LIKED, p.getLiked());
            j.put(HATE, p.getHate());
            j.put(HATED, p.getHated());

            User u = p.getUser();
            j.put(MAIL, u.getMail());
            j.put(UNAME, u.getUname());
        }

        return(j);
    }

    private static JSONArray jsonize(List<Profile> lp, boolean verbose)
    {
        JSONArray json = new JSONArray(lp.size());

        Iterator<Profile> i = lp.iterator();
        while (i.hasNext())
            json.add(jsonize(i.next(), verbose));

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
            Boolean verbose = HttpUtil.getBooleanParam(req, VERBOSE);
            verbose = verbose!=null?verbose:Boolean.FALSE;

            Integer start = HttpUtil.getIntParam(req, START);
            Integer size = HttpUtil.getIntParam(req, SIZE);
            size = size!=null?size:defaultPageSize;

            HiberDao.begin();

            List<Long> uids = HttpUtil.getLongListParam(req, ID);
            if (uids != null)
            {
                DetachedCriteria dc = DetachedCriteria.forClass(Profile.class)
                    .add(Restrictions.in("id", uids));

                List<Profile> lp = (List<Profile>)HiberDao.search(dc);

                out.println(jsonize(lp, verbose).toJSONString());

                return;
            }

            String q = HttpUtil.getParam(req, Q);
            if (q != null && q.length() > 0)
            {
                q = "%"+q+"%";
                List<Profile> lp = (List<Profile>)HiberDao.createQuery(
                    "SELECT p FROM Profile p INNER JOIN p.user u "+
                        "WHERE p.dname LIKE :q OR p.tname LIKE :q "+
                        "OR    u.uname LIKE :q OR u.mail LIKE :q")
                    .setString("q", q)
                    .list();

                out.println(jsonize(lp, verbose).toJSONString());

                return;
            }

            throw(new MissingParameterException());
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
