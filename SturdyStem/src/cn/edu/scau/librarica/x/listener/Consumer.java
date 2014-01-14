package cn.edu.scau.librarica.x.listener;

public class Consumer
{
    static
    {
        Producer.addListener(
            new Producer.Listener()
            {
                @Override
                public void callback()
                {
                    Consumer.print();
                }
            }
        );
    }

    public static void print()
    {
        System.out.println("Helllo");
    }
}
