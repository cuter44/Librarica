<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage="" %>
<%@ page import="
  com.github.cuter44.util.dao.*,
  com.github.cuter44.util.servlet.*,
  com.github.cuter44.util.crypto.*,
  com.github.cuter44.util.text.*,

  cn.edu.scau.librarica.authorize.dao.*,
  cn.edu.scau.librarica.authorize.core.*,
  cn.edu.scau.librarica.profile.dao.*,
  cn.edu.scau.librarica.profile.core.*
"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
  <title>木瓜 - IM</title>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="../css/theme-muuga.css"></style>
  <script src="../js/jq/jquery-1.10.2.js"></script>
 </head>
 <body>
  <%
    HiberDao.begin();
  %>
  <%
    Long    uid = HttpUtil.getLongParam(request, "uid");
    byte[]  s   = HttpUtil.getByteArrayParam(request, "s");
    if (uid==null || s==null || !Authorizer.verifySkey(uid, s))
    {
      String url = request.getRequestURL().toString();
      String   q = request.getQueryString();
      response.sendRedirect(
        "../user/login.jsp?redirect="+URLBuilder.encodeURIComponent(q!=null?url+"?"+q:url)
      );
      return;
    }
  %>
  <script>
    var my_uid = "<%=uid%>";
    var s = "<%=CryptoUtil.byteToHex(s)%>";
  </script>
  <h1>wwwww</h1>
  <style>
    div {
      border: 1px dotted white;
    }

    #top-wrap {
      position: relative;
      width: 80%;
      min-width: 720px;
      margin: 0px auto;
    }

    #contact-wrap, #chat-wrap {
      display: inline-block;
      height: 540px;
    }

    #contact-wrap {
      width: 240px;
    }

    #chat-wrap {
      position: absolute;
      top: 0px;
      left: 240px;
      right: 0px;
    }

    #chat-container {
      position: absolute;
      top:0px;
      left:0px;
      right:0px;
      bottom:0px;
    }

    #reply-container{
      position:absolute;
      bottom:0px;

      width: 100%;
    }
  </style>
  <div id="top-wrap">
    <!-- left -->
    <div id="contact-wrap">
      <div id="new-contact-container">
        <input id="new-contact-id" placeholder="输入对方id发起聊天"/>&nbsp;<input type="button" value=" + " onclick="" />
      </div>
      <div id="contacts-container">
      </div>
    </div>
    <!-- right -->
    <div id="chat-wrap">
      <div id="chat-container">
      </div>
      <div id="reply-container">
        <textarea id="reply-msg" placeholder="发信/回复"></textarea>&nbsp;<input type="button" id="reply-send" value="发送">
      </div>
    </div>
  </div>
  <script>
  </script>
  <%
    HiberDao.commit();
    HiberDao.close();
  %>
 </body>
</html>
