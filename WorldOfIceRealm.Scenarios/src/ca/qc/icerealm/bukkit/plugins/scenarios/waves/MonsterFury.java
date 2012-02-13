package ca.qc.icerealm.bukkit.plugins.scenarios.waves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ca.qc.icerealm.bukkit.plugins.common.EntityUtilities;
import ca.qc.icerealm.bukkit.plugins.common.RandomUtil;
import ca.qc.icerealm.bukkit.plugins.common.WorldZone;
import ca.qc.icerealm.bukkit.plugins.scenarios.core.Scenario;
import ca.qc.icerealm.bukkit.plugins.time.TimeObserver;
import ca.qc.icerealm.bukkit.plugins.time.TimeServer;
import ca.qc.icerealm.bukkit.plugins.zone.ZoneObserver;
import ca.qc.icerealm.bukkit.plugins.zone.ZoneServer;

public class MonsterFury implements ZoneObserver {
	public final Logger logger = Logger.getLogger(("Minecraft"));
	
	/*
	private int _minimumPlayerCount = 1;
	private boolean _active = false;
	private MonsterFuryListener _listener;
	private List<MonsterWave> _waves;
	private int nbWaveDone = 0;
	private long _coolDown = 30000;
	private long _lastRun = 0;
	private int _nbWave = 3;
	private int _nbMonsters = 3;
	private int _experience = 0;
	private int _money;
	private double _damageModifier = 0.0;
	private double _armorIncrement = 0.0;
	private long timeBetweenWave = 15000;
	private String _greater; 
	private WorldZone _greaterZone;
	private WorldZone _activationZone;
	*/
	
	private JavaPlugin _plugin;
	private World _world;
	private WorldZone _scenarioZone;
	private Server _server;
	private HashSet<Player> _players;
	private boolean _coolDownActive;
	private boolean _isActive;
	private long _coolDownTime;
	private CoolDownTimer _coolDownTimer;
	private ActivationZoneObserver _activationZoneObserver;
	private int _minimumPlayer;
	private WorldZone _activationZone;
	
	
	public MonsterFury(JavaPlugin plugin) {
		// parametre du scenario
		_plugin = plugin;
		_server = plugin.getServer();
		_world = _plugin.getServer().getWorld("world");
		_players = new HashSet<Player>();
		_isActive = false;
		_coolDownActive = false;
		_coolDownTime = 10000;
		_minimumPlayer = 1;
		_activationZone = new WorldZone(_world, "216,-740,219,-737,0,128");
		_scenarioZone = new WorldZone(_world, "199,-751,235,-725,0,128");
		
		// set la zone d'activation et enregistre le zone observer
		_activationZoneObserver = new ActivationZoneObserver(plugin.getServer(), _players, _minimumPlayer, this);
		_activationZoneObserver.setWorldZone(_activationZone);
		ZoneServer.getInstance().addListener(_activationZoneObserver);
	}
	
	public void triggerScenario() {
		// d�marrage du cooldown
		_coolDownActive = true;
		_coolDownTimer = new CoolDownTimer(this);
		TimeServer.getInstance().addListener(_coolDownTimer, _coolDownTime);
		
		// on update la zone du scenario en enlevant la zone d'activation 
		ZoneServer.getInstance().removeListener(_activationZoneObserver);
		ZoneServer.getInstance().addListener(this);
		this.logger.info("Scenario has been trigged");
		
		// on d�marre le sc�nario
		_isActive = true;
	}
	
	public void terminateScenario() {
		this.logger.info("Monster fury terminate");
		this.logger.info("Giving reward to players");
		
		_isActive = false;
		
		// on remet la zone d'activation
		resetActivationZone();
	}
	
	public void abortScenario() {
		this.logger.info("Monster fury aborted");
		
		_isActive = false;
		
		// on remet la zone d'activation
		resetActivationZone();
	}
	
	public void resetActivationZone() {
		ZoneServer.getInstance().removeListener(this);
		ZoneServer.getInstance().addListener(_activationZoneObserver);
	}
	
	public boolean isCoolDownActive() {
		return _coolDownActive;
	}
	
