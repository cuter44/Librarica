package com.github.cuter44.util.servlet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
/* util */
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
/* http */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Servlet ������
 * @version 1.0.0 builld 20131212
 */
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
            return(v==null?null:Integer.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
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
            return(v==null?null:Double.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
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
            return(v==null?null:Byte.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static Long getLongParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            return(v==null?null:Long.valueOf(v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static byte[] getByteArrayParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v == null)
                return(null);

            int l = v.length() / 2;

            ByteBuffer buf = ByteBuffer.allocate(l);
            for (int i=0; i<v.length(); i+=2)
            {
                buf.put(
                    Integer.valueOf(
                        v.substring(i, i+2),
                        16
                    ).byteValue()
                );
            }
            return(buf.array());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static Boolean getBooleanParam(HttpServletRequest req, String name)
    {
        String v = getParam(req, name);
        return(v==null?null:Boolean.valueOf(v));
    }

    public static List<String> getStringListParam(HttpServletRequest req, String name)
    {
        try
        {
            String v = getParam(req, name);
            if (v == null)
                return(null);
            if (v.length() == 0)
                return(new ArrayList<String>());

            StringTokenizer st = new StringTokenizer(v, ",");
            List<String> l = new ArrayList<String>(st.countTokens());

            while (st.hasMoreTokens())
                l.add(st.nextToken());

            return(l);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }


    public static List<Long> getLongListParam(HttpServletRequest req, String name)
    {
        try
        {
            List<String> ls = getStringListParam(req, name);
            if (ls == null)
                return(null);

            List<Long> l = new ArrayList<Long>();
            Iterator<String> itr = ls.iterator();
            while (itr.hasNext())
                l.add(Long.valueOf(itr.next()));

            return(l);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }


}
