package org.icetank;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class AutoAnvilRenameModule extends ToggleableModule {
	private final StringSetting renameText = new StringSetting("RenameText", "Sponsored by RusherHack Plugins");
	private final BooleanSetting selectiveMode = new BooleanSetting("Selective", false);
	private final StringSetting selectiveId = new StringSetting("ItemId", "end_crystal").setVisibility(this.selectiveMode::getValue);
	private final BooleanSetting onlyShulkers = new BooleanSetting("OnlyShulkers", false)
			.setVisibility(() -> !(this.selectiveMode.getValue()));
	private final BooleanSetting onlyRenamed = new BooleanSetting("OnlyRenamed", false);
	private final NumberSetting<Integer> clickDelay = new NumberSetting<>("Click Delay", 1, 3, 10);
	private final BooleanSetting autoXP = new BooleanSetting("AutoXP", false);
	private int delay = 0;

	public AutoAnvilRenameModule() {
		super("AutoAnvilRename", "Renames items in an anvil automatically", ModuleCategory.CLIENT);
		
		//register settings
		this.registerSettings(
				this.renameText,
				this.selectiveMode,
				this.selectiveId,
				this.onlyShulkers,
				this.onlyRenamed,
				this.clickDelay,
				this.autoXP
		);
	}

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

		// Check if there is an output
		if (!itemStackOutput.isEmpty()) {

			int cost = ((AnvilMenu) mc.player.containerMenu).getCost();

			// Check if name matches the renameText option with an edge case for empty name values which remove the name.
			if (outputItemName.equals(renameText.getValue()) || (renameText.getValue().equals("") && !itemStackOutput.hasCustomHoverName())) {

				// Automatically use XP bottles until the rename can be afforded
				if ((playerLevels < cost && !mc.player.isCreative()) && autoXP.getValue()) {
					if (!mc.player.isHolding(Items.EXPERIENCE_BOTTLE)) {
						int bottleSlot = InventoryUtils.findItemHotbar(Items.EXPERIENCE_BOTTLE);
						if (bottleSlot != -1 && bottleSlot > 0 && bottleSlot < 9) {
							mc.player.getInventory().selected = bottleSlot;
						}
					}

					if (mc.player.isHolding(Items.EXPERIENCE_BOTTLE)) {
						mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 1));
					}
				}

				// Take item from output if player has enough XP
				if (playerLevels >= cost || mc.player.isCreative()) {
					InventoryUtils.clickSlot(2, true);
					return;
				}
			}
		}

		// Set the name of the item in the anvil if present
		if (!itemStackInput1.isEmpty() && !outputItemName.equals(renameText.getValue())) {
			EditBox editBox = AnvilScreenAccessInvoker.getEditBox(((AnvilScreen) mc.screen));
			if (editBox != null) editBox.setValue(renameText.getValue());
			return;
		}

		// Move an item into the anvil if there is no item in the anvil
		if (itemStackInput1.isEmpty() && itemStackInput2.isEmpty()) {
			for (int i = 3; i < 36 + 3; i++) {
				ItemStack itemStack = containerMenu.getSlot(i).getItem();
				String itemName = removeBrackets(itemStack.getDisplayName().getString());
				String[] longItemId = itemStack.getDescriptionId().split("\\.");
				if (longItemId.length < 2) continue;
				String itemId = longItemId[longItemId.length - 1];

				if (selectiveMode.getValue() && !selectiveId.getValue().equals(itemId)) continue;
				if (onlyRenamed.getValue() && !itemStack.hasCustomHoverName()) continue;
				if (onlyShulkers.getValue() && !isShulker(itemStack) && !selectiveMode.getValue()) continue;
				if (itemStack.isEmpty()) continue;
				if (itemName.equals(renameText.getValue())) continue;

				ChatUtils.print(itemName + " -> " + renameText.getValue());
				InventoryUtils.clickSlot(i, true);
				return;
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