	public void setCoolDownActive(boolean active) {
		this.logger.info("setCoolDownAcitve to " + active);
		_coolDownActive = active;

		// on doit enlever le listener et en cr�er un nouveau
		TimeServer.getInstance().removeListener(_coolDownTimer);
		
		if (_coolDownActive) {
			_coolDownTimer = new CoolDownTimer(this);
			TimeServer.getInstance().addListener(_coolDownTimer, _coolDownTime);
		}
		else if (!_coolDownActive && !_isActive) {
			ZoneServer.getInstance().addListener(_activationZoneObserver);
		}
	}
	
	public void setPlayers(HashSet<Player> l) {
		_players = l;
	}
		
	@Override
	public void playerEntered(Player p) {
		// TODO Auto-generated method stub
		if (_isActive && !_players.contains(p)) {
			_players.add(p);
			this.logger.info(p.getName() + " has been added to the scenario");
		}
	}

	@Override
	public void playerLeft(Player p) {
		// TODO Auto-generated method stub
		if (_isActive && _players.contains(p)) {
			_players.remove(p);
			
			if (_players.size() == 0) {
				abortScenario();
			}
		}
	}

	@Override
	public Server getCurrentServer() {
		return _server;
	}
	
	@Override
	public void setWorldZone(WorldZone z) {
		_scenarioZone = z;
	}

	@Override
	public WorldZone getWorldZone() {
		return _scenarioZone;
	}
	
	
	/*
	public MonsterFury(int minPlayer, long coolDown, double protectRadius, int wave, int monster, int exp, int money, double armor, String greater) {
		_minimumPlayerCount = minPlayer;
		_coolDown = coolDown;

		_nbWave = wave;
		_nbMonsters = monster;
		_experience = exp;
		_money = money;
		_armorIncrement = armor;
		_greater = greater;
	}
	
	@Override
	public void terminateInit() {
		_greaterZone = new WorldZone(getWorld(), _greater);
		_activationZone = getZone();
	}
	
	@Override
	public boolean isTriggered() {
		return _active;
	}

	@Override
	public void triggerScenario() {
		
		setZone(_greaterZone);
		
		if (_waves != null) {
			_waves.clear();
		}
		else {
			_waves = new ArrayList<MonsterWave>();
		}
					
		// creation des listeners
		if (_listener == null) {
			_listener = new MonsterFuryListener(this);
			getServer().getPluginManager().registerEvents(_listener, getPlugin());
		}
		
		// creation des waves
		for (int i = 0; i < _nbWave; i++) {
			MonsterWave wave = new MonsterWave(_nbMonsters, _damageModifier, this, _activationZone);
			_damageModifier += _armorIncrement;
			_waves.add(wave);
		}
		
		getServer().broadcastMessage(ChatColor.RED + "The ennemy is approching!");
		getServer().broadcastMessage(ChatColor.GREEN + "Get ready to push them back!");
		getServer().broadcastMessage(ChatColor.YELLOW + String.valueOf(_nbWave) + ChatColor.GREEN + " waves have been spotted!");

		// on active la premiere wave et le sc�nario
		/*
		getServer().broadcastMessage(ChatColor.GREEN + "First wave is coming!!!");
		_listener.setMonsterWave(_waves.get(nbWaveDone));
		
		TimeServer.getInstance().addListener(this, 10000);
		
		_active = true;
	}

	@Override
	public void abortScenario() {
		// enleve les joueurs du sc�nario, enleve les waves
		if (_active) {
			getServer().broadcastMessage("You retreated into safety");
			getPlayers().clear();
			
			if (_waves != null) {
				for (MonsterWave wave : _waves) {
					wave.removeMonsters();
				}
				_waves.clear();
			}
		}
		
		setZone(_activationZone);
		_active = false;
	}

	@Override
	public boolean abortWhenLeaving() {
		// si un joueur se pousse, il est exclu, il perd tt sa progression
		return true;
	}

	@Override
	public boolean canBeTriggered() {
		boolean coolDown = (_lastRun + _coolDown) <= System.currentTimeMillis();
		return coolDown && getPlayers().size() >= _minimumPlayerCount;
	}

	@Override
	public boolean mustBeStop() {
		return (getPlayers().size() == 0);
	}

	@Override
	public void terminateScenario() {
		// donne le XP, du health au joueur
		getServer().broadcastMessage(ChatColor.GOLD +"The ennemy has been defeated!");
		int xp = _experience / getPlayers().size();
		for (Player p : getPlayers()) {
			p.setLevel(p.getLevel() + xp);
			p.sendMessage(ChatColor.GREEN + "You received " + ChatColor.GOLD + String.valueOf(xp) + " level of XP!");
		}
		
		_lastRun = System.currentTimeMillis();
		nbWaveDone = 0;
		setZone(_activationZone);
		_active = false;
	}
	*/


}
/*
	
	public void waveIsDone() {
		// on load la prochaine wave!
		nbWaveDone++;
				
		if (nbWaveDone < _waves.size()) {	
			getServer().broadcastMessage(ChatColor.GOLD + "This wave has been pushed back!");
			TimeServer.getInstance().addListener(this, timeBetweenWave);			
		}
		else {
			terminateScenario();
		}
	}

	@Override
	public void setAlaram(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWorldZone(WorldZone z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WorldZone getWorldZone() {
		// TODO Auto-generated method stub
		return null;
	}
*/

