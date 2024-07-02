package net.justonedeveloper.plugins.trading.main;

import java.util.Objects;

public class KeyValuePair<TK, TV> {
	
	public TK Key;
	public TV Value;
	
	public KeyValuePair(TK Key, TV Value)
	{
		this.Key = Key;
		this.Value = Value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KeyValuePair<?, ?> that = (KeyValuePair<?, ?>) o;
		return Objects.equals(Key, that.Key) && Objects.equals(Value, that.Value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(Key, Value);
	}
}
