package tincczw.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.callback.ConnectCallBack;
import tincczw.config.Config;
import tincczw.key.Constants;
import tincczw.manager.ProxyConnectionManager;
import tincczw.message.ProxyMessageUtil;

import java.net.URL;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    /**
     * 收到的msg是Http代理请求，需要转化Sock5请求（需要加密，AES或DES）发给远方代理服务器
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */

    public String host;
    public int port;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            System.out.println("收到HTTP请求");
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            if(fullHttpRequest.getMethod()== HttpMethod.CONNECT){
                String uri = fullHttpRequest.getUri();
                if (!uri.startsWith("/")) {
                    if(!uri.startsWith("http://")){
                        uri = "http://" + uri;
                    }
                }
                URL url = new URL(uri);
                host = url.getHost();
                port = url.getPort()!=-1?url.getPort():url.getDefaultPort();
                ProxyConnectionManager.getProxyConnect(new ConnectCallBack() {
                    @Override
                    public void success(Channel channel) {
                        //绑定连接
                        ctx.channel().attr(Constants.REFLECT_CHANNEL).set(channel);
                        channel.attr(Constants.REFLECT_CHANNEL).set(ctx.channel());
                        //发送建立连接请求
                        channel.writeAndFlush(ProxyMessageUtil.wrapBuildConnect(host,port,Config.username,Config.password));
                    }

                    @Override
                    public void error() {
                        logger.error("something error happen when connect remote");
                        ctx.channel().close();
                    }
                });
            }
            if(ctx.channel().attr(Constants.REFLECT_CHANNEL).get()!=null){
                ByteBuf byteBuf= (ByteBuf) msg;
                ctx.channel().attr(Constants.REFLECT_CHANNEL).get().writeAndFlush(ProxyMessageUtil.wrapTransfer(byteBuf,Config.username, Config.password));
            }else {
                    //Todo:是否存在没发connect，直接发httprequest的情况，兼容？
            }

        }else{
            //发的https加密报文
            if(ctx.channel().attr(Constants.REFLECT_CHANNEL).get()!=null){
                ByteBuf byteBuf= (ByteBuf) msg;
                ctx.channel().attr(Constants.REFLECT_CHANNEL).get().writeAndFlush(ProxyMessageUtil.wrapTransfer(byteBuf,Config.username, Config.password));
            }else {
                //Todo:是否存在没发connect，直接发httprequest的情况，兼容？
            }
        }
    }


}
