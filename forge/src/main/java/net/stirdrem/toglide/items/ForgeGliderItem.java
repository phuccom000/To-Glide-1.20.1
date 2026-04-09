package net.stirdrem.toglide.items;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public class ForgeGliderItem extends GliderItem {

    public ForgeGliderItem(double dropVelocity, double speedFactor, Properties settings) {
        super(dropVelocity, speedFactor, settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof PlayerEntityDuck duck)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);

        if (!player.onGround() && !player.isFallFlying()) {

            if (!level.isClientSide) { // 🔥 SERVER ONLY

                // Toggle state
                if (duck.toglide$isGliding()) {
                    duck.toglide$setIsGliding(false);
                    duck.toglide$setActiveGlider(null);
                    duck.toglide$setIsActivatingGlider(false);
                } else {
                    duck.toglide$setIsGliding(true);
                    duck.toglide$setIsActivatingGlider(true);
                    duck.toglide$setActiveGlider(this);
                }

                String id = "";
                if (duck.toglide$getActiveGlider() != null) {
                    id = BuiltInRegistries.ITEM
                            .getKey(duck.toglide$getActiveGlider())
                            .toString();
                }

                SyncGliderPacket packet = new SyncGliderPacket(
                        player.getId(),
                        duck.toglide$isGliding(),
                        duck.toglide$isActivatingGlider(),
                        id
                );

                ServerPlayer sp = (ServerPlayer) player;

                ModNetworking.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> sp),
                        packet
                );

                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        packet
                );
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }
}