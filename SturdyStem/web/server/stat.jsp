<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage=""
import="
  java.util.Hashtable,
  java.util.Set,
  java.util.Iterator,
  org.hibernate.HibernateException,
  org.hibernate.criterion.*,
  com.github.cuter44.util.dao.*, 
  cn.edu.scau.librarica.authorize.dao.*,
  cn.edu.scau.librarica.shelf.dao.*,
  cn.edu.scau.librarica.lend.dao.*,
  cn.edu.scau.librarica.buddy.dao.*,
  cn.edu.scau.librarica.sale.dao.*,
  cn.edu.scau.librarica.util.audit.UriAuditor
" %>
<%
try
{
  HiberDao.begin();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>木瓜 - 统计数据</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="../css/theme-muuga.css"></style>
    <link rel="stylesheet" href="./css/page-stat.css"></style>
  </head>

  <body>
    <div id="content-wrap">
      <h1>Chaenomeles Statistics</h1>
      <div class="indent">
        (≧▽≦)っ Muuga!
        <br />
        这里显示了木瓜的统计数据, 可以用来...用来干什么? Σ(°д°|||)
      </div>

      <h2>用户</h2>
      <div class="indent expandable">
      <%
        try
        {
          long count;
          DetachedCriteria dc;

          // ACTIVATED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.ACTIVATED));
          count = HiberDao.count(dc);
          out.println("注册用户&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;只, 其中<br />");
          %>
            <div class="indent">
            <%
              count = (Long)HiberDao
                .createQuery("select count(a) from Buddy a, Buddy b where a.me=b.op and a.op=b.me and a.r=b.r and a.r=:like")
                .setByte("like",Buddy.LIKE)
                .uniqueResult()/2L;
              out.println("有&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;对相互关注的好基友<br />");

              count = (Long)HiberDao
                .createQuery("select count(a) from Buddy a, Buddy b where a.me=b.op and a.op=b.me and a.r=b.r and a.r=:hate")
                .setByte("hate",Buddy.HATE)
                .uniqueResult()/2L;
              out.println("有&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;对割席断交<br />");
            
            %>
            </div>
          <%
          // CANCELED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.CANCELED));
          count = HiberDao.count(dc);
          out.println("跑掉的用户&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;只<br />");

          // REGISTERED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.REGISTERED));
          count = HiberDao.count(dc);
          out.println("幽灵用户&nbsp<span class='stat stat-num"+(count!=0L?" stat-bad":"")+"'>"+count+"</span>&nbsp;只<br />");

          // CANCELED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.BANNED));
          count = HiberDao.count(dc);
          out.println("小黑屋里的用户&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;只<br />");
        }
        catch (HibernateException ex)
        {
          ex.printStackTrace();
          out.println("数据库连接不能 (×﹏×)<br />");
        }
      %>
      </div>

      <h2>书籍</h2>
      <div class="indent">
      <%
        try
        {
          long count;
          DetachedCriteria dc;

          // TOTAL
          dc = DetachedCriteria.forClass(Book.class);
          count = HiberDao.count(dc);
          out.println("全部用户的藏书总计&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;册<br />");

          // BORROWABLE
          dc = DetachedCriteria.forClass(BorrowableBook.class);
          count = HiberDao.count(dc);
          out.println("就是现在, 可供借阅的书籍&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;册<br />");

          // SALABLE
          dc = DetachedCriteria.forClass(SalableBook.class);
          count = HiberDao.count(dc);
          out.println("...以及可供购买的书籍&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;册<br />");
        }
        catch (HibernateException ex)
        {
          out.println("数据库连接不能 (×﹏×)<br />");
        }
      %>
      </div>

      <h2>从始至今... </h2>
      <div class="indent">
      <%
        try
        {
          long count;
          DetachedCriteria dc;

          // BORROW
          dc = DetachedCriteria.forClass(BorrowSession.class);
          count = HiberDao.count(dc);
          out.println("产生了&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;次借阅请求, 其中&nbsp;");

          dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.eq("status", BorrowSession.CLOSED));
          count = HiberDao.count(dc);
          out.println("<span class='stat stat-num'>"+count+"</span>&nbsp;个顺利达成了<br />");

          // SALE
          dc = DetachedCriteria.forClass(BuySession.class);
          count = HiberDao.count(dc);
          out.println("产生了&nbsp<span class='stat stat-num'>"+count+"</span>&nbsp;次交易请求, 其中&nbsp;");

          dc = DetachedCriteria.forClass(BuySession.class)
            .add(Restrictions.eq("status", BuySession.CLOSED));
          count = HiberDao.count(dc);
          out.println("<span class='stat stat-num'>"+count+"</span>&nbsp;个顺利达成了<br />");

        }
        catch (HibernateException ex)
        {
          out.println("数据库连接不能 (×﹏×)<br />");
        }
      %>
      </div>

      <h2>前一分钟... </h2>
      <div class="indent">
      <%
        try
        {
          // TOTAL
          Hashtable<String, Integer> m = UriAuditor.getStatistics();
          out.println("木瓜伺服了&nbsp<span class='stat stat-num'>"+(m.get("")!=null?m.get(""):0)+"</span>&nbsp;个请求, 分布在以下 URI <br />");
      %>
        <div class="indent">
        <%
          Iterator<String> i = m.keySet().iterator();
          while (i.hasNext())
          {
            String k = i.next();
            if ("".equals(k))
              continue;

            out.println("<span class='stat stat-uri-count'>"+m.get(k)+"</span>&nbsp;@&nbsp;<span class='stat stat-uri'>"+k+"</span><br />");
          }
        %>
        </div>
      <%
        }
        catch (Exception ex)
        {
          out.println("好像哪里不对... (= =b)<br />");
        }
      %>
      </div>
      <div id="footer">
        {{footer}}
      </div>
    </div>
  </body>
</html>
<%
}
finally
{
  HiberDao.close();
}
%>