package net.stirdrem.toglide.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public class FabricGliderItem extends GliderItem {
    public FabricGliderItem(double dropVelocity, double speedFactor, Properties settings) {
        super(dropVelocity, speedFactor, settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof PlayerEntityDuck duck)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);

        if (!player.onGround() && !player.isFallFlying() && !player.isInWater()) {

            if (!level.isClientSide) {

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
                    id = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getKey(duck.toglide$getActiveGlider())
                            .toString();
                }

                SyncGliderPacket packet = new SyncGliderPacket(
                        player.getId(),
                        duck.toglide$isGliding(),
                        duck.toglide$isActivatingGlider(),
                        id
                );

                net.minecraft.server.level.ServerPlayer sp = (net.minecraft.server.level.ServerPlayer) player;

                net.minecraft.network.FriendlyByteBuf buf =
                        net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();

                SyncGliderPacket.encode(packet, buf);

                // send to others
                for (net.minecraft.server.level.ServerPlayer p :
                        net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(sp)) {

                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                            p,
                            ModNetworking.SYNC_GLIDER,
                            buf
                    );
                }

                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                        sp,
                        ModNetworking.SYNC_GLIDER,
                        buf
                );
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }
}
