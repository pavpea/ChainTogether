package com.evailcodes.chaintogether.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateChainLengthPacket {
    private final Vec3 player1Pos;
    private final Vec3 player2Pos;

    public UpdateChainLengthPacket(Vec3 player1Pos, Vec3 player2Pos) {
        this.player1Pos = player1Pos;
        this.player2Pos = player2Pos;
    }

    public static void encode(UpdateChainLengthPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.player1Pos.x);
        buf.writeDouble(packet.player1Pos.y);
        buf.writeDouble(packet.player1Pos.z);
        buf.writeDouble(packet.player2Pos.x);
        buf.writeDouble(packet.player2Pos.y);
        buf.writeDouble(packet.player2Pos.z);
    }

    public static UpdateChainLengthPacket decode(FriendlyByteBuf buf) {
        Vec3 player1Pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 player2Pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new UpdateChainLengthPacket(player1Pos, player2Pos);
    }

    public static void handle(UpdateChainLengthPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 客户端处理逻辑将在客户端包处理器中实现
        });
        context.get().setPacketHandled(true);
    }

    public Vec3 getPlayer1Pos() {
        return player1Pos;
    }

    public Vec3 getPlayer2Pos() {
        return player2Pos;
    }
}
