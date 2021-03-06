package ca.qc.icerealm.bukkit.plugins.perks.warrior;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.qc.icerealm.bukkit.plugins.perks.Cooldown;
import ca.qc.icerealm.bukkit.plugins.perks.PerkService;

public class LastManStandingPerk implements Listener {
	private PerkService perkService = PerkService.getInstance();
	private HashMap<String, Cooldown> cooldowns = new HashMap<String, Cooldown>();
	private static final long CooldownTime = 60000;
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShoot(EntityDamageEvent evt) {
		
		boolean canLastMan = false;
		
		if (evt.getEntity() instanceof Player && perkService.playerHasPerk((Player)evt.getEntity(), WarriorTree.LastManStandingId)) {
			Player player = (Player)evt.getEntity();
			
			if (player.getHealth() > 10) {
				return;
			}
			if (cooldowns.containsKey(player.getName())) {
				if (!cooldowns.get(player.getName()).isOnCooldown()) {
					Cooldown cd = new Cooldown(CooldownTime);
					cooldowns.put(player.getName(), cd);
					cd.start();
					
					canLastMan = true;
				}
			} else {
				Cooldown cd = new Cooldown(CooldownTime);
				cooldowns.put(player.getName(), cd);
				cd.start();
				
				canLastMan = true;
			}
			
			if (canLastMan) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*10, 2));
			}
		}
		
	}

}
