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
    <meta charset="utf-8"/>
    <link rel="stylesheet" href="./css/template-hasaha.css"></style>
    <link rel="stylesheet" href="./css/page-stat.css"></style>
  </head>

  <body>
    <div id="content-wrap">
      <h1>Chaenomeles Statistics</h1>
      <div class="indent">
        (≧▽≦)っ Muuga 伺服中!
        <br />
        检视以下数据以确保木瓜运作良好哦!
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
          out.println("注册用户&nbsp<span class='stat-num'>"+count+"</span>&nbsp;只<br />");

          // CANCELED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.CANCELED));
          count = HiberDao.count(dc);
          out.println("跑掉的用户&nbsp<span class='stat-num'>"+count+"</span>&nbsp;只<br />");

          // REGISTERED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.REGISTERED));
          count = HiberDao.count(dc);
          out.println("幽灵用户&nbsp<span class='stat-num"+(count!=0L?" stat-bad":"")+"'>"+count+"</span>&nbsp;只<br />");

          // CANCELED
          dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("status", User.BANNED));
          count = HiberDao.count(dc);
          out.println("小黑屋里的用户&nbsp<span class='stat-num'>"+count+"</span>&nbsp;只<br />");
        }
        catch (HibernateException ex)
        {
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
          out.println("全部用户的藏书总计&nbsp<span class='stat-num'>"+count+"</span>&nbsp;册<br />");

          // BORROWABLE
          dc = DetachedCriteria.forClass(BorrowableBook.class);
          count = HiberDao.count(dc);
          out.println("就是现在, 可供借阅的书籍&nbsp<span class='stat-num'>"+count+"</span>&nbsp;册<br />");

          // SALABLE
          count = 0L;
          out.println("<del>");
          out.println("...以及可供购买的书籍&nbsp<span class='stat-num"+(count!=0L?" stat-bad":"")+"'>"+count+"</span>&nbsp;册<br />");
          out.println("</del>");
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

          // TOTAL
          dc = DetachedCriteria.forClass(BorrowSession.class);
          count = HiberDao.count(dc);
          out.println("产生了&nbsp<span class='stat-num'>"+count+"</span>&nbsp;次借阅请求, 其中<br />");
      %>
        <div class="indent">
        <%
          dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.eq("status", BorrowSession.CLOSED));
          count = HiberDao.count(dc);
          out.println("<span class='stat-num'>"+count+"</span>&nbsp;个顺利达成了<br />");

          dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.eq("status", BorrowSession.CLOSED));
          count = HiberDao.count(dc);
          out.println("<span class='stat-num'>"+count+"</span>&nbsp;个被拒绝了<br />");

        %>
        </div>
      <%
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
          out.println("木瓜伺服了&nbsp<span class='stat-num'>"+(m.get("")!=null?m.get(""):0)+"</span>&nbsp;个请求, 分布在以下 URI <br />");
      %>
        <div class="indent">
        <%
          Iterator<String> i = m.keySet().iterator();
          while (i.hasNext())
          {
            String k = i.next();
            if ("".equals(k))
              continue;

            out.println("<span class='stat-uri-count'>"+m.get(k)+"</span>&nbsp;@&nbsp;<span class='stat-uri'>"+k+"</span><br />");
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