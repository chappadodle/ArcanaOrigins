package com.chappadodle.arcanaorigins.network;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.client.OreSenseRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Our few packets. */
@EventBusSubscriber(modid = ArcanaOrigins.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(OreSensePayload.TYPE, OreSensePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> OreSenseRenderer.show(payload.ores())));
    }
}
