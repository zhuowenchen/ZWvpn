package tincczw.config;

public class Config {

    public static final String username;
    public static final String password;

    public static final String serverHost;
    public static final int serverPort;

    public static final int localServerPort;

    static {
        username="";
        password="";
        serverHost="your server ip";
        serverPort=8888;
        localServerPort = 7777;
    }
}
