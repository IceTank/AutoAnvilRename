package org.icetank;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.setting.StringSetting;

/**
 * Example rusherhack command
 *
 * @author John200410
 */
public class AutoAnvilRenameCommand extends Command {
	
	public AutoAnvilRenameCommand() {
		super("AutoAnvilRename", "Renames items in an anvil automatically");
	}
	
	/**
	 * base command that takes in no arguments
	 */
	@CommandExecutor
	private String autoAnvilRename() {
		//when return type is String you return the message you want to return to the user
		return "Hello World!";
	}
	
	@CommandExecutor(subCommand = "setname")
	@CommandExecutor.Argument("string") //must set argument names
	private String addToExampleList(String string) {
		RusherHackAPI.getModuleManager().getFeature("AutoAnvilRename").ifPresent(module -> {
			if (module.getSetting("RenameText") instanceof StringSetting setting) {
				setting.setValue(string);
			}
		});
		return "Added " + string;
	}
}
