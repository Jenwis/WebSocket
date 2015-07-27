package com.jenwis.websocket.base;

import android.content.Context;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

/**
 * Created by zhengyuji on 9/12/14.
 * 守护线程，在主线程（UI）线程销毁后，此线程才会被销毁
 * 定时发送心跳包，保证app的生命过程中，WebSocket不会挂掉
 */
public class WebSocketDaemonThread extends Thread {
    private Context context;

    public static final long INTERVAL_SEND_HEARTBEAT_PACKET = 3000;

    private WebSocketClient webSocketClient;
    private long interval = INTERVAL_SEND_HEARTBEAT_PACKET;
    private boolean keepAlive = true;
    private final static String TAG = LogUtils.makeLogTag(WebSocketDaemonThread.class);
    private boolean isLogout = false;

    public WebSocketDaemonThread(Context context, WebSocketClient webSocketClient) {
        this.context = context;
        this.webSocketClient = webSocketClient;
    }

    public WebSocketDaemonThread(Context context, WebSocketClient webSocketClient, long interval) {
        this.context = context;
        this.webSocketClient = webSocketClient;
        this.interval = interval;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void die() {
        keepAlive = false;
        isLogout = true;
    }

    @Override
    public void run() {
        while (keepAlive) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isLogout) return;
            //定时发送心跳包
            LogUtils.LogD(TAG, "try to send heartbeat packet");
            if (webSocketClient != null && webSocketClient.getConnection() != null && webSocketClient.getConnection().isOpen()) {
                try {
                    webSocketClient.send(WebSocketUtils.MSG_HEART_PACKAGE_CONTENT);
                } catch (WebsocketNotConnectedException e) {
                    LogUtils.LogE(TAG, "WebSocket is Not Connected");
                }

                LogUtils.LogD(TAG, "send heartbeat packet success!");
            } else {
                LogUtils.LogD(TAG, "SocketClient is dead!");
                WebSocketUtils.initInstanceIfNecessary(context).reLiveSocketClient();
                LogUtils.LogD(TAG, "SocketClient is reAlive!");
            }
        }
    }
}
