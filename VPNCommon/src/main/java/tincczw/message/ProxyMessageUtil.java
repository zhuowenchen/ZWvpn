package tincczw.message;

import io.netty.buffer.ByteBuf;

public class ProxyMessageUtil {

    public static ProxyMessage wrapBuildConnect(String host, int port,String username,String password){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.BUILD_CONNECT);
        proxyMessage.setUsername(username);
        proxyMessage.setPassword(password);
        proxyMessage.setTargetHost(host);
        proxyMessage.setTargetPort(port);
        proxyMessage.setData("1".getBytes());
        return proxyMessage;
    }

    public static ProxyMessage wrapClose(String username,String password){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CLOSE);
        proxyMessage.setUsername(username);
        proxyMessage.setPassword(password);
        proxyMessage.setTargetHost("");
        proxyMessage.setTargetPort(8888);
        proxyMessage.setData("4".getBytes());
        return proxyMessage;
    }

    public static ProxyMessage wrapTransfer(ByteBuf byteBuf,String username,String password,String host){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TRANSFER);
        proxyMessage.setUsername(username);
        proxyMessage.setPassword(password);
        proxyMessage.setTargetHost(host);
        proxyMessage.setTargetPort(0);
        byte[] data=new byte[byteBuf.readableBytes()];
        System.out.println("transfer 数据大小为 "+data.length);
        byteBuf.readBytes(data);
        proxyMessage.setData(data);
        return proxyMessage;
    }
}
