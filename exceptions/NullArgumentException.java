package exceptions;

public final class NullArgumentException extends IllegalArgumentException
{
	public NullArgumentException()
	{
		super("Argument is null");
	}

	public NullArgumentException(String name)
	{
		super(name == null ? "Argument" : name + " is null");
	}
}