package cn.edu.scau.librarica.lend.remind;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.remind.dao.*;
import cn.edu.scau.librarica.remind.core.*;
import cn.edu.scau.librarica.lend.core.BorrowProcessor;

public class LenderRemindGenerator
    implements BorrowProcessor.StatusChangedListener
{
    static
    {
        BorrowProcessor.addListener(
            new LenderRemindGenerator()
        );
    }

    @Override
    public void onStatusChanged(BorrowSession bs)
    {
        try
        {
            Byte status = bs.getStatus();
            if (
                BorrowSession.ABORTED.equals(status) ||
                BorrowSession.REQUESTED.equals(status) ||
                BorrowSession.BORROWED.equals(status) ||
                BorrowSession.RETURNING.equals(status)
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

    private LenderRemindGenerator()
    {
        return;
    }
}
