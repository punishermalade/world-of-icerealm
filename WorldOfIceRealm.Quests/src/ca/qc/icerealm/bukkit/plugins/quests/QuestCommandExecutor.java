package ca.qc.icerealm.bukkit.plugins.quests;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommandExecutor implements CommandExecutor {
	
	private final Quests quests;

	public QuestCommandExecutor(Quests quests) {
		this.quests = quests;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String param, String[] params) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			Quest quest = this.quests.getQuestService().getQuest(player);
			quest.start();
			
			return true;
		} else {
			return false;
		}
	}

}