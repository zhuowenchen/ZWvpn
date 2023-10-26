package tincczw.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.key.Constants;
import tincczw.manager.ProxyConnectionManager;

public class HttpClientHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger= LoggerFactory.getLogger(HttpClientHandler.class);
    public HttpClientHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel connectChannel = ctx.channel();
        Channel serverChannel = connectChannel.attr(Constants.REFLECT_CHANNEL).get();
        //回写数据
        if (serverChannel != null && serverChannel.isActive()) {
            ByteBuf byteBuf = (ByteBuf) msg;
            logger.info("[请求连接]回写数据大小->{}字节", byteBuf.readableBytes());
            serverChannel.writeAndFlush(ProxyConnectionManager.wrapTransfer(byteBuf));
        } else {
            logger.info("[请求连接]服务连接不存在，关闭请求连接");
            connectChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
