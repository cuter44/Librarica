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
        String v = Singleton.instance.prop.getProperty(name);

        return(
            v!=null ? v : null
        );
    }


    public static Integer getInt(String name)
    {
        try
        {
            String v = get(name);
            return(
                v!=null ? Integer.valueOf(v) : null
            );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static Integer getInt(String name, Integer defaultValue)
    {
        Integer v = getInt(name);
        return(
            v!=null ? v : defaultValue
        );
    }

    public static Long getLong(String name)
    {
        try
        {
            String v = get(name);
            return(
                v!=null ? Long.valueOf(v) : null
            );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static Long getLong(String name, Long defaultValue)
    {
        Long v = getLong(name);
        return(
            v!=null ? v : defaultValue
        );
    }

    public static Double getDouble(String name)
    {
        try
        {
            String v = get(name);
            return(
                v!=null ? Double.valueOf(v) : null
            );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static Double getDouble(String name, Double defaultValue)
    {
        Double v = getDouble(name);
        return(
            v!=null ? v : defaultValue
        );
    }
}
