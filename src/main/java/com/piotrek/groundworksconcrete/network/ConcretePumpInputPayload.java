package com.piotrek.groundworksconcrete.network;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Sends only operator control axes and edges, not arbitrary world positions.
 * Stage 7 wireless remote will use a separate pairing/authorization mechanism.
 */
public record ConcretePumpInputPayload(
        float throttle,float steer,float slewAxis,float jointAxis,int selectedJoint,
        boolean toggleLegs,boolean toggleLights,boolean horn,boolean stow
) implements CustomPacketPayload {
    public static final Type<ConcretePumpInputPayload> TYPE=new Type<>(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"pump_truck_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf,ConcretePumpInputPayload> CODEC=
            new StreamCodec<>() {
                @Override
                public ConcretePumpInputPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ConcretePumpInputPayload(buf.readFloat(),buf.readFloat(),
                            buf.readFloat(),buf.readFloat(),buf.readVarInt(),
                            buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readBoolean());
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf,ConcretePumpInputPayload p){
                    buf.writeFloat(p.throttle());buf.writeFloat(p.steer());
                    buf.writeFloat(p.slewAxis());buf.writeFloat(p.jointAxis());
                    buf.writeVarInt(p.selectedJoint());
                    buf.writeBoolean(p.toggleLegs());buf.writeBoolean(p.toggleLights());
                    buf.writeBoolean(p.horn());buf.writeBoolean(p.stow());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
