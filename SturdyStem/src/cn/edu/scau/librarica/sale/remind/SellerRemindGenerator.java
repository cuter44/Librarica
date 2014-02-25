package cn.edu.scau.librarica.sale.remind;

import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.remind.dao.*;
import cn.edu.scau.librarica.remind.core.*;

import cn.edu.scau.librarica.sale.core.DealProcessor;

public class SellerRemindGenerator
    implements DealProcessor.StatusChangedListener
{
    static
    {
        DealProcessor.addListener(
            new SellerRemindGenerator()
        );
    }

    @Override
    public void onStatusChanged(BuySession bs)
    {
        try
        {
            Byte status = bs.getStatus();
            if (
                BuySession.ABORTED.equals(status) ||
                BuySession.REQUESTED.equals(status) ||
                BuySession.CLOSED.equals(status)
            )
            {
                Remind r = RemindMgr.createTransient(
                    bs.getBook().getOwner(),
                    bs.getClass().getSimpleName(),
                    bs.getId()
                );
                RemindRouter.put(r);
            }
        }
        catch (NullPointerException ex)
        {
            ex.printStackTrace();
        }

        return;
    }

    private SellerRemindGenerator()
    {
        return;
    }
}
