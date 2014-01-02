package cn.edu.scau.librarica.util.bootstrap;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import cn.edu.scau.librarica.util.bootstrap.Bootstraper;

/** This is to trigger the Bootstraper via JSP container.
 * don't know why load-on-startup not works, this is an alternative from http://stackoverflow.com/questions/3289737
 */
public class BootstrapJSPAdapter
    implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent ev)
    {
        // direct invoke
        Bootstraper.start();
        // reflect
        //try
        //{
            //Class c = Class.forName("cn.edu.scau.librarica.util.bootstrap.Bootstraper");
        //}
        //catch (ClassNotFoundException ex);
        //{
            //ex.printStackTrace();
        //}

        return;
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev)
    {
        return;
    }
}
