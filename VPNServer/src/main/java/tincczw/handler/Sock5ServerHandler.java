package tincczw.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tincczw.callback.ConnectCallBack;
import tincczw.config.Config;
import tincczw.key.Constants;
import tincczw.manager.ProxyConnectionManager;
import tincczw.message.ProxyMessage;

public class Sock5ServerHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(Sock5ServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if(msg instanceof ProxyMessage){
            Channel serverChannel=channelHandlerContext.channel();
            ProxyMessage proxyMessage = (ProxyMessage) msg;
            //验证账号密码是否正确
            if (Config.username.equals(proxyMessage.getUsername())&&Config.password.equals(proxyMessage.getPassword())){
                if (ProxyMessage.BUILD_CONNECT==proxyMessage.getType()){


                    ProxyConnectionManager.connect(proxyMessage.getTargetHost(), proxyMessage.getTargetPort(), serverChannel, new ConnectCallBack() {
                        @Override
                        public void success(Channel connectChannel) {
                            //绑定连接
                            serverChannel.attr(Constants.REFLECT_CHANNEL).set(connectChannel);
                            connectChannel.attr(Constants.REFLECT_CHANNEL).set(serverChannel);
                            //发送连接成功回调
                            serverChannel.writeAndFlush(ProxyConnectionManager.wrapConnectSuccess(proxyMessage.getTargetHost(),proxyMessage.getTargetPort()));
                        }

                        @Override
                        public void error() {
                            //通知客户端关闭连接
                            ProxyConnectionManager.notifyClientClose(serverChannel);
                        }
                    });
                }
                if (ProxyMessage.TRANSFER==proxyMessage.getType()){
                    Channel connectChannel=serverChannel.attr(Constants.REFLECT_CHANNEL).get();
                    if (connectChannel!=null&&connectChannel.isActive()){
        //                    connectChannel.config().setOption(ChannelOption.AUTO_READ,true);
                        ByteBuf byteBuf = channelHandlerContext.alloc().buffer(proxyMessage.getData().length);
                        byteBuf.writeBytes(proxyMessage.getData());
                        logger.info("[转发数据]发送数据大小->{}字节", proxyMessage.getData().length);
                        connectChannel.writeAndFlush(byteBuf);
                    }else {
                        //通知客户端关闭连接
                        ProxyConnectionManager.notifyClientClose(serverChannel);
                    }
                }
                if (ProxyMessage.CLOSE==proxyMessage.getType()){
                    //关闭连接
                    ProxyConnectionManager.notifyClientClose(serverChannel);
                }
            }
        }
    }
}
