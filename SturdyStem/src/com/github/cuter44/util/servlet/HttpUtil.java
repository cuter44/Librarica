package com.github.cuter44.util.servlet;

/* util */
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
/* http */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
/* log */
import org.apache.log4j.Logger;

public class HttpUtil
{
    /**
     * �� HTTP �����м����Ӧ����ֵ�ļ���װ
     *
     * û�и������Ĳ���ʱ����null
     * (!) ���������� Session �м�� Object, �᷵�����ǵ� toString()
     * ����˳��Ϊ Http������� > Session > Cookie
     * @param req Http����
     * @param name ����������
     * @return String ������ֵ
     */
    public static String getParam(HttpServletRequest req, String name)
    {
        // Server-side Attribute
        Object ra = req.getAttribute(name);
        if (ra != null)
            return(ra.toString());

        // Http Parameter
        String value = null;
        if ((value = req.getParameter(name)) != null)
            return(value);

        // Session
        HttpSession s = req.getSession();
        Object sa = s.getAttribute(name);
        if (sa != null)
            return(sa.toString());

        // Cookies
        Cookie[] carr = req.getCookies();
        if (carr != null)
            for (int i=0; i<carr.length; i++)
                if (carr[i].getName().equals(name))
                    return(carr[i].getValue());

        return(null);
    }

  // WRAPPER
    /**
     * ͬ getParam() ����ת��Ϊ Integer ����
     *
     * �����޷�ת����ֵ����null, û�ж�Ӧ��ֵ����null
     * ͬ����Ϊ��һ��ת������Ч���Ե�
     * @param req Http����
     * @param name ����������
     * @return Integer ������ֵ
     */
    public static Integer getIntParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v != null)
                return(Integer.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Logger.getLogger("librarica.servlet")
                .error(ex.toString());
        }
        return(null);
    }

    /**
     * ͬ getParam() ����ת��Ϊ Double ����
     *
     * �����޷�ת����ֵ����null, û�ж�Ӧ��ֵ����null
     * ͬ����Ϊ��һ��ת������Ч���Ե�
     * @param req Http����
     * @param name ����������
     * @return Double ������ֵ
     */
    public static Double getDoubleParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v != null)
                return(Double.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Logger.getLogger("librarica.servlet")
                .error(ex.toString());
        }
        return(null);
    }

    /**
     * ͬ getParam() ����ת��Ϊ Byte ����
     *
     * �����޷�ת����ֵ����null, û�ж�Ӧ��ֵ����null
     * ͬ����Ϊ��һ��ת������Ч���Ե�
     * @param req Http����
     * @param name ����������
     * @return Byte ������ֵ
     */
    public static Byte getByteParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v != null)
                return(Byte.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Logger.getLogger("librarica.servlet")
                .error(ex.toString());
        }
        return(null);
    }

    public static Long getLongParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v != null)
                return(Long.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Logger.getLogger("librarica.servlet")
                .error(ex.toString());
        }
        return(null);
    }

    public static List<Long> getLongListParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v == null)
                return(null);

            StringTokenizer st = new StringTokenizer(v, ",");
            List<Long> l = new ArrayList<Long>(st.countTokens());

            while (st.hasMoreTokens())
                l.add(Long.valueOf(st.nextToken()));

            return(l);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Logger.getLogger("librarica.servlet")
                .error(ex.toString());
        }
        return(null);
    }
}
