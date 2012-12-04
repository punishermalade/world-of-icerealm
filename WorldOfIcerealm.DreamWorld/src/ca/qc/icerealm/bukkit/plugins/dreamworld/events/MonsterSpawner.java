package ca.qc.icerealm.bukkit.plugins.dreamworld.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import ca.qc.icerealm.bukkit.plugins.common.EntityUtilities;
import ca.qc.icerealm.bukkit.plugins.common.RandomUtil;
import ca.qc.icerealm.bukkit.plugins.scenarios.core.ScenarioService;

public class MonsterSpawner implements Runnable {

	private Location _location;
	private World _world;
	private String _name;
	private String[] _monsters = new String[] { "skeleton", "zombie", "spider", "cavespider", "pigzombie" };
	private boolean _done = false;
	private List<LivingEntity> _entity;
	private long _coolDownInHours = 12;
	
	public MonsterSpawner(Location loc, String name, List<LivingEntity> entity) {
		_location = loc;
		_world = loc.getWorld();
		_name = name;
		_entity = entity;		
	}
	
	@Override
	public void run() {
		if (!_done) {
			_done = true;
			double modifier = ScenarioService.getInstance().calculateHealthModifierWithFrontier(_location, _world.getSpawnLocation());
			EntityType creature = EntityUtilities.getEntityType(_monsters[RandomUtil.getRandomInt(_monsters.length)]);
			LivingEntity entity = (LivingEntity)ScenarioService.getInstance().spawnCreature(_world, _location, creature, modifier, false);
			_entity.add(entity);
			
			Executors.newSingleThreadScheduledExecutor().schedule(new SpawnerActivator(this), _coolDownInHours, TimeUnit.HOURS);
		}
	}
	
	public void setActivate(boolean b) {
		_done = b;
	}
	
	public String getName() {
		return _name;
	}
	
}

class SpawnerActivator implements Runnable {

	private MonsterSpawner _spawner;
	
	public SpawnerActivator(MonsterSpawner spawner) {
		_spawner = spawner;
	}
	
	@Override
	public void run() {
		_spawner.setActivate(false);
	}
	
}
