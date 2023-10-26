package tincczw.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.key.Constants;
import tincczw.message.ProxyMessage;

public class Sock5ClientHandler extends ChannelInboundHandlerAdapter {
    Logger logger = LoggerFactory.getLogger(Sock5ClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if (msg instanceof ProxyMessage) {
            ProxyMessage proxyMessage = (ProxyMessage) msg;
            Channel proxyChannel = channelHandlerContext.channel();
            Channel localChannel = proxyChannel.attr(Constants.REFLECT_CHANNEL).get();
            if (localChannel != null && localChannel.isActive()) {
                if (ProxyMessage.CONNECT_SUCCESS == proxyMessage.getType()) {
                    //do nothing;
                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    localChannel.writeAndFlush(response);
                    localChannel.pipeline().remove("aggregator");
                    localChannel.pipeline().remove("codec");
                }
                if (ProxyMessage.TRANSFER == proxyMessage.getType()) {
                    if (localChannel != null && localChannel.isActive()) {
                        ByteBuf byteBuf = channelHandlerContext.alloc().buffer(proxyMessage.getData().length);
                        logger.info("回写数据大小->{}字节", proxyMessage.getData().length);
                        byteBuf.writeBytes(proxyMessage.getData());
                        localChannel.writeAndFlush(byteBuf);
                    }
                }
                if (ProxyMessage.CLOSE == proxyMessage.getType()) {
                    logger.info("收到服务端关闭通知，关闭本地连接,归还代理连接");


                    //关闭本地连接
                    if (localChannel.isActive()) {
                        localChannel.close();
                    }
                }
            } else {

            }
        } else {
            logger.error("msg is not a proxyMessage");
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("服务连接关闭");
        Channel localChannel=ctx.channel().attr(Constants.REFLECT_CHANNEL).get();
        if (localChannel!=null&&localChannel.isActive()){
            localChannel.attr(Constants.REFLECT_CHANNEL).set(null);
            localChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("错误",cause.getMessage());
    }
}
