package cn.edu.scau.librarica.buddy.servlet;

import java.util.List;
import java.io.*;
import javax.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.buddy.dao.*;

public class J
{
    private static final String ME = "me";
    private static final String OP = "op";
    private static final String R = "r";

    public static JSONObject jsonize(Buddy b)
    {
        JSONObject j = new JSONObject();

        j.put(ME, b.getMe().getId());
        j.put(OP, b.getOp().getId());
        j.put(R , b.getR());

        return(j);
    }

    public static JSONArray jsonize(List<Buddy> l)
    {
        JSONArray j = new JSONArray();

        for (Buddy b:l)
            j.add(jsonize(b));

        return(j);
    }

    public static void write(Buddy b, ServletResponse resp)
        throws IOException
    {
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println(
            jsonize(b).toJSONString()
        );

        return;
    }

    public static void write(List<Buddy> l, ServletResponse resp)
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
