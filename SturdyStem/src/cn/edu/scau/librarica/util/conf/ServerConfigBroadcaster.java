package cn.edu.scau.librarica.util.conf;

/* io */
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
/* net&servlet */
import java.net.URLDecoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

import java.util.Properties;
import java.util.Enumeration;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.util.conf.Configurator;

public class ServerConfigBroadcaster extends HttpServlet
{
    private static JSONObject jsonize(Properties prop)
    {
        JSONObject json = new JSONObject();

        Enumeration e = prop.propertyNames();
        while (e.hasMoreElements())
        {
            String key = (String)e.nextElement();
            json.put(key, prop.getProperty(key));
        }

        return(json);
    }

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
        resp.setCharacterEncoding("utf-8");

        // Dequote if pend to write binary
        //resp.setContentType("?MIME?");
        //OutputStream out = resp.getOutputStream();

        // Dequote if pend to write chars
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        Properties prop = Configurator.getPublicProperties();

        out.println(
            jsonize(prop).toString()
        );

        return;
    }
}
