package ca.qc.icerealm.bukkit.plugins.advancedcompass;

import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedCompass extends JavaPlugin
{
	public static final int PLAYER_MODE_INTERVAL = 1000;
	
	@Override
	public void onDisable() {
				
	}

	@Override
	public void onEnable() {
		
		// Version 0.3.0
		getServer().getPluginManager().registerEvents(new CompassActionListener(), this);
		
		getCommand("c").setExecutor(new CompassCommandExecutor());
	}
}