package com.evailcodes.chaintogether.network;

import com.evailcodes.chaintogether.ChainTogether;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ChainPacketHandler {
    private static final SimpleChannel INSTANCE;

    static {
        // Debug: Print ResourceLocation methods
        try {
            System.out.println("DEBUG: Dumping ResourceLocation methods:");
            for (java.lang.reflect.Method m : ResourceLocation.class.getMethods()) {
                System.out.println("Method: " + m.getName() + " " + m.getParameterCount());
            }
            for (java.lang.reflect.Method m : ResourceLocation.class.getDeclaredMethods()) {
                System.out.println("Declared Method: " + m.getName() + " " + m.getParameterCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SimpleChannel channel = null;
        try {
             // Try parse (m_338530_)
             channel = ChannelBuilder.named(
                ResourceLocation.parse(ChainTogether.MODID + ":main"))
                .networkProtocolVersion(1)
                .clientAcceptedVersions((s, v) -> true)
                .serverAcceptedVersions((s, v) -> true)
                .simpleChannel();
        } catch (Throwable t) {
            System.out.println("Failed to init channel with parse: " + t.getMessage());
            try {
                // Try fromNamespaceAndPath (m_339182_)
                channel = ChannelBuilder.named(
                    ResourceLocation.fromNamespaceAndPath(ChainTogether.MODID, "main"))
                    .networkProtocolVersion(1)
                    .clientAcceptedVersions((s, v) -> true)
                    .serverAcceptedVersions((s, v) -> true)
                    .simpleChannel();
            } catch (Throwable t2) {
                 System.out.println("Failed to init channel with fromNamespaceAndPath: " + t2.getMessage());
                 // Try legacy constructor via reflection?
                 try {
                     java.lang.reflect.Constructor<ResourceLocation> c = ResourceLocation.class.getDeclaredConstructor(String.class, String.class);
                     c.setAccessible(true);
                     channel = ChannelBuilder.named(c.newInstance(ChainTogether.MODID, "main"))
                        .networkProtocolVersion(1)
                        .clientAcceptedVersions((s, v) -> true)
                        .serverAcceptedVersions((s, v) -> true)
                        .simpleChannel();
                 } catch (Throwable t3) {
                     throw new RuntimeException("Failed to create ResourceLocation", t3);
                 }
            }
        }
        INSTANCE = channel;
    }
    private static int packetId = 0;

    public static void register() {
        // 注册包
        INSTANCE.messageBuilder(UpdateChainLengthPacket.class, packetId++, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UpdateChainLengthPacket::encode)
                .decoder(UpdateChainLengthPacket::decode)
                .consumerNetworkThread(UpdateChainLengthPacket::handle)
                .add();
        
        // 注册绑定状态同步包
        INSTANCE.messageBuilder(SyncBoundStatusPacket.class, packetId++, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncBoundStatusPacket::encode)
                .decoder(SyncBoundStatusPacket::decode)
                .consumerNetworkThread(SyncBoundStatusPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static <MSG> void sendToAll(MSG message) {
        INSTANCE.send(message, PacketDistributor.ALL.noArg());
    }
}