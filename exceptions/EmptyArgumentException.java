package exceptions;

public final class EmptyArgumentException extends IllegalArgumentException
{
	public EmptyArgumentException()
	{
		super("Argument is empty");
	}

	public EmptyArgumentException(String name)
	{
		super(name == null ? "Argument" : name + " is empty");
	}
}