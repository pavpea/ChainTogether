package com.evailcodes.chaintogether.client;

import com.evailcodes.chaintogether.config.ChainConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "chaintogether")
public class ChainRenderer {
    // 客户端绑定状态缓存
    private static final Map<java.util.UUID, java.util.UUID> CLIENT_BOUND_PLAYERS = new HashMap<>();
    
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer localPlayer = mc.player;
            
            if (localPlayer == null || mc.level == null) {
                return;
            }
            
            renderWires(event.getPoseStack(), localPlayer, event.getPartialTick());
        }
    }
    
    // 检查两个玩家是否绑定
    private static boolean isPlayersBound(Player player1, Player player2) {
        // 检查是否在客户端缓存中绑定
        return CLIENT_BOUND_PLAYERS.containsKey(player1.getUUID()) && 
               CLIENT_BOUND_PLAYERS.get(player1.getUUID()).equals(player2.getUUID());
    }
    
    // 同步绑定状态（实际应该通过网络包调用）
    public static void syncBoundStatus(java.util.UUID player1, java.util.UUID player2, boolean bound) {
        if (bound) {
            CLIENT_BOUND_PLAYERS.put(player1, player2);
            CLIENT_BOUND_PLAYERS.put(player2, player1);
        } else {
            CLIENT_BOUND_PLAYERS.remove(player1);
            CLIENT_BOUND_PLAYERS.remove(player2);
        }
    }
    
    private static void renderWires(PoseStack poseStack, Player player, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        
        if (localPlayer == null || mc.level == null) {
            return;
        }
        
        List<PlayerWireData> wires = new ArrayList<>();
        
        // 只渲染本地玩家和其绑定伙伴之间的线条
        // 由于网络同步暂时未实现，这里使用一个简单的方法：
        // 检查本地玩家是否有绑定伙伴（通过客户端缓存）
        // 注意：这是一个临时解决方案，实际应该通过网络包同步绑定状态
        for (Player otherPlayer : mc.level.players()) {
            if (otherPlayer != localPlayer) {
                // 检查两个玩家是否绑定（这里应该通过网络同步获取，暂时使用模拟方法）
                // 实际实现时，应该从服务器同步绑定状态
                if (isPlayersBound(localPlayer, otherPlayer)) {
                    // 使用实际的配置值作为距离倍数
                    double maxDistance = ChainConfig.CHAIN_LENGTH.get() * 10.0;
                    wires.add(new PlayerWireData(localPlayer.getUUID(), otherPlayer.getUUID(), maxDistance, true));
                }
            }
        }
        
        if (wires.isEmpty()) {
            return;
        }
        
        // 获取渲染缓冲区
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.LINES);
        
        // 获取相机位置
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        for (PlayerWireData wire : wires) {
            // 检查是否激活
            if (!wire.isActive()) {
                continue; // 不渲染
            }
            
            Player player1 = mc.level.getPlayerByUUID(wire.getPlayer1());
            Player player2 = mc.level.getPlayerByUUID(wire.getPlayer2());
            
            if (player1 == null || player2 == null) {
                continue;
            }
            
            // 计算位置（考虑部分刻度）
            Vec3 pos1 = player1.getPosition(partialTicks);
            Vec3 pos2 = player2.getPosition(partialTicks);
            
            // 计算距离
            double distance = pos1.distanceTo(pos2);
            double maxDistance = wire.getMaxDistance();
            
            // 检查是否超过200%距离
            if (distance > maxDistance * 2.0) {
                continue; // 不渲染
            }
            
            // 计算颜色渐变
            Color color = calculateColor(distance, maxDistance);
            
            // 渲染线
            renderLine(poseStack, vertexConsumer, cameraPos, pos1, pos2, color);
        }
        
        // 结束渲染
        bufferSource.endBatch(RenderType.LINES);
    }
    
    private static Color calculateColor(double distance, double maxDistance) {
        double percentage = distance / maxDistance;
        
        if (percentage <= 0.5) {
            // 绿色
            return Color.GREEN;
        } else if (percentage <= 1.0) {
            // 从绿色渐变到红色
            float factor = (float) ((percentage - 0.5) / 0.5);
            int red = Math.min(255, (int) (255 * factor));
            int green = Math.min(255, 255 - red);
            return new Color(red, green, 0);
        } else {
            // 红色
            return Color.RED;
        }
    }
    
    private static void renderLine(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 start, Vec3 end, Color color) {
        poseStack.pushPose();
        
        // 获取透明度
        float alpha = ChainConfig.getTransparencyAsFloat();
        
        // 计算相对位置
        Vec3 startRelative = start.subtract(cameraPos);
        Vec3 endRelative = end.subtract(cameraPos);
        
        // 转换颜色为浮点数
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        
        // 渲染线
        vertexConsumer.vertex(poseStack.last().pose(), (float) startRelative.x, (float) startRelative.y + 1.0f, (float) startRelative.z)
                .color(r, g, b, alpha)
                .normal(0, 1, 0)
                .endVertex();
        
        vertexConsumer.vertex(poseStack.last().pose(), (float) endRelative.x, (float) endRelative.y + 1.0f, (float) endRelative.z)
                .color(r, g, b, alpha)
                .normal(0, 1, 0)
                .endVertex();
        
        poseStack.popPose();
    }
    
    // 玩家绑定线数据类
    private static class PlayerWireData {
        private final java.util.UUID player1;
        private final java.util.UUID player2;
        private final double maxDistance;
        private final boolean active;
        
        public PlayerWireData(java.util.UUID player1, java.util.UUID player2, double maxDistance, boolean active) {
            this.player1 = player1;
            this.player2 = player2;
            this.maxDistance = maxDistance;
            this.active = active;
        }
        
        public java.util.UUID getPlayer1() {
            return player1;
        }
        
        public java.util.UUID getPlayer2() {
            return player2;
        }
        
        public double getMaxDistance() {
            return maxDistance;
        }
        
        public boolean isActive() {
            return active;
        }
    }
}