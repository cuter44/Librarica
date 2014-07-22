<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage=""
  import="
    java.io.PrintWriter,
    java.security.interfaces.RSAPublicKey,
    com.github.cuter44.util.dao.*,
    com.github.cuter44.util.crypto.*,
    com.github.cuter44.util.servlet.*,
    cn.edu.scau.librarica.authorize.dao.*,
    cn.edu.scau.librarica.authorize.core.*,
    cn.edu.scau.librarica.authorize.exception.*,
    cn.edu.scau.librarica.profile.dao.*,
    cn.edu.scau.librarica.profile.core.*
  "
%>
<%--
  argument:
  uid:long, REQUIRED, uid
  code:hex, REQUIRED, activation code
  pass:hex, USER-TYPE-IN, password encoded in RSA
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
  <title>木瓜 - 用户激活</title>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="../css/theme-muuga.css"></style>
  <!-- <link rel="stylesheet" href="./css/misc-wechat.css"></style> -->
  <script src="../js/jq/jquery-1.10.2.js"></script>
 </head>
 <body>
  <%
  try {
    Long uid = (Long)HttpUtil.notNull(HttpUtil.getLongParam(request, "uid"));
    String code = (String)HttpUtil.notNull(HttpUtil.getParam(request, "code"));
    // prepare data
    User u = UserMgr.get(uid);
    if (u == null)
      throw(new EntityNotFoundException("No such User:"+uid));
    if (User.REGISTERED>u.getStatus())
      throw(new LoginBlockedException("╮(╯□╰)╭ 嗯...你已经因为禁断的行为被关小黑屋咯~"));
    if (User.REGISTERED<u.getStatus())
      throw(new LoginBlockedException("(^ω^\") 帐户已经是激活状态了啊~"));
    Profile div = ProfileMgr.get(uid);
    String avatar = div.getAvatar().replace(":format", "jpg").replace(":size", "64");
  %>
  <h1>还差一步~</h1>
  
  <div style="max-width:640px;">
  <!--<div class="indent">
    <div class="wechat-wrap-recv">
      <span class="wechat-element">( ^ω^)</span><span class="wechat-balloon wechat-recv">嗨我是木瓜的板娘姬<br />在下表填好名字和密码, 就可以使用了~</span>
    </div>
    <div class="wechat-wrap-sent">
      <span class="wechat-balloon wechat-sent">我去年填了个表的!!</span class="wechat-element"><span>(╯‵д′)╯︵┻━┻</span>
    </div>
    <div class="wechat-wrap-recv">
      <span class="wechat-element">┳━┳ シ(^ω^"ノ)</span><span class="wechat-balloon wechat-recv">就填一个嘛, 最后一个了...</span>
    </div>   
  </div>
  -->

    <div class="indent">
      <p>
      完成激活就可以使用了~
      </p>
      <noscript>
        但是...激活页面要使用 javascript, 你懂的...
        <style type="text/css">
          .form { display:none; }
        </style>
      </noscript>
    </div>

    <div class="indent" id="status">
    </div>

    <div class="indent form">
      <!--<form>-->
        <fieldset>
          <table>
            <tr>
              <td>头像</td>
              <td>
                <div class="input" style="display:inline-block;">
                 <img src="<%=avatar%>" title="抱歉目前还不能上传头像, 请使用 gavatar (^ω^b)">
                </div>
              </td>
            </tr>
            <tr>
              <td>昵称</td>
              <td><input id="dname" title="随时都可以换" name="dname" value="<%=div.getDname()%>" placeholder="昵称" /></td>
            </tr>
            <tr>
              <td>邮件地址</td>
              <td><input name="mail" title="已经无法变更了!" value="<%=u.getMail()%>" readonly /></td>
            </tr>
            <tr>
              <td>登录密码</td>
              <td><input type="password" id="pass" name="pass" placeholder="登录密码" /></td>
            </tr>
            <input type="hidden" name="uid" value="<%=uid%>"></input>
            <input type="hidden" name="code" value="<%=code%>"></input>
            <tr>
              <td></td>
              <td><input type="button" value="激活!( °д°)→" onclick="doActivate();" /></td>
            </tr>
          </table>
        </fieldset>
      <!--</form>-->
    </div>

  </div>

  <script src="../js/crypto/rsa.js"></script>
  <script src="../js/crypto/jsbn.js"></script>
  <script src="../js/crypto/prng4.js"></script>
  <script src="../js/crypto/rng.js"></script>
  <script>
    function doActivate() {
      $("div.form").slideUp().promise().done(
        function() {
          try {
            $.ajaxSetup({async:false});
            var key = getKey();
            var s = activate(key);
            var profile = chdname(s);
            $("div#status").html("<p>✓ 完成! 可以在应用上登录了<p>");
          } catch (ex) {
            $("div#status").html("<p>× "+ex+"</p>");
          }
      });

      return(false);
    }

    function getKey() {
      var key;

      $("div#status").html("<p>获取RSA公钥...</p>");
      $.getJSON(
        "../security/get-rsa-key.api",
        {uid:"<%=uid%>"}
      ).fail(
        function(){throw "获取公钥失败";}
      ).done(
        function(json){key=json;}
      );

      return(key);
    }


    function activate(key) {
      var s;

      $("div#status").html("<p>加密密码...</p>");
      var rsa = new RSAKey();
      rsa.setPublic(key.m, key.e);
      
      $("div#status").html("<p>设定密码...</p>")
      $.post(
        "./activate.api",
        {
          uid:"<%=uid%>",
          code:"<%=code%>",
          pass:rsa.encrypt($("input#pass").val())
        }
      ).fail(
        function(){throw "设定密码失败";}
      ).done(
        function(json){s=json;}
      );

      return(s);
    }

    function chdname(wrapped) {
      var p;

      $("div#status").html("<p>设定昵称...</p>");
      $.post(
        "../profile/update.api",
        {
          uid:"<%=uid%>",
          s:wrapped.s,
          dname:$("input#dname").val()
        }
      ).fail(
        function(){throw "设定昵称失败";}
      ).done(
        function(json){p=json;}
      );

      return(p);
    }
  </script>

  <%
  }
  catch (LoginBlockedException ex)
  {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    out.println("<h1>阿勒...? (°ω°\")</h1><div class=\"indent\"><p>激活不能!</p><p><strong>"+ex.getMessage()+"</strong></p></div>");
  }
  catch (EntityNotFoundException ex)
  {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    out.println("<h1>阿勒...? (°ω°\")</h1><div class=\"indent\">用户名单上没有你哦, (度娘腔)\"您要找的是不是: <a>注册</a>\"</div>");
  }
  catch (MissingParameterException ex)
  {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    out.println("<h1>一定是打开的方式不对... (= =b)</h1><div class=\"indent\">好像少了参数诶, 这样真的大丈夫吗?<br />");
    out.print("<code>");
    out.print(request.getRequestURL()+"?"+request.getQueryString());
    out.print("</code>");
    out.println("</div>");
  }
  catch (Exception ex)
  {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    out.println("<h1>服务器君倒地了 ( ×﹏×)</h1><div class=\"indent\">可以帮忙截个图发给<a href='https://github.com/cuter44/Librarica/issues'>维护君</a>吗?<br />");
    out.print("<code>");
    ex.printStackTrace(new PrintWriter(out));
    out.print("</code>");
    out.println("</div>");
  }
  %>
 </body>
</html>
