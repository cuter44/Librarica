package cn.edu.scau.librarica.util.conf;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URLDecoder;
import javax.servlet.ServletException;
import java.util.Properties;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.util.conf.Configurator;

public class ServerConfigBroadcaster extends HttpServlet
{

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        this.doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        Properties prop = Configurator.getPublicProperties();

        JSONObject j = new JSONObject();
        for (String key:prop.stringPropertyNames())
            j.put(key, prop.get(key));

        out.println(
           j.toString()
        );

        return;
    }
}