/*
class MonsterFuryListener implements Listener {
	public final Logger logger = Logger.getLogger(("Minecraft"));
	private MonsterWave _currentWave;
	private MonsterFury _scenario;
	
	public MonsterFuryListener(MonsterFury s) {
		_scenario = s;
	}
	
	public void setMonsterWave(MonsterWave wave) {
		_currentWave = wave;
		_currentWave.spawnWave();
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			try {
				_scenario.getPlayers().remove((Player)entity);	
			}
			catch (Exception ex) { }
		}
		else {
			_currentWave.processEntityDeath(entity);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		try {
			_currentWave.processDamage(event);	
		}
		catch (Exception ex) {
			this.logger.info("MonsterFury threw an exception on EntityDamageEvent");
		}
		
	}
	
}
*/
/*
class MonsterWave {
	public final Logger logger = Logger.getLogger(("Minecraft"));
	private int _nbMonsters = 0;
	private double _armorModifier = 0.0;
	private Set<Entity> _monstersTable;
	private MonsterFury _scenario;
	private WorldZone _exclude;
	private String[] possibleMonsters = new String[] { "zombie", "skeleton", "spider" };
	
	public MonsterWave(int qty, double armorModifier, MonsterFury s, WorldZone exclude) {
		_scenario = s;
		_monstersTable = new HashSet<Entity>();
		_nbMonsters = qty;
		_armorModifier = armorModifier;
		_exclude = exclude;
	}
	
	public void spawnWave() {
		for (int i = 0; i < _nbMonsters; i++) {
			// creation de la location et du monstre
			Location loc = _scenario.getZone().getRandomLocationOutsideThisZone(_scenario.getWorld(), _exclude);
			CreatureType type = EntityUtilities.getCreatureType(possibleMonsters[RandomUtil.getRandomInt(possibleMonsters.length)]);			
			LivingEntity living = _scenario.getWorld().spawnCreature(loc, type);
			// adding to the table
			_monstersTable.add(living);
		}
		
	}
	
	public void processEntityDeath(Entity e) {
		if (_monstersTable != null && _monstersTable.contains(e)) {
			_monstersTable.remove(e);
			
			if (_monstersTable.size() == 0) {
				_scenario.waveIsDone();
			}
			else {
				_scenario.getServer().broadcastMessage(_monstersTable.size() + " monsters left!");
			}
		}
	}
	
	public void processDamage(EntityDamageEvent e) {
		if (_monstersTable != null && _monstersTable.contains(e.getEntity())) {
			
			if (e.getCause() == DamageCause.FIRE_TICK) {
				e.setCancelled(true);
			}
						
		}
	}
	
	public void removeMonsters() {
		if (_monstersTable != null && _monstersTable.size() > 0) {
			 for (Entity l : _monstersTable) {
				 l.remove();
			 }
		}
	}
	
	public List<Entity> getFirstMonster(int i) {
		List<Entity> list = new ArrayList<Entity>();
		for (Entity l : _monstersTable) {
			list.add(l);
		}
		return list;
	}
	

}*/