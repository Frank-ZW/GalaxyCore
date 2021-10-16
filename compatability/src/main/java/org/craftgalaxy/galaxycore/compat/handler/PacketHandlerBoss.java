package org.craftgalaxy.galaxycore.compat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.PacketHandler;
import org.craftgalaxy.galaxycore.compat.exception.KeepAliveTimeoutException;
import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.compat.wrapper.PacketWrapper;

@NoArgsConstructor
public class PacketHandlerBoss extends ChannelInboundHandlerAdapter {

    protected ChannelWrapper channelWrapper;
    private PacketHandler packetHandler;

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channelWrapper = new ChannelWrapper(ctx);
        if (this.packetHandler != null) {
            this.packetHandler.connected(this.channelWrapper);
        }
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (this.packetHandler != null) {
            this.channelWrapper.markClosed();
            this.packetHandler.disconnected(this.channelWrapper);
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (this.packetHandler != null) {
            PacketWrapper wrapper = (PacketWrapper)msg;
            boolean shouldHandle = this.packetHandler.shouldHandle(wrapper);
            try {
                if (shouldHandle && wrapper.getPacket() != null) {
                    wrapper.getPacket().handle(this.packetHandler);
                }

                if (shouldHandle) {
                    this.packetHandler.handle(wrapper);
                }
            } finally {
                wrapper.trySingleRelease();
            }
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (cause instanceof KeepAliveTimeoutException) {
            this.channelWrapper.close();
        }
    }

    public ChannelWrapper getChannelWrapper() {
        return this.channelWrapper;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }
}
