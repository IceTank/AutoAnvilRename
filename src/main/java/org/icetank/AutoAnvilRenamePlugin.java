package org.icetank;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * Example rusherhack plugin
 *
 * @author John200410
 */
public class AutoAnvilRenamePlugin extends Plugin {
	
	@Override
	public void onLoad() {
		
		//logger
		this.getLogger().info(this.getName() + " loaded!");
		
		//creating and registering a new module
		final AutoAnvilRenameModule autoAnvilRenameModule = new AutoAnvilRenameModule();
		RusherHackAPI.getModuleManager().registerFeature(autoAnvilRenameModule);
		
		//creating and registering a new command
		final AutoAnvilRenameCommand autoAnvilRenameCommand = new AutoAnvilRenameCommand();
		RusherHackAPI.getCommandManager().registerFeature(autoAnvilRenameCommand);
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info(this.getName() + " unloaded!");
	}
	
	@Override
	public String getName() {
		return "AutoAnvilRenamePlugin";
	}
	
	@Override
	public String getVersion() {
		return "v0.1";
	}
	
	@Override
	public String getDescription() {
		return "Auto Anvil Rename";
	}
	
	@Override
	public String[] getAuthors() {
		return new String[]{"IceTank (ign: Ic3Tank)"};
	}
}
