package sn2.crafttakestime.event;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import sn2.crafttakestime.common.core.CraftManager;
import sn2.crafttakestime.networking.CraftConfigPayload;
import sn2.crafttakestime.networking.PacketCraftConfig;
import sn2.crafttakestime.networking.TimeCraftPacketHandler;

import java.util.Objects;

@EventBusSubscriber
public class Events {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        CraftManager.getInstance().loadConfig();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.getEntity() instanceof ServerPlayer serverPlayer) {
             byte[] bytes = new PacketCraftConfig(CraftManager.getInstance().getConfig()).toBytes();
             PacketDistributor.sendToPlayer(serverPlayer, new CraftConfigPayload(bytes));
         }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        CraftManager.getInstance().tick();
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        var cmd = dispatcher.register(
                Commands.literal("crafttakestime")
                        .then(Commands.literal("config").requires((commandSource) -> commandSource.hasPermission(2))
                                .then(Commands.literal("reload")
                                        .executes((commandSource) -> {
                                            CraftManager.getInstance().loadConfig();
                                            PacketCraftConfig packet = new PacketCraftConfig(CraftManager.getInstance().getConfig());
                                            PacketDistributor.sendToAllPlayers(new CraftConfigPayload(packet.toBytes()));
                                            commandSource.getSource().sendSuccess(
                                                    () -> Component.literal("Config reloaded and send to all players"),
                                                    true);
                                            return 1;
                                        }))
                                .then(Commands.literal("print")
                                        .executes((commandSource) -> {
                                            commandSource.getSource().sendSuccess(
                                                    () -> Component.literal(CraftManager.getInstance().getConfig().toString()),
                                                    true);
                                            return 1;
                                        }))));
        dispatcher.register(Commands.literal("ctt").redirect(cmd));
    }
}
