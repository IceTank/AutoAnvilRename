package org.icetank;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.client.api.feature.command.arg.ItemReference;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.setting.StringSetting;

public class AutoAnvilRenameCommand extends Command {
	
	public AutoAnvilRenameCommand() {
		super("AutoAnvilRename", "Renames items in an anvil automatically");
	}
	
	@CommandExecutor(subCommand = "setname")
	@CommandExecutor.Argument("string") //must set argument names
	private String setName(String string) {
		RusherHackAPI.getModuleManager().getFeature("AutoAnvilRename").ifPresent(module -> {
			if (module.getSetting("RenameText") instanceof StringSetting setting) {
				setting.setValue(string);
			}
		});
		return "Added " + string;
	}

	@CommandExecutor(subCommand = "selectId")
	@CommandExecutor.Argument("Item") //must set argument names
	private String select(ItemReference item) {
		String name = AutoAnvilRenameModule.getItemId(item.items()[0]);
		RusherHackAPI.getModuleManager().getFeature("AutoAnvilRename").ifPresent(module -> {
			if (module.getSetting("ItemId") instanceof StringSetting setting) {
				setting.setValue(name);
			}
		});
		return "Selected item " + name;
	}
}
