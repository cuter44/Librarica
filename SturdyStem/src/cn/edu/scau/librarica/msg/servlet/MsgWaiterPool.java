package cn.edu.scau.librarica.msg.servlet;

import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

import cn.edu.scau.librarica.msg.dao.*;

/**
 * support singleton
 */
public class MsgWaiterPool extends HashMap<Long, AsyncContext>
{
  // CONSTRUCT
    public MsgWaiterPool()
    {
        super();

        return;
    }

    private static class Singleton
    {
        public static final MsgWaiterPool instance = new MsgWaiterPool();
    }

    public static MsgWaiterPool getInstance()
    {
        return(
            Singleton.instance
        );
    }

  // CTX_EXPIRER
    private static final AsyncListener WIPER = new AsyncListener()
    {
        @Override
        public void onComplete(AsyncEvent ev)
        {
            return;
        }

        @Override
        public void onError(AsyncEvent ev)
        {
            return;
        }

        @Override
        public void onStartAsync(AsyncEvent ev)
        {
            return;
        }

        @Override
        public void onTimeout(AsyncEvent ev)
        {
            try
            {
                ServletResponse resp = ev.getSuppliedResponse();
                resp.setContentType("application/json, charset=utf-8");
                resp.getWriter().println("[]");

                ev.getAsyncContext().complete();

                return;
            }
            catch (IOException ex)
            {
                throw(new RuntimeException(ex));
            }
        }

    };

  // MAP
    /**
     * short for this.put(id, new MsgWaiter(ctx))
     */
    @Override
    public AsyncContext put(Long id, AsyncContext ctx)
    {
        ctx.addListener(WIPER);

        return(
            super.put(id, ctx)
        );
    }

}
