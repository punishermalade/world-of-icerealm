package ca.qc.icerealm.bukkit.plugins.scenarios.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.EntityType;

import ca.qc.icerealm.bukkit.plugins.common.EntityUtilities;
import ca.qc.icerealm.bukkit.plugins.common.RandomUtil;

public class EntityHelper extends EntityUtilities {
	/*
	public static EntityType getRandomEntity(String monsters) {
		return getRandomEntity(monsters.split(","));
	}
	
	public static EntityType getRandomEntity(String[] monsters) {
		List<EntityType> list = new ArrayList<EntityType>();
		for (String s : monsters) {
			list.add(EntityUtilities.getEntityType(s));
		}
		return getRandomEntity((EntityType[])list.toArray());
		
		
	}
	
	public static EntityType getRandomEntity(EntityType[] monsters) {
		return monsters[RandomUtil.getRandomInt(monsters.length)];
	}
	*/
	
	public static CreatureType getRandomEntity(String monsters) {
		return getRandomEntity(monsters.split(","));
	}
	
	public static CreatureType getRandomEntity(String[] monsters) {
		List<CreatureType> list = new ArrayList<CreatureType>();
		for (String s : monsters) {
			list.add(EntityUtilities.getCreatureType(s));
		}
		
		int index = RandomUtil.getRandomInt(list.size());
		return list.get(index);		
	}
	
	public static CreatureType getRandomEntity(CreatureType[] monsters) {
		return monsters[RandomUtil.getRandomInt(monsters.length)];
	}
}
