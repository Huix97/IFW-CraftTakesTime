package sn2.crafttakestime.networking;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sn2.crafttakestime.CraftTakesTime;
import sn2.crafttakestime.common.config.CraftConfig;
import sn2.crafttakestime.common.core.CraftManager;

@EventBusSubscriber(modid = CraftTakesTime.MODID, bus = EventBusSubscriber.Bus.MOD)
public class TimeCraftPacketHandler {
    private static final Logger log = LogManager.getLogger(TimeCraftPacketHandler.class);

    @SubscribeEvent
    private static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                CraftConfigPayload.TYPE,
                CraftConfigPayload.STREAM_CODEC,
                TimeCraftPacketHandler::handle);
    }


    private static void handle(CraftConfigPayload payload, IPayloadContext context) {
        try {
            String json = new String(payload.contents());
            CraftConfig config = new Gson().fromJson(json, CraftConfig.class);
            context.enqueueWork(() -> CraftManager.getInstance().setConfig(config));
        } catch (Exception e) {
            log.error("Failed to read config from bytes");
        }
    }
}
