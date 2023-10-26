package tincczw.boot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.config.Config;
import tincczw.decoder.MLengthFieldBasedFrameDecoder;
import tincczw.decoder.ProxyMessageDecoder;
import tincczw.encoder.ProxyMessageEncoder;
import tincczw.handler.Sock5ServerHandler;

public class ServerProxy {
    protected static Logger logger= LoggerFactory.getLogger(ServerProxy.class);

    private EventLoopGroup bossGroup=new NioEventLoopGroup();
    private EventLoopGroup workerGroup=new NioEventLoopGroup();
    private ServerBootstrap bootstrap=new ServerBootstrap();

    private int port;

    public ServerProxy(int port) {
        this.port = port;
    }


    public void run(){
        try {
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline =socketChannel.pipeline();
                            pipeline.addLast(new MLengthFieldBasedFrameDecoder());
                            pipeline.addLast(new ProxyMessageDecoder());
                            pipeline.addLast(new ProxyMessageEncoder());
                            //处理数据
                            pipeline.addLast(new Sock5ServerHandler());
                        }
                    });
            logger.debug("bind port : " + port);
            ChannelFuture future = bootstrap.bind(port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        ServerProxy proxySock5ServerBoot=new ServerProxy(Config.serverPort);
        proxySock5ServerBoot.run();
    }
}
