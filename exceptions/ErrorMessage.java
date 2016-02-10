package exceptions;


import java.nio.file.Path;
import com.sun.istack.internal.NotNull;
import main.Paths;
import main.Strings;


public final class ErrorMessage
{
	static private final String _JAVA_RE = "^(java|com\\.sun).*";
	static private final String _SRC_RE = "^src\\.|\\.java$";
	static private final String _AT = "at";
	static private final String _DEFAULT_MESSAGE = "Internal error";

	private String _message = _DEFAULT_MESSAGE;
	private ErrorMessageType _type;


	public ErrorMessage() { /* void */ }


	public ErrorMessage(String message)
	{
		if (message != null && !message.isEmpty()) _message = message;
	}


	public ErrorMessage(ErrorMessageType type, String target)
	{
		_type = type;
		_setMessage(type);
		_setTarget(target);
	}


	public ErrorMessage(ErrorMessageType type, Class targetClass, String target)
	{
		_type = type;
		_setMessage(type);
		_setTarget(targetClass, target);
	}


	public ErrorMessage(Throwable exception, boolean multiLine)
	{
		if (exception == null) return;
		_message = exception.getClass().getName() + Strings.COLON + Strings.SPACE + exception.getMessage();
		StackTraceElement[] s = exception.getStackTrace();
		String c;

		for (StackTraceElement i : s)
		{
			c = i.getClassName();
			if (c == null || c.matches(_JAVA_RE)) continue;
			if (multiLine) _message += Strings.NEW_LINE + Strings.SPACE;
			_message += _AT + Strings.SPACE + c.replaceAll(_SRC_RE, Strings.EMPTY) +
						Strings.PARENTHESISE(String.valueOf(i.getLineNumber()));
			if (!multiLine) return;
		}
	}


	public ErrorMessage(Path path)
	{
		_setMessage(null);
		_setPath(path);
	}


	public ErrorMessage(Path path, String target)
	{
		_setMessage(null);
		_setPath(path);
		_setTarget(target);
	}


	public ErrorMessage(ErrorMessageType type, Path path)
	{
		_type = type;
		_setMessage(type);
		_setPath(path);
	}


	public ErrorMessage(ErrorMessageType type, Path path, String target)
	{
		_type = type;
		_setMessage(type);
		_setPath(path);
		_setTarget(target);
	}


	@NotNull
	public String getMessage() { return _message; }


	public ErrorMessageType getType() { return _type; }


	private void _setMessage(ErrorMessageType t)
	{
		_message = t != null ? Strings.CAPITALIZE(Strings.SPLIT_CAMEL_CASE(t.toString())) : _DEFAULT_MESSAGE;
	}


	private void _setPath(Path p)
	{
		if (p == null) return;
		_message = _message == null ? Strings.EMPTY : _message + Strings.SPACE;
		_message += _AT + Strings.SPACE + Paths.USER_DIR.relativize(p).toString();
	}


	private void _setTarget(Class c, String t)
	{
		_message = _message == null ? _DEFAULT_MESSAGE : _message;
		_message += Strings.SPACE + Strings.PARENTHESISE((c != null ? c.getSimpleName() + Strings.COLON : Strings.EMPTY) + t);
	}


	private void _setTarget(String t)
	{
		_message = (_message == null ? _DEFAULT_MESSAGE : _message) + Strings.SPACE + Strings.PARENTHESISE(t);
	}
}