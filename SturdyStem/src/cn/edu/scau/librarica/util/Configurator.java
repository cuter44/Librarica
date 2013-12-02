package cn.edu.scau.librarica.util;

import java.util.Properties;

import java.io.InputStreamReader;

/**
 * �� /tvprotal.properties ��ȡ��������Ӧ��Ӧ�ó���
 */
public class Configurator
{
    private Properties prop = null;

    private static class Singleton
    {
        private static Configurator instance = new Configurator();
    }

    private Configurator()
    {
        this.load();
    }

    /** ���������ļ�
     */
    private void load()
    {
        try
        {
            this.prop = new Properties();
            this.prop.load(
                new InputStreamReader(
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("/librarica.properties"),
                    "utf-8"
                )
            );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /** ���������ļ�
     */
    public static void reload()
    {
        Singleton.instance.load();
    }

    /** ������� Properties ������
     * ��ʵ���ǽ����õ� Properties ��Ϊ�󱸷���.
     * �������������̰߳�ȫ��, Ҳ�����������������µ����Զ���Ӱ������ֵ
     */
    public static Properties getProperties()
    {
        Properties prop = new Properties(Singleton.instance.prop);

        return(prop);
    }

    /**
     * ��ȡ�����ļ��еĲ���
     */
    public static String get(String name)
    {
        return(
            Singleton.instance.prop.getProperty(name)
        );
    }

    public static String get(String name, String defaultValue)
    {
        try
        {
            String v = Singleton.instance.prop.getProperty(name);

            if (v != null)
                return(v);
            else
                throw(new IllegalArgumentException("Missing required config key: " + name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(defaultValue);
        }
    }


    public static Integer getInt(String name)
    {
        return(
            Integer.valueOf(
                get(name)
            )
        );
    }

    public static Integer getInt(String name, Integer defaultValue)
    {
        try
        {
            Integer v = Integer.valueOf(get(name));

            if (v != null)
                return(v);
            else
                throw(new IllegalArgumentException("Missing required config key: " + name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(defaultValue);
        }
    }

    public static Double getDouble(String name)
    {
        return(
            Double.valueOf(
                get(name)
            )
        );
    }

    public static Double getDouble(String name, Double defaultValue)
    {
        try
        {
            Double v = Double.valueOf(get(name));

            if (v != null)
                return(v);
            else
                throw(new IllegalArgumentException("Missing required config key: " + name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(defaultValue);
        }
    }
}
