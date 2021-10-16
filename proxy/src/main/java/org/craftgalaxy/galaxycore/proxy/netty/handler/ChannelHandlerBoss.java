package org.craftgalaxy.galaxycore.proxy.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;

@AllArgsConstructor
public class ChannelHandlerBoss extends ChannelInboundHandlerAdapter {

    private final CoreProxyPlugin plugin;

    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.plugin.getServerManager().unregister(ctx.channel());
    }
}
