package tincczw.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class NettyProxyClientHandler extends ChannelInboundHandlerAdapter {
    private Channel inBoundChannel;
    public NettyProxyClientHandler(Channel channel) {
        this.inBoundChannel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( inBoundChannel.isActive() ) {

            inBoundChannel.writeAndFlush(msg);
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
