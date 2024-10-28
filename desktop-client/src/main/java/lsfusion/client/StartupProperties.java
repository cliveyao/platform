package lsfusion.client;

public class StartupProperties {

    public static final String LSFUSION_CLIENT_HOSTNAME = "lsfusion.client.hostname";

    public static final String LSFUSION_CLIENT_HOSTPORT = "lsfusion.client.hostport";

    public static final String LSFUSION_CLIENT_EXPORTNAME = "lsfusion.client.exportname";

    public static final String LSFUSION_CLIENT_USER = "lsfusion.client.user";

    public static final String LSFUSION_CLIENT_PASSWORD = "lsfusion.client.password";

    public static final String LSFUSION_CLIENT_AUTOLOGIN = "lsfusion.client.autologin";

    public static final String LSFUSION_CLIENT_SINGLEINSTANCE = "lsfusion.client.singleinstance";

    public static final String LSFUSION_CLIENT_CONNECTION_LOST_TIMEOUT = "lsfusion.client.connection.lost.timeout";

    public static final String LSFUSION_CLIENT_LOG_RMI = "lsfusion.client.log.rmi";

    public static final String LSFUSION_CLIENT_LOG_BASEDIR = "lsfusion.client.log.basedir";

    public static final String LSFUSION_CLIENT_PING_TIME = "lsfusion.client.pingTime";

    public static final String LSFUSION_CLIENT_BLOCKER_ACTIVATION_OFF = "lsfusion.client.blocker.activation.off";

    public static final String LSFUSION_CLIENT_ASYNC_TIMEOUT = "lsfusion.client.async.timeout";

    public static final int pullMessagesPeriod = Integer.parseInt(System.getProperty(LSFUSION_CLIENT_PING_TIME, "1000"));

    public final static boolean preventBlockerActivation = System.getProperty(StartupProperties.LSFUSION_CLIENT_BLOCKER_ACTIVATION_OFF) != null;

    public final static int rmiTimeout = Integer.valueOf(System.getProperty(LSFUSION_CLIENT_CONNECTION_LOST_TIMEOUT, "7200000"));
}
