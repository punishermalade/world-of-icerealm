package ca.qc.icerealm.bukkit.plugins.scenarios.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.qc.icerealm.bukkit.plugins.common.EntityUtilities;
import ca.qc.icerealm.bukkit.plugins.common.RandomUtil;
import ca.qc.icerealm.bukkit.plugins.common.WorldZone;
import ca.qc.icerealm.bukkit.plugins.scenarios.core.ScenarioService;
import ca.qc.icerealm.bukkit.plugins.scenarios.frontier.Frontier;
import ca.qc.icerealm.bukkit.plugins.scenarios.mobcontrol.AgressivityMobControl;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.ArtilleryShelling;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.BlockRestore;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.Loot;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.LootGenerator;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.MonsterLeach;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.PinPoint;
import ca.qc.icerealm.bukkit.plugins.scenarios.tools.TimeFormatter;
import ca.qc.icerealm.bukkit.plugins.zone.ZoneObserver;
import ca.qc.icerealm.bukkit.plugins.zone.ZoneSubject;

public class BarbarianRaid extends BaseEvent implements Runnable, ZoneObserver {

	private Logger _logger = Logger.getLogger("Minecraft");
	private int MAX_WAVE = 5;
	private int MONSTER_PER_LOCATION = 1;
	protected long INTERVAL_BETWEEN_ATTACK = 7200; // 7200 sec = 2 heures
	private long INTERVAL_BETWEEN_WAVE = 10; // 10 secondes;
	private boolean USE_ARTILLERY = false;
	private int NB_ARTILLERY_SHOT = 5;
	private List<Location> _locations;
	private String[] _monsters = new String[] { "zombie", "spider", "cavespider", "pigzombie" };
	protected int _waveDone = 0;
	private World _world;
	private HashSet<Integer> _monstersContainer;
	private WorldZone _activationZone;
	protected List<Player> _players;
	protected boolean _activated;
	private boolean _started;
	private Loot _loot;
	private BlockRestore _blockRestore;
	protected long _timeForReactivation;
	private HashSet<Monster> _monstersEntity;
	private ZoneSubject _zoneServer;
	private String _welcomeMessage;
	private String _endMessage;
	private ScheduledExecutorService _executor;
		
	public BarbarianRaid() {
		_monstersContainer = new HashSet<Integer>();
		_monstersEntity = new HashSet<Monster>();
		_locations = new ArrayList<Location>();
		_players = new ArrayList<Player>();
		_activated = true;
		_started = false;
		_welcomeMessage = "This looks like a occupied place by bandits!";
		_endMessage = "This place looks like it is abandonned!";		
	}
		
