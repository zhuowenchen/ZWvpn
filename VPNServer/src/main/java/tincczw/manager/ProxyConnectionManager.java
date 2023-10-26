package tincczw.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.callback.ConnectCallBack;
import tincczw.config.Config;
import tincczw.handler.HttpClientHandler;
import tincczw.key.Constants;
import tincczw.message.ProxyMessage;

public class ProxyConnectionManager {
    protected static Logger logger= LoggerFactory.getLogger(ProxyConnectionManager.class);

    private static Bootstrap bootstrap=new Bootstrap();

    static {
        bootstrap
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                //关闭Nagle算法
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline=socketChannel.pipeline();
                        pipeline.addLast(new HttpClientHandler());
                    }
                });
    }

    public static void connect(String host, int port, Channel serverChannel, ConnectCallBack connectCallBack) {
        String id = serverChannel.id().asShortText();
       bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (channelFuture.isSuccess()) {
                    //添加数据回调处理
                    logger.info("服务channelId->{},连接成功->{},{}", id, host, port);
                    connectCallBack.success(channelFuture.channel());
                } else {
                    logger.info("服务channelId->{},连接失败->{},{}", id, host, port);
                    connectCallBack.error();
                }
            }
        });
    }

    public static  void  notifyClientClose(Channel serverChannel) {
        if (serverChannel!=null&&serverChannel.isActive()) {
            logger.info("通知客户端关闭连接");
            Channel connectChannel = serverChannel.attr(Constants.REFLECT_CHANNEL).get();
            if (connectChannel != null && connectChannel.isActive()) {
                connectChannel.close();
                connectChannel.attr(Constants.REFLECT_CHANNEL).set(null);
            }
            serverChannel.attr(Constants.REFLECT_CHANNEL).set(null);
            serverChannel.writeAndFlush(ProxyConnectionManager.wrapClose());
        }
    }

    public static ProxyMessage wrapClose(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CLOSE);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost("4");
        proxyMessage.setTargetPort(4);
        proxyMessage.setData("4".getBytes());

        return proxyMessage;
    }

    public static ProxyMessage wrapConnectSuccess(String host,int port){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CONNECT_SUCCESS);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost(host);
        proxyMessage.setTargetPort(port);
        proxyMessage.setData("2".getBytes());

        return proxyMessage;
    }

    public static ProxyMessage wrapTransfer(ByteBuf byteBuf){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TRANSFER);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost("3");
        proxyMessage.setTargetPort(8888);
        byte[] data=new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        proxyMessage.setData(data);
        return proxyMessage;
    }
}
