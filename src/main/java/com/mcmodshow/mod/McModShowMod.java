package com.mcmodshow.mod;

import com.mcmodshow.mod.config.McModShowConfig;
import com.mcmodshow.mod.setup.McModShowClientEvents;
import com.mcmodshow.mod.setup.McModShowForgeEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(McModShowMod.MOD_ID)
public class McModShowMod {
    public static final String MOD_ID = "mcmodshow";

    public McModShowMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, McModShowConfig.SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        McModShowClientEvents.register(modBus);

        MinecraftForge.EVENT_BUS.register(new McModShowForgeEvents());
    }
}