	@Override
	public void run() {
			
		if (_activated) {
			for (Location loc : _locations) {

				Block b = _world.getBlockAt(loc);
				while (b.getType() != Material.AIR) {
					loc = new Location(_world, loc.getX(), loc.getY() + 1, loc.getZ());
					b = _world.getBlockAt(loc);
				}

				double playerBasedModifier = ((double)_waveDone / (double)MAX_WAVE);
				if (_players.size() > 1) {
					playerBasedModifier += ((_players.size() - 1) * 0.25); 
				}

				double modifier = Frontier.getInstance().calculateGlobalModifier(loc) + playerBasedModifier;
				
				for (int i = 0; i < MONSTER_PER_LOCATION; i++) {
					String monster = _monsters[RandomUtil.getRandomInt(_monsters.length)];
					Entity e = ScenarioService.getInstance().spawnCreature(_world, loc, EntityUtilities.getEntityType(monster), modifier, false);
					_monstersContainer.add(e.getEntityId());
					_monstersEntity.add((Monster)e);
					
					
					if (_players.size() > 0) {
						Collections.shuffle(_players);
						Monster m = (Monster)e;
						AgressivityMobControl.defineTarget(m, _players.get(0));
						//m.setTarget(_players.get(0));
					}
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void playerDies(PlayerDeathEvent event) {
		Player p = event.getEntity();
		boolean playerRemoved = _players.remove(p);
		
		if (playerRemoved && _players.size() == 0 && _started) {
			EventActivator activator = new EventActivator(this);
			activator.run();
			
			if (_blockRestore != null) {
				_blockRestore.run();
			}
			for (Monster m : _monstersEntity) { 
				m.remove();
			}
			_monstersContainer.clear();
			_started = false;
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void playerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		boolean playerRemoved = _players.remove(p);
		
		if (playerRemoved && _players.size() == 0 && _started) {
			processEndEvent();
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void monsterDies(EntityDeathEvent event) {
		processKill(event.getEntity().getEntityId());
	}

	@EventHandler (priority = EventPriority.NORMAL)
	public void monsterDies(EntityExplodeEvent event) {
		if (event.getEntity() != null) {
			processKill(event.getEntity().getEntityId());
		}
	}
	
	private void processKill(int id) {
		
		if (_monstersContainer.contains(id)) {
			_monstersContainer.remove(id);
			
			if (_monstersContainer.size() == 0) {
				_waveDone++;
				
				for (Player p : _players) {
					p.sendMessage(ChatColor.YELLOW + "This wave has been wiped out!");
				}
				
				if (_waveDone < MAX_WAVE) {
					if (USE_ARTILLERY) {
						_executor.schedule(new ArtilleryShelling(_activationZone, NB_ARTILLERY_SHOT), INTERVAL_BETWEEN_WAVE - 3, TimeUnit.SECONDS);	
					}
					
					_executor.schedule(this, INTERVAL_BETWEEN_WAVE, TimeUnit.SECONDS);
					for (Player p : _players) {
						p.sendMessage(ChatColor.RED + "Another wave is coming... " + ChatColor.GOLD + " They look stronger!");
					}
				}
				else {
					generateLoot();
					processEndEvent();
				}
			}
		}
		
		
	}
	
	protected void processEndEvent() {
		_activated = false;
		_started = false;
		_timeForReactivation = System.currentTimeMillis() + INTERVAL_BETWEEN_ATTACK * 1000;
		this.sendEventCompleted(_players, Frontier.getInstance().calculateGlobalModifier(_source));			
		Executors.newSingleThreadScheduledExecutor().schedule(new EventActivator(this), INTERVAL_BETWEEN_ATTACK, TimeUnit.SECONDS);
		Executors.newSingleThreadScheduledExecutor().schedule(_blockRestore, INTERVAL_BETWEEN_ATTACK, TimeUnit.SECONDS);
		_players.clear();
	}
	
	protected void welcomeMessage(Player arg0) {
		if (!_activated) {
			arg0.sendMessage(ChatColor.GREEN + _endMessage + ChatColor.AQUA + " Come back in " + ChatColor.GREEN + TimeFormatter.readableTime(_timeForReactivation - System.currentTimeMillis()));
		}
		else {
			arg0.sendMessage(ChatColor.GOLD + _welcomeMessage);	
		}
	}
	
	protected void generateLoot() {
		Location lootLocation = getRandomLocation(_lootPoints);
		double lootModifier = Frontier.getInstance().calculateGlobalModifier(lootLocation);
		_loot = LootGenerator.getFightingRandomLoot(lootModifier); 
		_loot.generateLoot(lootLocation);
		
		for (Player p : _players) {
			p.sendMessage(ChatColor.DARK_GREEN + "You survived this attack, " + ChatColor.GOLD + "take the loot" + ChatColor.YELLOW + " and get the fuck out!");
		}
	}
	
	protected Location getRandomLocation(List<PinPoint> pts) {
		if (pts.size() > 0) {
			Collections.shuffle(pts);
			return new Location(_world, _source.getX() + pts.get(0).X, _source.getY() + pts.get(0).Y, _source.getZ() + pts.get(0).Z);
		}
		return _source;
	}

	@Override
	public void playerEntered(Player arg0) {
		if (!_players.contains(arg0)) {
			_players.add(arg0);
			welcomeMessage(arg0);
		}
		if (_activated && !_started)  {
			_started = true;
			
			if (USE_ARTILLERY) {
				_executor.schedule(new ArtilleryShelling(_activationZone, NB_ARTILLERY_SHOT), INTERVAL_BETWEEN_WAVE - 5, TimeUnit.SECONDS);	
			}
			_executor.schedule(this, INTERVAL_BETWEEN_WAVE, TimeUnit.SECONDS);
		}
		
		
	}

	@Override
	public void playerLeft(Player arg0) {
		if (!_activated && !_started) {
			_players.remove(arg0);	
		}
		
	}
	
	@Override
	public void setWelcomeMessage(String s) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void setEndMessage(String s) {
		// TODO Auto-generated method stub
	}

	@Override
	public void activateEvent() {
		_executor = Executors.newSingleThreadScheduledExecutor();
		_world = this._source.getWorld();
		_zoneServer = this.getZoneSubjectInstance();
		
		for (int i = 0; i < _pinPoints.size(); i++) {
			Location l = new Location(_world, _source.getX() + _pinPoints.get(i).X, _source.getY() + _pinPoints.get(i).Y, _source.getZ() + _pinPoints.get(i).Z);
			_locations.add(l);
		}
		
		for (int i = 0; i < _zones.size(); i++) {
			List<PinPoint> zone = _zones.get(i);
			
			if (zone.size() == 2) {
				Location lower = new Location(_world, _source.getX() + zone.get(0).X, _source.getY() + zone.get(0).Y, _source.getZ() +zone.get(0).Z);
				Location higher = new Location(_world, _source.getX() + zone.get(1).X, _source.getY() + zone.get(1).Y, _source.getZ() +zone.get(1).Z);
				_activationZone = new WorldZone(lower, higher);
				_zoneServer.addListener(this);
				_blockRestore = BlockRestore.getBlockRestoreFromWorldZone(_activationZone);
			}
		}
		
		// appliquer la config
		applyConfiguration();
		_activated = true;
		
	}
	
	protected void applyConfiguration() {
		String[] config = getConfiguration().split(",");
		if (config.length > 6) {
			NB_ARTILLERY_SHOT = Integer.parseInt(config[0]);
			USE_ARTILLERY = Boolean.parseBoolean(config[1]);
			MAX_WAVE = Integer.parseInt(config[2]);
			MONSTER_PER_LOCATION = Integer.parseInt(config[3]);
			INTERVAL_BETWEEN_ATTACK = Long.parseLong(config[4]);
			INTERVAL_BETWEEN_WAVE = Long.parseLong(config[5]);
			_monsters = config[6].split(" ");
			
			if (config.length > 7) {
				_welcomeMessage = config[7];
			}
			
			if (config.length > 8) {
				_endMessage = config[8];
			}
		}
		else {
			_logger.info("Barbarian raid will use default settings");
		}
	}

	@Override
	public void releaseEvent() {
		_zoneServer.removeListener(this);
		if (_loot != null) {
			_loot.removeLoot();	
		}
		if (_blockRestore != null) {
			_blockRestore.run();	
		}
		
		_players.clear();
		_started = false;
		_activated = false;
		
		if (_executor != null) _executor.shutdownNow();
	}

	@Override
	public String getName() {
		return "barbarian";
	}

	@Override
	public Server getCurrentServer() {
		return this._server;
	}

	@Override
	public WorldZone getWorldZone() {
		return _activationZone;
	}


	@Override
	public void setWorldZone(WorldZone arg0) {
		_activationZone = arg0;
	}
	
	public void setActivate(boolean b) {
		if (_loot != null) {
			_loot.removeLoot();	
		}
		
		_waveDone = 0;
		_activated = b;
	}

	@Override
	protected long getCoolDownInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void resetEvent() {
		// TODO Auto-generated method stub
		
	}
}


class EventActivator implements Runnable {

	private Logger _logger = Logger.getLogger("Minecraft");
	private BarbarianRaid _raid;
	
	public EventActivator(BarbarianRaid r) {
		_raid = r;
	}
	
	@Override
	public void run() {
		_logger.info("setActivate event activator");
		_raid.setActivate(true);
	}
	
}