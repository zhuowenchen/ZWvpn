package tincczw.handler1;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;


public class NettyProxyClientHandler extends SimpleChannelInboundHandler {
    private Channel inBoundChannel;
    public NettyProxyClientHandler(Channel channel) {
        this.inBoundChannel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( inBoundChannel.isActive() ) {
            System.out.println("accept msg write to client");
            inBoundChannel.writeAndFlush(msg);
        } else {
            ctx.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
