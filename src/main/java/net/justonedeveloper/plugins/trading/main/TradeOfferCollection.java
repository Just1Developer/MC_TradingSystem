package net.justonedeveloper.plugins.trading.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeOfferCollection {

	private final List<KeyValuePair<UUID, Long>> TradeOffers;
	
	public TradeOfferCollection()
	{
		TradeOffers = new ArrayList<>();
	}
	
	public List<KeyValuePair<UUID, Long>> asList()
	{
		return TradeOffers;
	}
	
	public List<UUID> getUUIDs()
	{
		List<UUID> list = new ArrayList<>();
		for(KeyValuePair<UUID, Long> pair : TradeOffers)
		{
			list.add(pair.Key);
		}
		return list;
	}
	
	public KeyValuePair<UUID, Long> get(UUID uuid)
	{
		for(KeyValuePair<UUID, Long> pair : TradeOffers)
		{
			if(pair.Key.equals(uuid)) return pair;
		}
		return null;
	}
	
	public KeyValuePair<UUID, Long> get(int index)
	{
		if(index >= size()) return null;
		return TradeOffers.get(index);
	}
	
	public boolean contains(UUID uuid)
	{
		return get(uuid) != null;
	}
	
	public long getTotalSystemTimeMillisOf(UUID uuid)
	{
		KeyValuePair<UUID, Long> result = get(uuid);
		if(result == null) return System.currentTimeMillis() - 1;
		return result.Value;
	}
	
	public void add(UUID uuid, long time)
	{
		TradeOffers.add(new KeyValuePair<>(uuid, time));
	}
	
	public void add(UUID uuid)
	{
		TradeOffers.add(new KeyValuePair<>(uuid, future()));
	}
	
	public void remove(UUID uuid)
	{
		TradeOffers.removeIf(pair -> pair.Key.equals(uuid));
	}
	
	public void remove(int index)
	{
		if(index < size())
			TradeOffers.remove(index);
	}
	
	public int size()
	{
		return TradeOffers.size();
	}
	
	public boolean isEmpty()
	{
		return TradeOffers.isEmpty();
	}
	
	private static long future()
	{
		return System.currentTimeMillis() + TradeCommand.TradeRequestTimeMS;
	}

}
