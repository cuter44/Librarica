package cn.edu.scau.librarica.util.conf;

import java.util.Properties;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * �� /tvprotal.properties �� /tvprotal.public.properties ��ȡ��������Ӧ��Ӧ�ó���
 */
public class Configurator
{
    private static Logger logger = Logger.getLogger(Configurator.class);

    private Properties publicProp;
    private Properties prop;

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
            InputStreamReader is;

            is = new InputStreamReader(
                this.getClass()
                    .getResourceAsStream("/librarica.public.properties"),
                "utf-8"
            );

            this.publicProp = new Properties();
            this.publicProp.load(is);
            is.close();

            is = new InputStreamReader(
                this.getClass()
                    .getResourceAsStream("/librarica.properties"),
                "utf-8"
            );

            this.prop = new Properties(this.publicProp);
            this.prop.load(is);
            is.close();
        }
        catch (Exception ex)
        {
            logger.error("Load configuration failed.", ex);
        }
    }

    /** ���������ļ�
     */
    public static void reload()
    {
        Singleton.instance.load();
    }

    /** ����������Կ��ֻ������
     * �������ǽ�ԭ�� prop ��ΪĬ�ϱ����±�����.
     * ���, �Եõ��ı��������޸Ĳ���Ӱ�쵽ԭ��.
     */
    public static Properties getProperties()
    {
        Properties prop = new Properties(Singleton.instance.prop);

        return(prop);
    }

    /** ��ù������Կ��ֻ������
     * �������ǽ�ԭ�� prop ��ΪĬ�ϱ����±�����.
     * ���, �Եõ��ı��������޸Ĳ���Ӱ�쵽ԭ��.
     * ��Ҫ���ڽ�������������ֵ���߿ͻ���.
     */
    public static Properties getPublicProperties()
    {
        Properties prop = new Properties(Singleton.instance.publicProp);

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

    public static Long getLong(String name)
    {
        return(
            Long.valueOf(
                get(name)
            )
        );
    }

    public static Long getLong(String name, Long defaultValue)
    {
        try
        {
            Long v = Long.valueOf(get(name));

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
