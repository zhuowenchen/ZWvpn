package tincczw.handler1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture cf;
    private String host;
    private int port;
    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(""+msg.getClass());
        if( msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest) msg;
            String host = request.headers().get("host");

            String[] temp = host.split(":");
            int port = 80;
            if (temp.length > 1) {
                port = Integer.parseInt(temp[1]);
            } else {
                if (request.getUri().indexOf("https") == 0) {
                    port = 443;
                }
            }
            this.host = temp[0];
            this.port = port;
            if(request.getMethod()==HttpMethod.CONNECT) {
                System.out.println("https Connect 请求.目标: " + request.getUri());
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("httpObject");
                return;
            }
            System.out.println("http 请求.目标: " + request.getUri());
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()) // 注册线程池
                    .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new ChannelInitializer(){
                        @Override
                        protected void initChannel(final Channel channel) {
                            channel.pipeline().addLast(new HttpClientCodec());
                            channel.pipeline().addLast(new HttpObjectAggregator(6553600));
                            channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    FullHttpResponse response = (FullHttpResponse) msg;
                                    channel.writeAndFlush(response);
                                }
                            });
                        }
                    });

            ChannelFuture cf = bootstrap.connect(temp[0], port);
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });
        }else{

            if(cf == null){
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop()) // 复用客户端连接线程池
                        .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                        .handler(new NettyProxyClientHandler(ctx.channel()));
                System.out.println("准备连接目标服务器： "+host);
                cf = bootstrap.connect(host, port);
                cf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            System.out.println("连接目标服务器成功： "+host);
                           /* EmbeddedChannel ch = new EmbeddedChannel(new HttpRequestEncoder());
                            System.out.println(1111111111);
                            ch.writeOutbound(msg);
                            System.out.println(222222222);
                            ByteBuf byteBuf= ch.readOutbound();
                            System.out.println(333333333);
                            byte[] data=new byte[byteBuf.readableBytes()];
                            System.out.println(44444);
                            ByteBuf byteBuf1 = future.channel().alloc().buffer(data.length);
                            System.out.println(55555);
                            byteBuf1.writeBytes(data);
                            System.out.println(6666);*/
                            future.channel().writeAndFlush(msg);
                        } else {
                            System.out.println("连接目标服务器失败： "+host);
                            ctx.channel().close();
                        }
                    }
                });

            } else {
                System.out.println("cf!=null： "+host);
                cf.channel().writeAndFlush(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
