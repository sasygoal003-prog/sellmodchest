package com.example.chestsell.mixin;

import com.example.chestsell.SellChestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class GenericContainerScreenMixin {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;

    @Inject(method = "init", at = @At("TAIL"))
    private void addSellAllButton(CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        ScreenHandler handler = screen.getScreenHandler();

        // Evita di mostrare il tasto nell'inventario personale del giocatore
        if (screen instanceof InventoryScreen) {
            return;
        }

        // Mostra il tasto solo se la schermata contiene almeno uno slot esterno all'inventario del giocatore
        boolean hasContainerSlots = handler.slots.stream()
            .anyMatch(slot -> !(slot.inventory instanceof PlayerInventory));

        if (!hasContainerSlots) {
            return;
        }

        ((ScreenAccessor) screen).invokeAddDrawableChild(
            ButtonWidget.builder(Text.literal("Vendi Tutto"), button -> {
                ClientPlayNetworking.send(new SellChestPayload());
            }).dimensions(this.x + this.backgroundWidth - 75, this.y - 22, 75, 20).build()
        );
    }
}
