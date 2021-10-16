package org.craftgalaxy.galaxycore.compat.wrapper;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.handler.PacketDecoder;
import org.craftgalaxy.galaxycore.compat.handler.PacketEncoder;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;

public class ChannelWrapper {

    private final Channel channel;
    private SocketAddress remoteAddress;
    private volatile boolean closing;
    private volatile boolean closed;

    public ChannelWrapper(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        this.remoteAddress = this.channel.remoteAddress() == null ? this.channel.parent().remoteAddress() : this.channel.remoteAddress();
    }

    public void setProtocol(Protocol protocol) {
        this.channel.pipeline().get(PacketDecoder.class).setProtocol(protocol);
        this.channel.pipeline().get(PacketEncoder.class).setProtocol(protocol);
    }

    public void write(Object packet) {
        if (!this.closed) {
            if (packet instanceof PacketWrapper wrapper) {
                wrapper.setReleased(true);
                this.channel.writeAndFlush(wrapper.getBuf(), this.channel.voidPromise());
            } else {
                this.channel.writeAndFlush(packet, this.channel.voidPromise());
            }
        }

    }

    public void markClosed() {
        this.closing = true;
        this.closed = true;
    }

    public void close() {
        this.close(null);
    }

    public void close(@Nullable Object packet) {
        if (!this.closed) {
            this.closing = true;
            this.closed = true;
            if (packet != null && this.channel.isActive()) {
                this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE).addListener(ChannelFutureListener.CLOSE);
            } else {
                this.channel.flush();
                this.channel.close();
            }
        }

    }

    public Channel getChannel() {
        return this.channel;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public boolean isClosing() {
        return this.closing;
    }

    public boolean isClosed() {
        return this.closed;
    }
}
