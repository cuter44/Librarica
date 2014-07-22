package cn.edu.scau.librarica.authorize.exception;

public class UnauthorizedException extends RuntimeException
{
    public UnauthorizedException()
    {
        super();
    }

    public UnauthorizedException(String message)
    {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause)
    {
        super(cause);
    }
}
