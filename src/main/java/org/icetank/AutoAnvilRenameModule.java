package org.icetank;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.render.EventRender2D;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.font.IFontRenderer;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.client.api.utils.InventoryUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

import java.awt.*;

/**
 * Example rusherhack module
 *
 * @author John200410
 */
public class AutoAnvilRenameModule extends ToggleableModule {
	
	/**
	 * Settings
	 */
	private final StringSetting renameText = new StringSetting("RenameText", "Sponsored by RusherHack Plugins");
	private final BooleanSetting onlyShulkers = new BooleanSetting("OnlyShulkers", true);
	private final NumberSetting<Integer> clickDelay = new NumberSetting<>("Click Delay", 1, 3, 10);
	private int delay = 0;

	/**
	 * Constructor
	 */
	public AutoAnvilRenameModule() {
		super("AutoAnvilRename", "Renames items in an anvil automatically", ModuleCategory.CLIENT);
		
		//register settings
		this.registerSettings(
				this.renameText,
				this.onlyShulkers,
				this.clickDelay
		);
	}
	
	/**
	 * 2d renderer demo
	 */
	@Subscribe
	private void onRender2D(EventRender2D event) {
		//renderers
		final IRenderer2D renderer = RusherHackAPI.getRenderer2D();
		final IFontRenderer fontRenderer = RusherHackAPI.fonts().getFontRenderer();
		
		//must begin renderer first
		renderer.begin(event.getMatrixStack(), fontRenderer);
		
		//draw stuff
		fontRenderer.drawString("Current Text:", 100, 100, Color.WHITE.getRGB());
		fontRenderer.drawString(this.renameText.getValue(), 100, 110, Color.WHITE.getRGB());
		
		//end renderer
		renderer.end();
	}
	
	/**
	 * Rotation demo
	 */
	@Subscribe
	private void onUpdate(EventUpdate event) {
		if (delay > clickDelay.getValue()) {
			delay = 0;
		}
		if (delay == 1) tick();
		delay++;
	}
	
	@Override
	public void onEnable() {

	}
	
	@Override
	public void onDisable() {

	}

	void tick() {
		if (mc.player == null || mc.level == null || mc.gameMode == null) return;
		if (mc.screen == null || !(mc.player.containerMenu instanceof AnvilMenu containerMenu)) return;

		ItemStack itemStackOutput = containerMenu.getSlot(2).getItem();
		ItemStack itemStackInput1 = containerMenu.getSlot(0).getItem();
		ItemStack itemStackInput2 = containerMenu.getSlot(1).getItem();

		String outputItemName = removeBrackets(itemStackOutput.getDisplayName().getString());

		int playerLevels = mc.player.experienceLevel;
		// Click the item in the anvil output slot if the item has the right name
		if (!itemStackOutput.isEmpty()
				/* && guess this does not work ¯\_(ツ)_/¯ containerMenu.mayPickup(mc.player, true) && */
				&& (playerLevels > 0 || mc.player.isCreative())
				&& outputItemName.equals(renameText.getValue())
		) {
			ChatUtils.print("Pulling out item");
			InventoryUtils.clickSlot(2, true);
			return;
		}

		// Set the name off the item in the anvil if present
		if (!itemStackInput1.isEmpty() && !outputItemName.equals(renameText.getValue())) {
			EditBox editBox = AnvilScreenAccessInvoker.getEditBox(((AnvilScreen) mc.screen));
			if (editBox != null) editBox.setValue(renameText.getValue());
			return;
		}

		// Move an item into the anvil if there is no item in the anvil
		if (itemStackInput1.isEmpty() && itemStackInput2.isEmpty()) {
			for (int i = 3; i < 36 + 3; i++) {
				ItemStack itemStack = containerMenu.getSlot(i).getItem();
				if (!itemStack.isEmpty() && !removeBrackets(itemStack.getDisplayName().getString()).equals(renameText.getValue())) {
					if (onlyShulkers.getValue() && !isShulker(itemStack)) continue;
					InventoryUtils.clickSlot(i, true);
					return;
				}
			}
		}
	}

	public static boolean isShulker(ItemStack itemStack) {
		return itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock;
	}

	public static String removeBrackets(String str) {
		StringBuilder sb = new StringBuilder(str);
		if (str.length() > 2) {
			sb.deleteCharAt(0);
			sb.setLength(sb.length() - 1);
			return sb.toString();
		}
		return str;
	}
}
