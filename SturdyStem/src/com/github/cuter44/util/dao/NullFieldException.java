package com.github.cuter44.util.dao;

/** ��ʾ����Ϊ�յ� exception
 * @version 1.0.0 builld 20131212
 */
public class NullFieldException
    extends RuntimeException
{
    public NullFieldException()
    {
        super();
    }

    public NullFieldException(String msg)
    {
        super(msg);
    }

    public NullFieldException(Throwable cause)
    {
        super(cause);
    }

    public NullFieldException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
