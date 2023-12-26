package net.justonedeveloper.plugins.trading.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.swing.text.html.HTMLDocument;

public class XPCalc {

	// Info: https://minecraft.fandom.com/wiki/Experience#Leveling_up
	
	public static int[] levelOf(int xp_points)
	{
		if(xp_points >= 50000) return levelOfFormulas(xp_points);
		return levelOfManual(xp_points);
	}
	
	// This is buggy at low levels, I tried to compensate
	
	public static int[] levelOfFormulas(int xp_points)
	{
		// 0 - 352 (Levels 0 - 15):
 		if(xp_points <= 352)
		{
 			double lvl = Math.sqrt(xp_points + 9) - 3;
 			//xp_points -= Math.pow(lvl, 2) + 6*lvl;							// Bug: Some stuff is NumberAbove.05 or something, like 23 -> 24.07
			double balance = (0.1 * (16-lvl));
			double lvlDownPoints = lvl == 0 ? 0 : 2 * (lvl-1) + 7 - balance;				// Lvl - 1 since that gives us xp from lvl to lvl+1, but we want lvl to lvl-1, so naturally: lvl-1 -> lvl
			double lvlUpPoints = 2 * lvl + 7 - balance;				// Now we want to next lvl
			
			//Bukkit.broadcastMessage("§9XP-Points: " + xp_points + " | LVL: " + lvl + " (" + ((int) lvl) + ") | DownPts: " + lvlDownPoints + " (" + ((int) lvlDownPoints) + ") | UpPts: " + lvlUpPoints + " (" + ((int) lvlUpPoints) + ")");
			
			return new int[] { (int) lvl, (int) lvlDownPoints, (int) lvlUpPoints };
        }
		
		// 353 - 1507 (Levels 16 - 30):
		if(xp_points <= 1507)
		{
			double lvl = 8.1 + Math.sqrt(0.4 * (xp_points - (195.975)));
			//xp_points -= 2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360;
			double lvlDownPoints = 5 * (lvl-1) - 38;
			double lvlUpPoints = 5 * lvl - 38;				// Now we want to next lvl
			return new int[] { (int) lvl, (int) lvlDownPoints, (int) lvlUpPoints };
		}
		
		// 1508+ (Levels 31+)
		double lvl = 18.0555 + Math.sqrt(0.2222222 * (xp_points - (752.986111)));
		//xp_points -= 4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220;
		double lvlDownPoints = 9 * (lvl-1) - 158;
		double lvlUpPoints = 9 * lvl - 158;				// Now we want to next lvl
		return new int[] { (int) lvl, (int) lvlDownPoints, (int) lvlUpPoints };
	}
	
	// Only works for small levels, apparently
	public static int[] levelOfManual(int xp_points)
	{
		int level = 0, levelPoints = 5, delta = 2;
		while (xp_points >= levelPoints)
		{
			levelPoints += delta;	// Only add delta if loop continues (last add bug). Thats why levelPoints starts at 5 instead of 7
			level++;
			xp_points -= levelPoints;
			if(level == 16) delta = 5;
			else if(level == 31) delta = 9;
		}
		if(levelPoints == 5) levelPoints = 7;
		return new int[] { level, levelPoints, levelPoints + delta };
	}
	
	public static int[] levelOf(Player p) { return levelOf(pointsOf(p.getLevel(), p.getExp())[0]); }
	public static int[] newLevelOf(Player p, int tradedXP) { return levelOf(pointsOf(p.getLevel(), p.getExp())[0] - tradedXP); }
	
	public static int[] pointsOf(Player p) { return pointsOf(p.getLevel(), p.getExp()); }
	public static int[] pointsOf(int level, double lvlDeltaPoints)
	{
		int run = 0;
		int xp_points = 0, levelPoints = 7, delta = 2;
		while (level > 0)
		{
			level--;
			run++;
			xp_points += levelPoints;
			
			if(run == 16) delta = 5;
			else if(run == 31) delta = 9;
			levelPoints += delta;
		}
		// if(levelPoints == 5) levelPoints = 7;
		xp_points += (int) Math.round(levelPoints * lvlDeltaPoints);
		return new int[] { xp_points, levelPoints - delta, levelPoints };
	}
	
	/** GeoGebra Function for 1-16
	 *
	 * -0.0015703759 x² + 0.1393247677x => -0.0015703759 * Math.pow(xp_points, 2) + 0.1393247677 + xp_points
	 *
	 /* Geogebra math for 1-15:
	 if(xp_points <= 352)
	 {
	 double lvl = -0.0015703759 * Math.pow(xp_points, 2) + 0.1393247677 + xp_points;
	 xp_points -= Math.pow(lvl, 2) + 6*lvl;
	 return new KeyValuePair<>((int) lvl, (int) xp_points);
	 }
	 *
	 * Old Code:
	 * 		if(xp_points <= 352)
	 *                {
	 * 			double lvl = Math.sqrt(xp_points + 9) - 3;
	 * 			xp_points -= Math.pow(lvl, 2) + 6*lvl;
	 * 			return new KeyValuePair<>((int) lvl, xp_points);
	 *        }
	 *
	 * 		// 1507 - 352 (lvl 17 - 31): 1155
	 * 		if(xp_points <= 1507)
	 *        {
	 * 			double lvl = 8.1 + Math.sqrt(0.4 * (xp_points - (195.975)));
	 * 			xp_points -= 2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360;
	 * 			return new KeyValuePair<>((int) lvl, xp_points);
	 *        }
	 *
	 * 		// 1508+
	 * 		double lvl = 18.0555 + Math.sqrt(0.2222222 * (xp_points - (752.986111)));
	 * 		xp_points -= 4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220;
	 * 		return new KeyValuePair<>((int) lvl, xp_points);
	 *
	 *
	 * @param level
	 * @return
	 */
	
	public static int getLevelDiffFor(int level)
	{
		return 0;
	}

}