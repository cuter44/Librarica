package cn.edu.scau.librarica.util.bootstrap;

import cn.edu.scau.librarica.util.conf.*;

public class Bootstraper
{
    private static String PREFIX = "librarica.bootstrap";

    static
    {
        int count = Configurator.getInt(PREFIX+".count");

        for (int i=1; i<=count; i++)
        {
            try
            {
                String name = Configurator.get(PREFIX+"."+i);
                if (name == null || name.equals(""))
                    continue;

                Class c = Class.forName(name);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    // Actually it does nothing...
    // just to trigger the static initialization block
    public static void start()
    {
    }
}
