package com.xlt.enchantment_overhaul.menu;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xlt.enchantment_overhaul.EnchantmentOverhaulMod;
import com.xlt.enchantment_overhaul.EnchantmentUtils;
import com.xlt.enchantment_overhaul.network.ApplyEnchantmentPacket;
import com.xlt.enchantment_overhaul.network.EnchantmentOverhaulNetwork;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

public class OverhaulEnchantingScreen extends AbstractContainerScreen<OverhaulEnchantingMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(EnchantmentOverhaulMod.MODID,"textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");

    private static final int SCROLLBAR_X = 156; // Position of scrollbar on GUI
    private static final int SCROLLBAR_Y = 18;  // Y position (fixed)
    private static final int SCROLLBAR_HEIGHT = 53; // Scrollable area height
    private static final int SLIDER_WIDTH = 12;  // Size of the scroller sprite
    private static final int SLIDER_HEIGHT = 15; // Height of the slider

    private Map<Enchantment, Integer> enchantmentList = new Map<Enchantment, Integer>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Integer get(Object key) {
            return 0;
        }

        @Override
        public Integer put(Enchantment key, Integer value) {
            return 0;
        }

        @Override
        public Integer remove(Object key) {
            return 0;
        }

        @Override
        public void putAll(Map<? extends Enchantment, ? extends Integer> m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<Enchantment> keySet() {
            return Set.of();
        }

        @Override
        public Collection<Integer> values() {
            return List.of();
        }

        @Override
        public Set<Entry<Enchantment, Integer>> entrySet() {
            return Set.of();
        }
    };

    private int scrollIndex = menu.getScrollIndex(); // Current scroll position
    private static final int MAX_DISPLAYED = 4; // Max number of enchantments to show

    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;

    public OverhaulEnchantingScreen(OverhaulEnchantingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    protected void init() {
        super.init();
        assert this.minecraft != null;
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    public void containerTick() {
        super.containerTick();
        this.tickBook();

        // Get the item in slot 0 (the enchantment slot)
        ItemStack stack = this.menu.getSlot(0).getItem();

        if (!ItemStack.matches(stack, this.menu.lastStack)) {
            scrollIndex = 0;  // Reset scrolling if new item is inserted
            this.menu.lastStack = stack.copy();
        }

        // Update enchantment list
        if (!stack.isEmpty()) {
            // Get valid enchantments for the item
            enchantmentList = EnchantmentUtils.getValidEnchantments(stack); // Pass ItemStack directly
        } else {
            // If slot is empty, clear the list
            enchantmentList.clear();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        int maxScroll = Math.max(0, enchantmentList.size() - MAX_DISPLAYED);

        // Calculate new scroll index
        int newScrollIndex = (int) Math.max(0, Math.min(scrollIndex - scrollAmount, maxScroll));

        // If the scroll index actually changed, play a sound
        if (newScrollIndex != scrollIndex) {
            scrollIndex = newScrollIndex;

            // Play a satisfying sound when scrolling
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.25F, 1.2F);
            }

            // Force UI refresh
            assert this.minecraft != null;
            this.minecraft.setScreen(this);
        }

        return true;
    }

    private void renderBook(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        float f = Mth.lerp(partialTick, this.oOpen, this.open);
        float f1 = Mth.lerp(partialTick, this.oFlip, this.flip);
        Lighting.setupForEntityInInventory();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)x + 30.0F, (float)y + 31.0F, 100.0F);
        float f2 = 40.0F;
        guiGraphics.pose().scale(-40.0F, 40.0F, 40.0F);
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
        guiGraphics.pose().translate((1.0F - f) * 0.2F, (1.0F - f) * 0.1F, (1.0F - f) * 0.25F);
        float f3 = -(1.0F - f) * 90.0F - 90.0F;
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(f3));
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
        float f4 = Mth.clamp(Mth.frac(f1 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float f5 = Mth.clamp(Mth.frac(f1 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.bookModel.setupAnim(0.0F, f4, f5, f);
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
        this.bookModel.renderToBuffer(guiGraphics.pose(), vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.flush();
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    public void tickBook() {
        this.oFlip = this.flip;
        this.oOpen = this.open;

        if (this.menu.getSlot(0).getItem() != ItemStack.EMPTY)
        {
            this.open += 0.2F;
        } else this.open -= 0.2F;

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float f1 = (this.flipT - this.flip) * 0.4F;
        float f = 0.2F;
        f1 = Mth.clamp(f1, -0.2F, 0.2F);
        this.flipA += (f1 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Ensure tooltips are rendered
        renderTooltip(guiGraphics, mouseX, mouseY);
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Custom GUI rendering logic here
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.renderBook(guiGraphics, i, j, partialTicks);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        int adjustedMouseX = mouseX - this.leftPos;
        int adjustedMouseY = mouseY - this.topPos;

        Map<Enchantment, Integer> enchantmentsWithLevels = EnchantmentUtils.getValidEnchantments(ItemStack.EMPTY);

        ItemStack stack = this.menu.getSlot(0).getItem();
        if (!stack.isEmpty()) {
            enchantmentsWithLevels = EnchantmentUtils.getValidEnchantments(stack);

            renderEnchantmentLabels(guiGraphics, enchantmentsWithLevels, adjustedMouseX, adjustedMouseY);
        }
        renderScrollbar(guiGraphics, enchantmentsWithLevels, adjustedMouseX, adjustedMouseY);
    }

    /** Render Enchantment Labels & Scaled Text */
    private void renderEnchantmentLabels(GuiGraphics guiGraphics, Map<Enchantment, Integer> enchantmentsWithLevels, int mouseX, int mouseY) {
        int yStart = 18;  // Starting Y position
        int xStart = 60;  // X position
        int buttonWidth = 94;
        int buttonHeight = 13;

        ResourceLocation texture = new ResourceLocation(EnchantmentOverhaulMod.MODID, "textures/gui/enchantment_buttons.png");

        int i = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantmentsWithLevels.entrySet()) {
            if (i >= scrollIndex && i < scrollIndex + MAX_DISPLAYED) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                int yPosition = yStart + ((i - scrollIndex) * buttonHeight);

                // Draw button background
                renderButtons(guiGraphics, texture, xStart, yPosition, buttonWidth, buttonHeight, mouseX, mouseY);

                // Scale and draw enchantment text
                String enchantmentText = enchantment.getFullname(level).getString();
                renderScaledText(guiGraphics, enchantmentText, xStart, yPosition, buttonWidth);
            }
            i++;
        }
    }

    /** Render Button Background & Hover Effects */
    private void renderButtons(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        int vOffset = isHovered ? height : 0;

        guiGraphics.blit(texture, x, y, 0, vOffset, width, height);
    }

    /** Render Scaled Enchantment Text */
    private void renderScaledText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth) {
        int textWidth = this.font.width(text);
        float maxTextWidth = maxWidth - 10;
        float scale = textWidth > maxTextWidth ? maxTextWidth / textWidth : 1.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        float adjustedX = (x + 5) / scale;
        float adjustedY = (y + 3) / scale;

        guiGraphics.drawString(this.font, text, adjustedX, adjustedY, 0xFFFFFF, false);
        guiGraphics.pose().popPose();
    }

    /** Render Scrollbar Slider */
    private void renderScrollbar(GuiGraphics guiGraphics, Map<Enchantment, Integer> enchantmentsWithLevels, int mouseX, int mouseY) {
        ResourceLocation SCROLLER_TEXTURE = new ResourceLocation(EnchantmentOverhaulMod.MODID, "textures/gui/scroller.png");

        int maxScroll = Math.max(0, enchantmentsWithLevels.size() - MAX_DISPLAYED);
        float scrollProgress = (maxScroll > 0) ? (float) scrollIndex / maxScroll : 0;

        // Ensure the slider Y position never exceeds limits
        int sliderY = SCROLLBAR_Y + (int) (scrollProgress * (SCROLLBAR_HEIGHT - SLIDER_HEIGHT));
        sliderY = Math.max(SCROLLBAR_Y, Math.min(sliderY, SCROLLBAR_Y + SCROLLBAR_HEIGHT - SLIDER_HEIGHT));

        // Locked state only when scrolling is completely impossible
        boolean isLocked = enchantmentsWithLevels.size() <= MAX_DISPLAYED;

        // Change V offset based on lock state instead of hover
        int vOffset = isLocked ? SLIDER_HEIGHT : 0;  // Assuming locked state is in U,V offset position 2

        // Render the scrollbar with the correct texture based on state
        guiGraphics.blit(SCROLLER_TEXTURE, SCROLLBAR_X, sliderY, 0, vOffset, SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left-click only
            int yStart = 18; // GUI-relative Y starting position
            int xStart = 60; // GUI-relative X starting position
            int buttonWidth = 94;
            int buttonHeight = 13;

            // 🟢 Adjust mouse coordinates to be relative to the GUI position
            int adjustedMouseX = (int) (mouseX - this.leftPos);
            int adjustedMouseY = (int) (mouseY - this.topPos);

            ItemStack stack = this.menu.getSlot(0).getItem();
            if (!stack.isEmpty()) {
                Map<Enchantment, Integer> enchantmentsWithLevels = EnchantmentUtils.getValidEnchantments(stack);

                int i = 0;
                for (Map.Entry<Enchantment, Integer> entry : enchantmentsWithLevels.entrySet()) {
                    if (i >= scrollIndex && i < scrollIndex + MAX_DISPLAYED) {
                        int yPosition = yStart + ((i - scrollIndex) * buttonHeight);

                        // 🟢 Check if the click is within this button's bounds
                        if (adjustedMouseX >= xStart && adjustedMouseX <= xStart + buttonWidth &&
                                adjustedMouseY >= yPosition && adjustedMouseY <= yPosition + buttonHeight) {

                            // Player clicked this enchantment, send it to the server
                            Enchantment selectedEnchantment = entry.getKey();
                            int level = entry.getValue();
                            applyEnchantmentToItem(selectedEnchantment, level);
                            System.out.println("Mouse clicked at: " + mouseX + ", " + mouseY);
                            return true;
                        }
                    }
                    i++;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void applyEnchantmentToItem(Enchantment enchantment, int level) {
        ItemStack stack = this.menu.getSlot(0).getItem();
        if (!stack.isEmpty()) {
            System.out.println("Sending packet: " + enchantment.getDescriptionId() + " Level: " + level);
            // 🟢 Send packet to the server instead of applying enchantment client-side
            EnchantmentOverhaulNetwork.CHANNEL.sendToServer(new ApplyEnchantmentPacket(
                    BuiltInRegistries.ENCHANTMENT.getKey(enchantment), level
            ));
        }
    }

}
