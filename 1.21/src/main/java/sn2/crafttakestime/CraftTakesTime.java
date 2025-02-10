package sn2.crafttakestime;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import sn2.crafttakestime.common.core.CraftManager;
import sn2.crafttakestime.impl.MC121Adapter;
import sn2.crafttakestime.networking.TimeCraftPacketHandler;
import sn2.crafttakestime.sound.SoundEventRegistry;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CraftTakesTime.MODID)
public class CraftTakesTime {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "crafttakestime";

    public CraftTakesTime(final ModContainer mod, final IEventBus bus) {
        SoundEventRegistry.SOUNDS.register(bus);
        CraftManager.getInstance().setMinecraftAdapter(new MC121Adapter());
    }
}
