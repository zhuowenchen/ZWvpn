package tincczw.callback;

import io.netty.channel.Channel;

public interface ConnectCallBack {

    void success(Channel channel);
    void error();
}
