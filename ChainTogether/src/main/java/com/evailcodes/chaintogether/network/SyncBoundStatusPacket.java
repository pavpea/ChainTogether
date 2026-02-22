package com.evailcodes.chaintogether.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncBoundStatusPacket {
    private final UUID player1;
    private final UUID player2;
    private final boolean bound;

    public SyncBoundStatusPacket(UUID player1, UUID player2, boolean bound) {
        this.player1 = player1;
        this.player2 = player2;
        this.bound = bound;
    }

    public static void encode(SyncBoundStatusPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeUUID(packet.player1);
        buf.writeUUID(packet.player2);
        buf.writeBoolean(packet.bound);
    }

    public static SyncBoundStatusPacket decode(RegistryFriendlyByteBuf buf) {
        UUID player1 = buf.readUUID();
        UUID player2 = buf.readUUID();
        boolean bound = buf.readBoolean();
        return new SyncBoundStatusPacket(player1, player2, bound);
    }

    public static void handle(SyncBoundStatusPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            // 客户端处理逻辑
            com.evailcodes.chaintogether.client.ChainRenderer.syncBoundStatus(
                    packet.player1, packet.player2, packet.bound);
        });
        context.setPacketHandled(true);
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public boolean isBound() {
        return bound;
    }
}
