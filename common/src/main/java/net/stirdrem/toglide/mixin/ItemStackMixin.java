package net.stirdrem.toglide.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.toglide.PlayerEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "inventoryTick", at = @At("TAIL"))
    private void checkDroppedGlider(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        PlayerEntityDuck duck = (PlayerEntityDuck) player;

        if (duck.toglide$isGliding()) {
            if (duck.toglide$getActiveGlider() != null
                    && !player.getInventory().contains(new ItemStack(duck.toglide$getActiveGlider()))) {
                duck.toglide$setIsGliding(false);
            }
        }
    }
}
