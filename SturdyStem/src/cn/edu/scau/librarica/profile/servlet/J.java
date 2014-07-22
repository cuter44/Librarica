package cn.edu.scau.librarica.profile.servlet;

import java.util.List;
import java.io.*;
import javax.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.profile.dao.*;

class J
{
    private static final String ID          = "id";
    private static final String DNAME       = "dname";
    private static final String TNAME       = "tname";
    private static final String MOTTO       = "motto";
    private static final String AVATAR      = "avatar";
    private static final String POS         = "pos";

    public static JSONObject jsonize(Profile p)
    {
        JSONObject j = new JSONObject();

        j.put(ID        , p.getId());
        j.put(DNAME     , p.getDname());
        j.put(TNAME     , p.getTname());
        j.put(MOTTO     , p.getMotto());
        j.put(AVATAR    , p.getAvatar());
        j.put(POS       , p.getPos());

        for (String key:p.others.stringPropertyNames())
            j.put(key, p.others.getProperty(key));

        return(j);
    }

    public static JSONArray jsonize(List<Profile> l)
    {
        JSONArray j = new JSONArray();

        for (Profile p:l)
            j.add(jsonize(p));

        return(j);
    }

    public static void write(Profile p, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonize(p).toJSONString()
        );

        return;
    }

    public static void write(List<Profile> l, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonize(l).toJSONString()
        );

        return;
    }
}
