package com.jeefbeebos23.qolmod.features;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MagicMirrorButton extends Button {

    private static final ItemStack ICON = new ItemStack(Items.RED_BED);

    public MagicMirrorButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 18, Component.empty(), onPress, DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable("qolmod.magicmirror")));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        context.item(ICON, getX() + 2, getY() + 1);
    }
}
