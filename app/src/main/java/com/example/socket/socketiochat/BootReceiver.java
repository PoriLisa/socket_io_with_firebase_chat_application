package com.example.socket.socketiochat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.socket.services.SocketIOService;


public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(SocketIOService.class.getName());
        context.startService(serviceIntent);
    }
}
