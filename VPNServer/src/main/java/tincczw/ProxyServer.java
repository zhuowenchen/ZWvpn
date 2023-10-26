package tincczw;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import tincczw.handler1.HttpServerHandler;

public class ProxyServer {

    private NioEventLoopGroup serverWorkerGroup;
    private NioEventLoopGroup serverBossGroup;

    public ProxyServer(){
        serverBossGroup = new NioEventLoopGroup();
        serverWorkerGroup = new NioEventLoopGroup();
    }

    private void init(){
        try{
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(serverBossGroup,serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler( new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast("httpCodec",new HttpServerCodec());
                socketChannel.pipeline().addLast("httpObject",new HttpObjectAggregator(65536));
                socketChannel.pipeline().addLast("serverHandle",new HttpServerHandler());
            }
        }
        );
        ChannelFuture f = serverBootstrap
                .bind(7070)
                .sync();
        f.channel().closeFuture().sync();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
            serverBossGroup.shutdownGracefully();
            serverWorkerGroup.shutdownGracefully();
    }

    }

    public static void main(String[] args) {
        new ProxyServer().init();
    }
}
