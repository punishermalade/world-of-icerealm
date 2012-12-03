package ca.qc.icerealm.bukkit.plugins.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class LightningReflexesPerk implements Listener {
	private final PerkService perkService = PerkService.getInstance();
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerHit(EntityDamageEvent evt) {
		if (evt.getEntity() instanceof Player) {
			if (evt.getCause().equals(DamageCause.ENTITY_ATTACK) || evt.getCause().equals(DamageCause.PROJECTILE)) {
				Player player = (Player)evt.getEntity();
				
				if (perkService.playerHasPerk(player, AdventurerPerks.LightningReflexesId)) {
						evt.setCancelled(true);
				}
			}
		}
	}
}
