package com.piotrek.groundworksconcrete.network;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Authenticated driver's held controls and edge-triggered action keys.
 * The server checks the source player's current vehicle and clamps inputs.
 */
public record MixerTruckInputPayload(
        float throttle, float steer,
        boolean toggleDrum, boolean toggleLights, boolean horn, boolean reverseDrum,
        float chuteYaw, float chuteTilt
) implements CustomPacketPayload {
    public static final Type<MixerTruckInputPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, "mixer_truck_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MixerTruckInputPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public MixerTruckInputPayload decode(RegistryFriendlyByteBuf buf) {
                    return new MixerTruckInputPayload(
                            buf.readFloat(), buf.readFloat(),
                            buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                            buf.readBoolean(), buf.readFloat(), buf.readFloat());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, MixerTruckInputPayload payload) {
                    buf.writeFloat(payload.throttle());
                    buf.writeFloat(payload.steer());
                    buf.writeBoolean(payload.toggleDrum());
                    buf.writeBoolean(payload.toggleLights());
                    buf.writeBoolean(payload.horn());
                    buf.writeBoolean(payload.reverseDrum());
                    buf.writeFloat(payload.chuteYaw());
                    buf.writeFloat(payload.chuteTilt());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
