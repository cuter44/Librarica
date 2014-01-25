package cn.edu.scau.librarica.util.bootstrap;

import cn.edu.scau.librarica.util.conf.*;
import org.apache.log4j.Logger;

public class Bootstraper
{
    private static Logger logger = Logger.getLogger(Bootstraper.class);

    private static String PREFIX = "librarica.bootstrap";

    static
    {
        int count = Configurator.getInt(PREFIX+".count");

        for (int i=1; i<=count; i++)
        {
            try
            {
                String name = Configurator.get(PREFIX+"."+i);

                logger.info("Bootstrap " + name);

                if (name == null || name.equals(""))
                    continue;

                Class c = Class.forName(name);

                logger.info("DONE");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error("Bootstrap FAILED", ex);
            }
        }
    }

    // Actually it does nothing...
    // just to trigger the static initialization block
    public static void start()
    {
    }
}
