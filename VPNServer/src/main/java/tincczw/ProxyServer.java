package tincczw;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import tincczw.handler.HttpServerHandler;

public class ProxyServer {

    private NioEventLoopGroup serverWorkerGroup;
    private NioEventLoopGroup serverBossGroup;

    public ProxyServer(){
        serverBossGroup = new NioEventLoopGroup();
        serverWorkerGroup = new NioEventLoopGroup();
    }

    private void init(){
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(serverBossGroup,serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler( new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new HttpResponseEncoder());
                socketChannel.pipeline().addLast(new HttpRequestDecoder());
                socketChannel.pipeline().addLast(new HttpServerHandler());
                socketChannel.pipeline().addLast(new HttpServerCodec());
            }
        }
        );
    }
}
