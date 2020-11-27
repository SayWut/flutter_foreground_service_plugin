package com.saywut.flutter_foreground_service_plugin;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FlutterForegroundServiceChannelHandler implements MethodChannel.MethodCallHandler
{
    private static final String START_FOREGROUND_SERVICE = "startForegroundService";
    private static final String STOP_FOREGROUND_SERVICE = "stopForegroundService";
    private static final String REFRESH_FOREGROUND_SERVICE_NOTIFICATION_CONTENT = "refreshForegroundServiceNotificationContent";
    private static final String IS_FOREGROUND_SERVICE_RUNNING = "isForegroundServiceRunning";
    private static final String START_PERIODIC_TASK = "startPeriodicTask";
    private static final String STOP_PERIODIC_TASK = "stopPeriodicTask";

    private Context context;

    public FlutterForegroundServiceChannelHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result)
    {
        switch (call.method)
        {
            case START_FOREGROUND_SERVICE:
                if (isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceRunningError(result);
                    break;
                }

                String notifTitleText = call.argument("notifTitleText");
                String notifBodyText = call.argument("notifBodyText");
                String notifSubText = call.argument("notifSubText");
                String notifIconName = call.argument("notifIconName");
                int notifColor = call.argument("notifColor");
                boolean notifEnableSound = call.argument("notifEnableSound");
                boolean notifEnableVibration = call.argument("notifEnableVibration");
                String channelID = call.argument("channelID");
                String channelNameText = call.argument("channelNameText");
                String channelDescriptionText = call.argument("channelDescriptionText");
                int channelImportance = call.argument("channelImportance");
                int channelLockscreenVisibility = call.argument("channelLockscreenVisibility");

                int notifIconID = getResourceID(notifIconName);
                if (notifIconID == 0)
                {
                    FlutterForegroundServiceError.fileNotFoundError(result, notifIconName);
                    break;
                }

                Intent startServiceIntent = new Intent(context, FlutterForegroundService.class);
                startServiceIntent.setAction(FlutterForegroundService.START_SERVICE);
                startServiceIntent.putExtra("notifTitleText", notifTitleText);
                startServiceIntent.putExtra("notifBodyText", notifBodyText);
                startServiceIntent.putExtra("notifSubText", notifSubText);
                startServiceIntent.putExtra("notifIconID", notifIconID);
                startServiceIntent.putExtra("notifColor", notifColor);
                startServiceIntent.putExtra("notifEnableSound", notifEnableSound);
                startServiceIntent.putExtra("notifEnableVibration", notifEnableVibration);
                startServiceIntent.putExtra("channelID", channelID);
                startServiceIntent.putExtra("channelNameText", channelNameText);
                startServiceIntent.putExtra("channelDescriptionText", channelDescriptionText);
                startServiceIntent.putExtra("channelImportance", channelImportance);
                startServiceIntent.putExtra("channelLockscreenVisibility", channelLockscreenVisibility);
                startServiceIntent.putExtra("isTaskRunning", false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(startServiceIntent);
                else
                    context.startService(startServiceIntent);

                result.success(true);
                break;
            case STOP_FOREGROUND_SERVICE:
                if (!isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceNotRunningError(result);
                    break;
                }

                Intent stopServiceIntent = new Intent(context, FlutterForegroundService.class);
                stopServiceIntent.setAction(FlutterForegroundService.STOP_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(stopServiceIntent);
                else
                    context.startService(stopServiceIntent);

                result.success(true);
                break;
            case REFRESH_FOREGROUND_SERVICE_NOTIFICATION_CONTENT:
                if (!isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceNotRunningError(result);
                    break;
                }

                notifTitleText = call.argument("notifTitleText");
                notifBodyText = call.argument("notifBodyText");
                notifSubText = call.argument("notifSubText");
                notifIconName = call.argument("notifIconName");
                notifColor = call.argument("notifColor");
                notifEnableSound = call.argument("notifEnableSound");
                notifEnableVibration = call.argument("notifEnableVibration");

                notifIconID = getResourceID(notifIconName);
                if (notifIconID == 0)
                {
                    FlutterForegroundServiceError.fileNotFoundError(result, notifIconName);
                    break;
                }

                Intent refreshForegroundServiceNotifContent = new Intent(context, FlutterForegroundService.class);
                refreshForegroundServiceNotifContent.setAction(FlutterForegroundService.REFRESH_CONTENT);
                refreshForegroundServiceNotifContent.putExtra("notifTitleText", notifTitleText);
                refreshForegroundServiceNotifContent.putExtra("notifBodyText", notifBodyText);
                refreshForegroundServiceNotifContent.putExtra("notifSubText", notifSubText);
                refreshForegroundServiceNotifContent.putExtra("notifIconID", notifIconID);
                refreshForegroundServiceNotifContent.putExtra("notifColor", notifColor);
                refreshForegroundServiceNotifContent.putExtra("notifEnableSound", notifEnableSound);
                refreshForegroundServiceNotifContent.putExtra("notifEnableVibration", notifEnableVibration);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(refreshForegroundServiceNotifContent);
                else
                    context.startService(refreshForegroundServiceNotifContent);
                break;

            case IS_FOREGROUND_SERVICE_RUNNING:
                final boolean isRunning = isServiceRunning(FlutterForegroundService.class);
                result.success(isRunning);
                break;
            case START_PERIODIC_TASK:
                if (!isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceNotRunningError(result);
                    break;
                }

                final long delay = Long.parseLong(call.argument("taskDelay") + "");
                final long period = Long.parseLong(call.argument("taskPeriod") + "");
                final long rawTaskHandler = Long.parseLong(call.argument("rawTaskHandler") + "");

                Intent startTaskIntent = new Intent(context, FlutterForegroundService.class);
                startTaskIntent.setAction(FlutterForegroundService.START_TASK);
                startTaskIntent.putExtra("isTaskRunning", false);
                startTaskIntent.putExtra("taskDelay", delay);
                startTaskIntent.putExtra("taskPeriod", period);
                startTaskIntent.putExtra("rawTaskHandler", rawTaskHandler);

                context.startService(startTaskIntent);

                result.success(true);
                break;
            case STOP_PERIODIC_TASK:
                if (!isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceNotRunningError(result);
                    break;
                }

                Intent stopTaskIntent = new Intent(context, FlutterForegroundService.class);
                stopTaskIntent.setAction(FlutterForegroundService.STOP_TASK);

                context.startService(stopTaskIntent);

                result.success(true);
                break;
            default:
                result.notImplemented();
        }
    }

    /**
     * gets the resource id from the drawable folder or mipmap folder
     * checks first in the drawable folder if nothing is found checks in the mipmap folder
     *
     * @param resourceName the name of the resource
     * @return returns the id of the resource if nothing found returns 0
     */
    public int getResourceID(String resourceName)
    {
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        if (id == 0)
            id = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());

        return id;
    }

    /**
     * checks whether a service is already running
     *
     * @param serviceClass the service class name
     * @return true if running, false otherwise
     */
    private boolean isServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;

        return false;
    }


}