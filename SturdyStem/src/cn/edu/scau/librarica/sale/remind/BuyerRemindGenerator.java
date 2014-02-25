package cn.edu.scau.librarica.sale.remind;

import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.remind.dao.*;
import cn.edu.scau.librarica.remind.core.*;
import cn.edu.scau.librarica.sale.core.DealProcessor;

/** 产生需要通知买家的通知产生器
 * Bootstrapable
 */
public class BuyerRemindGenerator
    implements DealProcessor.StatusChangedListener
{
    static
    {
        DealProcessor.addListener(
            new BuyerRemindGenerator()
        );
    }

    @Override
    public void onStatusChanged(BuySession bs)
    {
        try
        {
            Byte status = bs.getStatus();
            if (
                BuySession.REJECTED.equals(status) ||
                BuySession.ACCEPTED.equals(status)
            )
            {
                Remind r = RemindMgr.createTransient(
                    bs.getBuyer(),
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

    private BuyerRemindGenerator()
    {
        return;
    }
}
