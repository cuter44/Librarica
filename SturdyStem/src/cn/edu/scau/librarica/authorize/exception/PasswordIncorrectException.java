package cn.edu.scau.librarica.authorize.exception;

public class PasswordIncorrectException extends RuntimeException
{
    public PasswordIncorrectException()
    {
        super();
    }

    public PasswordIncorrectException(String message)
    {
        super(message);
    }

    public PasswordIncorrectException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PasswordIncorrectException(Throwable cause)
    {
        super(cause);
    }
}
