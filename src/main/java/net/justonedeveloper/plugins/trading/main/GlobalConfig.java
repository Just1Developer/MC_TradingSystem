package net.justonedeveloper.plugins.trading.main;

import net.justonedeveloper.plugins.trading.language.Language;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GlobalConfig {
	
	static File f = new File(TradingMain.main.getDataFolder(), "config.yml");
	
	public static boolean EnableXPTrading;
	public static Material DefaultLanguageItem;
	
	public static void LoadConfig()
	{
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		if(!f.exists())
		{
			cfg.options().copyDefaults(true);
			cfg.addDefault("Default Language", "en-US");
			cfg.addDefault("Enable XP-Trading", true);
			cfg.addDefault("Default Language Item", Material.DARK_OAK_SIGN.toString());
			saveCfg(cfg);
		}
		
		Language.DefaultLanguage = getOrDefaultString("Default Language", cfg);
		EnableXPTrading = cfg.getBoolean("Enable XP-Trading");
		DefaultLanguageItem = getOrDefaultMaterial("Default Language Item", cfg);
	}
	
	private static String getOrDefaultString(String key, YamlConfiguration cfg)
	{
		String result = cfg.getString(key);
		if(result == null)
		{
			result = "en-US";
			cfg.set(key, "en-US");
			saveCfg(cfg);
		}
		return result;
	}
	
	private static Material getOrDefaultMaterial(String key, YamlConfiguration cfg)
	{
		String mat = cfg.getString(key);
		if(mat == null)
		{
			mat = Material.DARK_OAK_SIGN.toString();
			cfg.set(key, Material.DARK_OAK_SIGN);
			saveCfg(cfg);
		}
		return Material.valueOf(mat);
	}
	
	public static void saveCfg(YamlConfiguration cfg)
	{
		try {
			cfg.save(f);
		} catch(IOException ignored) { }
	}
	
}
