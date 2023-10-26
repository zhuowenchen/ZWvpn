package tincczw.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.callback.ConnectCallBack;
import tincczw.config.Config;
import tincczw.decoder.MLengthFieldBasedFrameDecoder;
import tincczw.decoder.ProxyMessageDecoder;
import tincczw.encoder.ProxyMessageEncoder;
import tincczw.handler.Sock5ClientHandler;

public class ProxyConnectionManager {
    private static Logger logger= LoggerFactory.getLogger(ProxyConnectionManager.class);
    private static Bootstrap bootstrap=new Bootstrap();

    static {
        bootstrap
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                //关闭Nagle算法
                .option(ChannelOption.TCP_NODELAY, true)
                //连接超时
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline=socketChannel.pipeline();
                        pipeline.addLast(new MLengthFieldBasedFrameDecoder());
                        pipeline.addLast(new ProxyMessageEncoder());
                        pipeline.addLast(new ProxyMessageDecoder());
                        //处理返回数据
                        pipeline.addLast(new Sock5ClientHandler());
                    }
                });
    }


    public static void getProxyConnect(ConnectCallBack connectCallBack){

        bootstrap.connect(Config.serverHost, Config.serverPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()){
                    logger.info("[代理池]创建新连接成功");
                    connectCallBack.success(channelFuture.channel());
                }else {
                    logger.info("[代理池]创建新连接失败");
                    connectCallBack.error();
                }
            }
        });
    }
}
