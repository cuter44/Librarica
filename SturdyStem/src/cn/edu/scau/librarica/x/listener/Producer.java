package cn.edu.scau.librarica.x.listener;

public class Producer
{
    public static interface Listener
    {
        public abstract void callback();
    }

    private static Listener listener;

    public static void addListener(Listener l)
    {
        listener = l;
    }

    public static void main(String[] args)
    {
        try
        {
            Class c = Class.forName("cn.edu.scau.librarica.x.listener.Consumer");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        listener.callback();
    }
}
