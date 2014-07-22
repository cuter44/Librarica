package cn.edu.scau.librarica.util.conf;

import java.util.Properties;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * 从 /tvprotal.properties 和 /tvprotal.public.properties 读取参数并供应给应用程序
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

    /** 加载配置文件
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

    /** 重载配置文件
     */
    public static void reload()
    {
        Singleton.instance.load();
    }

    /** 获得整个属性库的只读副本
     * 本质上是将原有 prop 作为默认表创建新表并返回.
     * 因此, 对得到的表作出的修改不会影响到原表.
     */
    public static Properties getProperties()
    {
        Properties prop = new Properties(Singleton.instance.prop);

        return(prop);
    }

    /** 获得公开属性库的只读副本
     * 本质上是将原有 prop 作为默认表创建新表并返回.
     * 因此, 对得到的表作出的修改不会影响到原表.
     * 主要用于将服务器端设置值告诉客户端.
     */
    public static Properties getPublicProperties()
    {
        Properties prop = new Properties(Singleton.instance.publicProp);

        return(prop);
    }


    /**
     * 提取配置文件中的参数
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
