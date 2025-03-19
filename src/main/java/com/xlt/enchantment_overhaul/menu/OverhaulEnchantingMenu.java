package com.xlt.enchantment_overhaul.menu;

import com.xlt.enchantment_overhaul.EnchantmentUtils;
import com.xlt.enchantment_overhaul.ModMenuTypes;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class OverhaulEnchantingMenu extends AbstractContainerMenu {

    private final Container enchantSlot;
    public ItemStack lastStack = ItemStack.EMPTY;
    private int scrollIndex = 0;

    public OverhaulEnchantingMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, ContainerLevelAccess.NULL); // Only 1 slot needed
    }

    public OverhaulEnchantingMenu(int id, Inventory playerInventory, ContainerLevelAccess access){
        super(ModMenuTypes.OVERHAUL_ENCHANTING_MENU.get(), id);

        this.enchantSlot = new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                OverhaulEnchantingMenu.this.slotsChanged(this);
            }
        };

        //Handle the internal inventory
        this.addSlot(new Slot(enchantSlot, 0, 22, 47) {
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.isEnchantable() || itemstack.isEnchanted();
            }

            public int getMaxStackSize() {
                return 1;
            }
        });
        
        //Handle the Player's inventory
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        //Handle the Player's hotbar
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);

        scrollIndex = 0;  // Reset scrolling
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY; // No item in the slot
        }

        ItemStack originalStack = slot.getItem();
        ItemStack copyStack = originalStack.copy();

        int containerSlotCount = 1; // Number of slots in the custom container
        int hotbarStart = containerSlotCount + 27; // Hotbar starts after inventory slots
        int hotbarEnd = hotbarStart + 9; // Hotbar range
        int inventoryStart = containerSlotCount;
        int inventoryEnd = hotbarStart; // Inventory ends where the hotbar starts

        // Move from container to player's hotbar first, then inventory
        if (index < containerSlotCount) {
            if (!this.moveItemStackTo(originalStack, hotbarStart, hotbarEnd, false)) { // Try hotbar first
                if (!this.moveItemStackTo(originalStack, inventoryStart, inventoryEnd, false)) { // Then main inventory
                    return ItemStack.EMPTY;
                }
            }
        }
        // Move from player inventory to container
        else {
            if (!this.moveItemStackTo(originalStack, 0, containerSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        // Handle empty slot cleanup
        if (originalStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, originalStack);
        return copyStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlot.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        ItemStack stack = this.enchantSlot.getItem(0); // Get item from slot

        ItemStack movedStack = this.quickMoveStack(player, 0);
        if (!movedStack.isEmpty()) {
            return; // Successfully moved to inventory
        }

        // If quickMoveStack fails, drop the item on the ground
        player.drop(stack, false);
        this.enchantSlot.setItem(0, ItemStack.EMPTY);
    }

    public int getScrollIndex() {
        return scrollIndex;
    }

    public void setScrollIndex(int index) {
        scrollIndex = index;
    }
}
