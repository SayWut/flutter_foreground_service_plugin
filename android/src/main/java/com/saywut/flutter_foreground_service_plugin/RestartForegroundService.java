package com.saywut.flutter_foreground_service_plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * this broadcast is used for restarting the foreground
 * on some android systems the battery optimization is very aggressive
 * and kills services at random times so to keep it alive we need to catch that
 * kill and restart the service
 */
public class RestartForegroundService extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent startServiceIntent = new Intent(context, FlutterForegroundService.class);
        startServiceIntent.setAction(FlutterForegroundService.START_SERVICE);
        startServiceIntent.putExtras(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(startServiceIntent);
        else
            context.startService(startServiceIntent);
    }
}