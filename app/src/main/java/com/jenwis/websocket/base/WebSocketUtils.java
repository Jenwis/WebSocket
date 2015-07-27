package com.jenwis.websocket.base;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.jenwis.websocket.logic.WebSocketLogic;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by zhengyuji on 9/13/14.
 *
 */
public class WebSocketUtils {
    public static final String TAG = LogUtils.makeLogTag(WebSocketUtils.class);
    public static final String MSG_HEART_PACKAGE_CONTENT = "ping";
    private static Context sContext;
    private static WebSocketUtils sInstance;
    public static String URI_WEB_SOCKET = "";
    private static WebSocketClient sWebSocketClient;
    private static ArrayList<WebSocketListener> sWebSocketListenerList = new ArrayList<WebSocketListener>();
    private static WebSocketLogic sWebSocketLogic;
    //待发送的消息
    private static String sMsg2Send = null;
    private static Handler sHandler = new Handler();

    private WebSocketUtils() {
    }

    public static WebSocketUtils initInstanceIfNecessary(Context context) {
        sContext = context;
        if (sInstance == null) {
            String url = "";
            try {
                url = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA).metaData.getString("websocket_ip");
                url = url.replace("http", "ws");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            URI_WEB_SOCKET = url + "/cloud/websocket/v1/";
            sInstance = new WebSocketUtils();
            sWebSocketLogic = new WebSocketLogic();
            activateSocketClient();
        }
        return sInstance;
    }

    public static void clearInstance() {
        if (sWebSocketLogic != null) {
            sWebSocketLogic.destroy();
        }
        if (sWebSocketClient != null) {
            sWebSocketClient.close();
            sWebSocketClient = null;
        }
        sInstance = null;
    }

    private static void activateSocketClient() {
        initWebSocketClient();
        sWebSocketLogic.keepWebSocketAlive(sContext, sWebSocketClient);
    }

    public static void reLiveSocketClient() {
        initWebSocketClient();
        sWebSocketLogic.setWebSocketClient(sWebSocketClient);
    }

    private static void initWebSocketClient() {
        try {
            LogUtils.LogD(TAG, "init");
            sWebSocketClient = new WebSocketClient(new URI(URI_WEB_SOCKET), new Draft_17()) {
                @Override
                public void onOpen(final ServerHandshake serverHandshake) {
                    LogUtils.LogD(TAG, "onOpen");
                    send(MSG_HEART_PACKAGE_CONTENT);

                    //发送待发送的消息
                    if (sMsg2Send != null) {
                        send(sMsg2Send);
                        sMsg2Send = null;
                    }
                }

                @Override
                public void onMessage(final String s) {
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < sWebSocketListenerList.size(); i++) {
                                WebSocketListener webSocketListener = sWebSocketListenerList.get(i);
                                if (webSocketListener != null) {
                                    webSocketListener.onMessage(s);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onClose(final int index, final String s, final boolean b) {
                    LogUtils.LogD(TAG, "onClose");
                }

                @Override
                public void onError(final Exception e) {
                    LogUtils.LogD(TAG, "onError");
                }
            };
            sWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void setWebSocketListener(WebSocketListener webSocketListener) {
        sWebSocketListenerList.add(webSocketListener);
    }

    public static void removeWebSocketListener(WebSocketListener webSocketListener) {
        if (sWebSocketListenerList.contains(webSocketListener)) {
            sWebSocketListenerList.remove(webSocketListener);
        }
    }


    public void send(String msg) {
        if (sWebSocketClient.getConnection() == null || !sWebSocketClient.getConnection().isOpen()) {
            reLiveSocketClient();

            //保存没发送成功的非心跳包消息
            if (!msg.equals(MSG_HEART_PACKAGE_CONTENT)) {
                sMsg2Send = msg;
            }
        } else {
            sWebSocketClient.send(msg);
        }
    }

    public interface WebSocketListener {
        //public void onOpen(ServerHandshake serverHandshake);

        public void onMessage(String msg);

        //public void onClose(int i, String s, boolean b);

        //public void onError(Exception e);
    }
}
