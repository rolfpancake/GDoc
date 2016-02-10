package main;

public final class Pair<K, V>
{
	private K _key;
	private V _value;


	public Pair() { /* void */ }


	public Pair(K key, V value)
	{
		_key = key;
		_value = value;
	}


	public K getKey() { return _key; }


	public V getValue() { return _value; }


	public void setValue(V value) { _value = value; }
}