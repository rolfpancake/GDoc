package main;

abstract public class Debug
{
	/**
     * Imprime une ligne de longueur variable dans le flux de sortie.
     *
     * @param args Tout objet(s) Java
     */
    static public void trace(Object ... args)
    {
        String s = "";
        for (Object o : args) s += (s.isEmpty() ? "" : ", ") + (o == null ? "null" : o.toString());
        System.out.println(s);
    }
}