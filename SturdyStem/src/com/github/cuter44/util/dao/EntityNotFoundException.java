package com.github.cuter44.util.dao;

/** ��ʾָ����ʵ�岻���ҵ��� exception
 * @version 1.0.0 builld 20131212
 */
public class EntityNotFoundException
    extends RuntimeException
{
    public EntityNotFoundException()
    {
        super();
    }

    public EntityNotFoundException(String msg)
    {
        super(msg);
    }

    public EntityNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public EntityNotFoundException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
