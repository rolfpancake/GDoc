package data;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import exceptions.EmptyArgumentException;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import main.Launcher;


public final class Category extends Named
{
	static private final String _PATTERN = "^[_@a-zA-Z]+[ _\\-\\w]*$";

	static public final Category CORE = new Category("Core");
	static public final Category BUILT_IN = new Category("Built-In Types");

	private ArrayList<Type> _types;
	private boolean _valid;


	Category(@NotNull String name)
	{
		super.setName(name);
		if (name.isEmpty()) throw new EmptyArgumentException();
		if (name.equals("Core")) _types = new ArrayList<Type>(324);
		else if (name.equals("Built-In Types")) _types = new ArrayList<Type>(36);
		else _types = new ArrayList<Type>(1);
		_valid = name.matches(_PATTERN);
		if (!_valid) Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.invalidName, super.getClass(), name));
	}


	public short getSize() { return (short) _types.size(); }


	@NotNull
	public Type[] getTypes() { return _types.toArray(new Type[_types.size()]); }


	@Override
	public boolean nameIsValid() { return _valid; }


	void addType(@Nullable Type ...type)
	{
		if (type != null) for (Type i : type) if (!_types.contains(i)) _types.add(i);
	}
}