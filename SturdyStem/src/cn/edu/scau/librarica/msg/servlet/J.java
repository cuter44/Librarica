package cn.edu.scau.librarica.msg.servlet;

import java.io.*;
import java.util.List;
import javax.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.msg.dao.*;

public class J
{
    private static final String C = "c";
    private static final String F = "f";
    private static final String T = "t";
    private static final String M = "m";

    public static JSONObject jsonize(Msg m)
    {
        JSONObject j = new JSONObject();

        j.put(C, m.getC());
        j.put(F, m.getF().getId());
        j.put(T, m.getT().getId());
        j.put(M, m.getM().getTime());

        return(j);
    }

    public static JSONArray jsonize(List<Msg> l)
    {
        JSONArray j  =new JSONArray();

        for (Msg m:l)
            j.add(jsonize(m));

        return(j);
    }

    public static void write(Msg m, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonize(m).toJSONString()
        );

        return;
    }

    public static void write(List<Msg> l, ServletResponse resp)
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

    public static void writeOK(ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            "{\"error\":\"ok\"}"
        );

        return;
    }
}
