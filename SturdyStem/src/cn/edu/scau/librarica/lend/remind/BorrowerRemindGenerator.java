package cn.edu.scau.librarica.lend.remind;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.remind.dao.*;
import cn.edu.scau.librarica.remind.core.*;
import cn.edu.scau.librarica.lend.core.BorrowProcessor;

public class BorrowerRemindGenerator
    implements BorrowProcessor.StatusChangedListener
{
    static
    {
        BorrowProcessor.addListener(
            new BorrowerRemindGenerator()
        );
    }

    @Override
    public void onStatusChanged(BorrowSession bs)
    {
        try
        {
            Byte status = bs.getStatus();
            if (
                BorrowSession.REJECTED.equals(status) ||
                BorrowSession.ACCEPTED.equals(status) ||
                BorrowSession.CLOSED.equals(status)
            )
            {
                Remind r = RemindMgr.createTransient(
                    bs.getBorrower(),
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

    private BorrowerRemindGenerator()
    {
        return;
    }
}
