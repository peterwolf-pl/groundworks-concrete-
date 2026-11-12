package com.piotrek.groundworksconcrete.network;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Only the authenticated rider may send these inputs to its current truck. */
public record MixerTruckInputPayload(
        float throttle, float steer,
        boolean toggleDrum, boolean toggleLights, boolean horn
) implements CustomPacketPayload {
    public static final Type<MixerTruckInputPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, "mixer_truck_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MixerTruckInputPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public MixerTruckInputPayload decode(RegistryFriendlyByteBuf buf) {
                    return new MixerTruckInputPayload(buf.readFloat(),buf.readFloat(),
                            buf.readBoolean(),buf.readBoolean(),buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, MixerTruckInputPayload p) {
                    buf.writeFloat(p.throttle());
                    buf.writeFloat(p.steer());
                    buf.writeBoolean(p.toggleDrum());
                    buf.writeBoolean(p.toggleLights());
                    buf.writeBoolean(p.horn());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
