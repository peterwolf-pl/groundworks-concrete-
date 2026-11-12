package com.piotrek.groundworksconcrete.network;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client sends only intent; server validates owner, distance and held remote. */
public record PumpRemoteControlPayload(
        int targetEntityId,int selectedJoint,float slewAxis,float jointAxis,
        boolean togglePump,int rateDelta,boolean emergencyStop,boolean resetStop,
        boolean stow,boolean toggleLegs,boolean aimAtLook
) implements CustomPacketPayload {
    public static final Type<PumpRemoteControlPayload> TYPE=new Type<>(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"pump_remote_control"));

    public static final StreamCodec<RegistryFriendlyByteBuf,PumpRemoteControlPayload> CODEC=
            new StreamCodec<>() {
                @Override
                public PumpRemoteControlPayload decode(RegistryFriendlyByteBuf buf){
                    return new PumpRemoteControlPayload(buf.readVarInt(),buf.readVarInt(),
                            buf.readFloat(),buf.readFloat(),
                            buf.readBoolean(),buf.readVarInt(),buf.readBoolean(),buf.readBoolean(),
                            buf.readBoolean(),buf.readBoolean(),buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf,PumpRemoteControlPayload p){
                    buf.writeVarInt(p.targetEntityId());buf.writeVarInt(p.selectedJoint());
                    buf.writeFloat(p.slewAxis());buf.writeFloat(p.jointAxis());
                    buf.writeBoolean(p.togglePump());buf.writeVarInt(p.rateDelta());
                    buf.writeBoolean(p.emergencyStop());buf.writeBoolean(p.resetStop());
                    buf.writeBoolean(p.stow());buf.writeBoolean(p.toggleLegs());
                    buf.writeBoolean(p.aimAtLook());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
