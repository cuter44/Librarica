package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;

import com.alibaba.fastjson.*;
import static com.github.cuter44.util.crypto.CryptoUtil.byteToHex;

import cn.edu.scau.librarica.authorize.dao.*;

class J
{
    protected static String ID = "id";
    protected static String UID = "uid";
    protected static String UNAME = "uname";
    protected static String MAIL = "mail";
    protected static String S = "s";
    protected static String STATUS = "status";
    protected static String USER_TYPE = "userType";
    protected static String REG_DATE = "regDate";

    protected static JSONObject jsonizePublic(User u)
    {
        JSONObject j = new JSONObject();

        j.put(ID, u.getId());
        j.put(UID, u.getId());
        j.put(UNAME, u.getUname());
        j.put(MAIL, u.getMail());
        j.put(STATUS, u.getStatus());
        j.put(USER_TYPE, u.getUserType());
        j.put(REG_DATE, u.getRegDate());

        return(j);
    }

    protected static JSONObject jsonizePrivate(User u)
    {
        JSONObject j = jsonizePublic(u);

        if (u.getSkey()!=null)
            j.put(S, byteToHex(u.getSkey()));

        return(j);
    }

    protected static JSONArray jsonizePublic(List<User> l)
    {
        JSONArray a = new JSONArray();

        for (User u:l)
            a.add(jsonizePublic(u));

        return(a);
    }

    public static void writeUserPrivate(User u, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonizePrivate(u).toJSONString()
        );

        return;
    }

    public static void writeUserPublic(User u, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonizePublic(u).toJSONString()
        );

        return;
    }

    public static void writeUserListPublic(List<User> l, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonizePublic(l).toJSONString()
        );

        return;
    }


}
