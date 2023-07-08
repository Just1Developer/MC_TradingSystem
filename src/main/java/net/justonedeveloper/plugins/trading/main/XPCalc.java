package net.justonedeveloper.plugins.trading.main;

public class XPCalc {

	// Info: https://minecraft.fandom.com/wiki/Experience#Leveling_up
	
	public static KeyValuePair<Integer, Double> levelOf(double xp_points)
	{
		double lvl = 0, delta = 2, diff = 7;
		if(xp_points <= 352)
		{
			lvl += Math.sqrt(xp_points + 9) - 3;
			xp_points -= Math.pow(lvl, 2) + 6*lvl;
			return new KeyValuePair<>((int) lvl, xp_points);
		}
		
		// 1507 - 352 (lvl 17 - 31): 1155
		if(xp_points <= 1507)
		{
			lvl = 8.1 + Math.sqrt(0.4 * (xp_points - (195.975)));
			xp_points -= 2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360;
			return new KeyValuePair<>((int) lvl, xp_points);
		}
		
		// 1508+
		lvl += 18.0555 + Math.sqrt(0.2222222 * (xp_points - (752.986111)));
		xp_points -= 4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220;
		return new KeyValuePair<>((int) lvl, xp_points);
	}

}