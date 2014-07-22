<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage="" %>
<%@ page import="
  java.security.interfaces.RSAPublicKey,

  com.github.cuter44.util.dao.*,
  com.github.cuter44.util.servlet.*,
  com.github.cuter44.util.crypto.*,

  cn.edu.scau.librarica.authorize.dao.*,
  cn.edu.scau.librarica.authorize.core.*,
  cn.edu.scau.librarica.profile.dao.*,
  cn.edu.scau.librarica.profile.core.*
"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
  <title>木瓜 - 登入</title>

  <meta http-equiv="content-type" content="text/html; charset=UTF-8">

  <link rel="stylesheet" href="../css/theme-muuga.css"></style>

  <script src="../js/jq/jquery-1.10.2.js"></script>
  <script src="../js/crypto/rsa.js"></script>
  <script src="../js/crypto/jsbn.js"></script>
  <script src="../js/crypto/prng4.js"></script>
  <script src="../js/crypto/rng.js"></script>

  <!-- it is the demo that illustrate login, you'd better rewrite that -->
 </head>
 <body>
  <h1>登入</h1>

  <div class="indent" id="status">
  </div>

  <%
    HiberDao.begin();
    
    Long uid = HttpUtil.getLongParam(request, "uid");
    byte[] skey = HttpUtil.getByteArrayParam(request, "s");
    byte[] pass = HttpUtil.getByteArrayParam(request, "pass");

    boolean skeyValid = false;

    if (skey != null)
      skeyValid = Authorizer.verifySkey(uid, skey);
  %>

  <%
    if (skeyValid)
    {
      User u = UserMgr.get(uid);
      Profile p = ProfileMgr.get(uid);
  %>
      <div class="indent logged-in">
        <p>
          <strong>已经登入了</strong>
        </p>
        <p>
          <img src="<%=p.getAvatar().replace(":format", "jpg").replace(":size", "64")%>" style="margin:4px;">
          <%=p.getDname()%>
          &nbsp;
          <a href="#">退出登录</a>
        </p>
      </div>
  <%
    }
    else
    {
  %>
      <div class="indent form">
        <fieldset>
          <table>
            <tr>
              <td></td><td><input id="field-uid" name="uid" placeholder="uid" /></td>
            </tr>
            <tr>
              <td></td><td><input id="field-pass" type="password" name="pass" placeholder="密码"/></td>
            </tr>
            <tr>
              <td></td><td><input type="button" value="登录" onclick="doLogin();"/></td>
            </tr>
          </table>
        </fieldset>
      </div>
      <script>
        function doLogin()
        {
          $.ajaxSetup({async:false});

          var key = getKey($("#field-uid").val());
          var rsa = new RSAKey();
          rsa.setPublic(key.m, key.e);

          $.getJSON(
            "login.api",
            {uid:$("#field-uid").val(), pass:rsa.encrypt($("#field-pass").val())}
          ).success(
            function(json) {
              document.cookie = "uid="+json.uid+"; max-age=2592000; path=<%=request.getContextPath()%>";
              document.cookie = "s="+json.s+"; max-age=2592000; path=<%=request.getContextPath()%>";
              <%
                String redirect = HttpUtil.getParam(request, "redirect");
                if (redirect!=null)
                  out.println("location.href=\""+redirect+"\";");
                else
                  out.println("location.reload(true);");
              %>
            }
          ).fail(
            function(json) { $("#status").html(json.msg); }          
          );
        }

        function getKey(uid) {
          var key;

          $.getJSON(
            "../security/get-rsa-key.api",
            {uid:uid}
          ).fail(
            function(){throw "获取公钥失败";}
          ).done(
            function(json){key=json;}
          );

          return(key);
        }
      </script>
  <%
    }
  %>
  <%
    HiberDao.commit();
    HiberDao.close();
  %>
 </body>
</html>
