package data;


import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Un retour de méthode. Return est la seule occurrence du package ayant un nom nul.
 */
public final class Return extends Described
{
	private WeakReference<Method> _method;
	private WeakReference<Type> _type;


	Return(@NotNull Method method, @NotNull Type type)
	{
		super();
		_method = new WeakReference<Method>(method);
		_type = new WeakReference<Type>(type);
	}


	/**
	 * Récupére la méthode associée.
	 *
	 * @return Un objet Method ou null
	 */
	@Nullable
	public Method getMethod() { return _method.get(); }


	/**
	 * Récupére le type de retour. Il différe du type auquel appartient la méthode.
	 *
	 * @return Un objet Type ou null
	 */
	@Nullable
	public Type getType() { return _type.get(); }


	@Override
	public boolean nameIsValid() { return super.getName() == null; }
}