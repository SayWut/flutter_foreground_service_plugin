package com.saywut.flutter_foreground_service_plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/*
 * this broadcast is used for restarting the foreground service
 * on some android systems the battery optimization is very aggressive
 * and kills services to save battery, so to keep this service alive we need to restart the
 * service when the OS decide to kills it
 */
public class ForegroundServiceReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferencesHandler preferences = new SharedPreferencesHandler(context);

        // checks if need to start the service
        // conditions:
        // if the OS decided to kill the services to save battery
        // if the service was running and the phone restart
        String intentAction = intent.getAction();
        boolean startService =
                intentAction.equals(FlutterForegroundService.RESTART_FOREGROUND_SERVICE_ACTION) ||
               (intentAction.equals(Intent.ACTION_BOOT_COMPLETED) && preferences.getDefault("isStartOnBoot", false) && preferences.getDefault("isServiceShouldRun", false));

        if (startService)
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
}