package tincczw.handler1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class HttpServerHandler1 extends ChannelInboundHandlerAdapter {
    private String remoteHost ;
    private int remotePort ;
    private boolean isTunnel;
    private Channel outBoundChannel;
    private boolean hasConnect;
    ChannelFuture remoteConnectFuture;
    private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    public HttpServerHandler1(){
        super();
    }
    public HttpServerHandler1(String remoteHost, int remotePort){
        super();
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            System.out.println("收到HTTP请求");
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            if(remoteHost==null&&fullHttpRequest.getMethod()==HttpMethod.CONNECT){
                String uri = fullHttpRequest.getUri();
                if (!uri.startsWith("/")) {
                    if(!uri.startsWith("http://")){
                        uri = "http://" + uri;
                    }
                }
                System.out.println(uri);
                URL url = new URL(uri);
                remoteHost = url.getHost();
                remotePort = url.getPort()!=-1?url.getPort():url.getDefaultPort();

            }
            if(remoteHost!=null){
                if(isTunnel){
                    writeToRemote(fullHttpRequest);
                } else{

                    if(remoteConnectFuture == null){
                        remoteConnectFuture = connectRemote(ctx);
                    }
                    if(!HttpMethod.CONNECT.equals(fullHttpRequest.getMethod())){
                        remoteConnectFuture.addListener(future -> writeToRemote(fullHttpRequest));
                    }

                }
            }
        }else{
           if(outBoundChannel!=null){
               outBoundChannel.writeAndFlush(msg);
           }else{
               connectRemote(ctx);
               outBoundChannel.writeAndFlush(msg);
           }
        }
    }

    /*@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        DefaultHttpRequest defaultHttpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.GET,"/");
        ChannelFuture cl = ctx.writeAndFlush(defaultHttpRequest);
    }*/
    private ChannelFuture connectRemote(final ChannelHandlerContext ctx){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch)
                            throws Exception {
                        outBoundChannel = ch;
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                        pipeline.addLast(new NettyProxyClientHandler(ctx.channel()));
                    }}).option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = bootstrap.connect(remoteHost,remotePort);
        future.addListener(future1 -> {
            if (future1.isSuccess()) {
                isTunnel = true;
                logger.info("连接成功: " + remoteHost + ":" + remotePort);
            } else {
                logger.error("连接失败: " + remoteHost + ":" + remotePort);
            }
        });
        return future;
    }
    private void writeToRemote(FullHttpRequest httpRequest){
        DefaultFullHttpRequest defaultFullHttpRequest;
        if(httpRequest.content()!=null){
            defaultFullHttpRequest = new DefaultFullHttpRequest(httpRequest.getProtocolVersion(),httpRequest.getMethod(),httpRequest.getUri(),httpRequest.content());
        }
       else{
           defaultFullHttpRequest = new DefaultFullHttpRequest(httpRequest.getProtocolVersion(),httpRequest.getMethod(),httpRequest.getUri());
        }
       outBoundChannel.writeAndFlush(defaultFullHttpRequest).addListener(future -> {
           if(future.isSuccess()){

           }else{
               logger.warn("Something wrong happen,can't write message");
           }
       });
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
