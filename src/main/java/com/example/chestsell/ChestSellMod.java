package com.example.chestsell;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ChestSellMod implements ModInitializer {

    private static final Map<Identifier, Integer> SELL_PRICES = Map.of(
        Identifier.of("cobblemon", "relic_coin"), 50,
        Identifier.of("cobblemon", "relic_coin_pouch"), 450,
        Identifier.of("cobblemon", "relic_coin_sack"), 4050
    );

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(SellChestPayload.ID, SellChestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SellChestPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = player.getServer();

            server.execute(() -> {
                ScreenHandler handler = player.currentScreenHandler;
                if (handler == null || handler == player.playerScreenHandler) {
                    return;
                }

                long totalEarned = 0;

                // Scansiona tutti gli slot che appartengono al contenitore (vanilla o Sophisticated Storage)
                // escludendo automaticamente l'inventario del giocatore
                for (Slot slot : handler.slots) {
                    if (slot.inventory instanceof PlayerInventory) {
                        continue;
                    }

                    ItemStack stack = slot.getStack();
                    if (stack.isEmpty()) continue;

                    Identifier itemId = Registries.ITEM.getId(stack.getItem());
                    Integer price = SELL_PRICES.get(itemId);

                    if (price != null && slot.canTakeItems(player)) {
                        totalEarned += (long) price * stack.getCount();
                        slot.setStack(ItemStack.EMPTY);
                    }
                }

                if (totalEarned > 0) {
                    handler.sendContentUpdates();
                    String command = String.format("cobbledollars give %s %d", player.getName().getString(), totalEarned);
                    server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), command);
                    player.sendMessage(Text.literal("§aHai venduto gli oggetti nella cassa per §6" + totalEarned + " CobbleDollars§a!"), false);
                } else {
                    player.sendMessage(Text.literal("§cNessun oggetto vendibile trovato nel contenitore."), true);
                }
            });
        });
    }
}
