package com.jenwis.websocket.logic;

import android.content.Context;

import com.jenwis.websocket.base.WebSocketDaemonThread;

import org.java_websocket.client.WebSocketClient;

/**
 * Created by zhengyuji on 9/12/14.
 */
public class WebSocketLogic {
    private WebSocketDaemonThread daemonThread;

    public void keepWebSocketAlive(Context context, WebSocketClient webSocketClient) {
        daemonThread = new WebSocketDaemonThread(context, webSocketClient);
        daemonThread.setDaemon(true);
        daemonThread.start();
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        if (daemonThread != null) {
            daemonThread.setWebSocketClient(webSocketClient);
        }
    }

    public void destroy() {
        if (daemonThread != null) {
            daemonThread.die();
            daemonThread = null;
        }
    }
}
