package ca.qc.icerealm.bukkit.plugins.scenarios.mobcontrol;

import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityPlayer;

import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;

import ca.qc.icerealm.bukkit.plugins.scenarios.tools.EntityReflection;

public class AgressivityMobControl {

	public AgressivityMobControl() {
	}
	
	public static void defineTarget(LivingEntity entity, LivingEntity target) {
		if (entity != null && target != null) {

			EntityCreature aggro = EntityReflection.getEntityCreature(entity);
			try {
				EntityCreature creatureTarget = EntityReflection.getEntityCreature(target);
				aggro.b(creatureTarget);
			}
			catch (ClassCastException ex) {
				// le target n'est pas un EntityCreature!
				CraftPlayer player = (CraftPlayer)target;
				EntityPlayer finalTarget = player.getHandle();
				aggro.b(finalTarget);
			}
		}
	}
}
