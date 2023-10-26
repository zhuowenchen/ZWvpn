package tincczw.key;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public interface Constants {
        AttributeKey<Channel> REFLECT_CHANNEL=AttributeKey.newInstance("REFLECT_CHANNEL");
}
