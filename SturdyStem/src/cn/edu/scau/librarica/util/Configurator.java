package cn.edu.scau.librarica.util;

import java.util.Properties;

import java.io.InputStreamReader;

/**
 * 从 /tvprotal.properties 读取参数并供应给应用程序
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

    /** 加载配置文件
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

    /** 重载配置文件
     */
    public static void reload()
    {
        Singleton.instance.load();
    }

    /** 获得整个 Properties 的引用
     * 事实上是将内置的 Properties 作为后备返回.
     * 因此这个方法是线程安全的, 也可以随便向其中添加新的属性而不影响读入的值
     */
    public static Properties getProperties()
    {
        Properties prop = new Properties(Singleton.instance.prop);

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
