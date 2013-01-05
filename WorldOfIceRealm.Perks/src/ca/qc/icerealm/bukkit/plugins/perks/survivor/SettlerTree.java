package ca.qc.icerealm.bukkit.plugins.perks.survivor;

import ca.qc.icerealm.bukkit.plugins.perks.Perk;
import ca.qc.icerealm.bukkit.plugins.perks.PerkTree;

public class SettlerTree extends PerkTree {

	public SettlerTree() {
		super("Settler");
		// TODO Auto-generated constructor stub
	}


	public static final String StoneWorkerId = "sw";
	public static final String WoodmanId = "wm";
	public static final String VassalId = "vas";
	public static final String GreenThumbId = "gt"; 

	public static final Perk StoneWorker = new Perk(	StoneWorkerId, 
														"Stone Worker", 
														"Mining stone gives double cobble stone.", 
														1, 
														null, 
														null);
	
	public static final Perk Vassal = new Perk(	VassalId, 
												"Vassal", 
												"Upon harvesting, give one more of harvested resource.", 
												3, 
												new String[] { StoneWorkerId }, 
												GreenThumbId);
	
	public static final Perk GreenThumb = new Perk(	GreenThumbId, 
													"Green Thumb", 
													"Planted crops have 20% chance to grow instantly.", 
													5, 
													new String[] { VassalId	}, 
													null);

	public static final Perk Woodman = new Perk(	WoodmanId, 
													"Woodman", 
													"Cutting a tree will have all attached blocks destroyed instantly. All loot rules apply.", 
													5, 
													new String[] { GreenThumbId	}, 
													null);

	public static final Perk[] Perks = new Perk[] { 
		StoneWorker, 
		Vassal, 
		GreenThumb, 
		Woodman
	};

	
	@Override
	public void initializePerks() {
		for(Perk perk : Perks) {
			getPerks().add(perk);
		}
	}

}
