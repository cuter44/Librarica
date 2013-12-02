package com.github.cuter44.util.servlet;

public class MissingParameterException
    extends NullPointerException
{
    public MissingParameterException()
    {
        super();
    }

    public MissingParameterException(String paramName)
    {
        super("Missing parameter:" + paramName);
    }
}
