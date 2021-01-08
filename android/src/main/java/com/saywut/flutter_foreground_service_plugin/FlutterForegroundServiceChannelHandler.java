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

    private final Context context;

    public FlutterForegroundServiceChannelHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result)
    {
        SharedPreferencesHandler preferencesHandler = new SharedPreferencesHandler(context);
        Intent serviceAction = new Intent(context, FlutterForegroundService.class);

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
                int notifPriority = call.argument("notifPriority");
                String channelID = call.argument("channelID");
                String channelNameText = call.argument("channelNameText");
                String channelDescriptionText = call.argument("channelDescriptionText");
                int channelImportance = call.argument("channelImportance");
                int channelLockscreenVisibility = call.argument("channelLockscreenVisibility");
                boolean isStartOnBoot = call.argument("isStartOnBoot");

                int notifIconID = getResourceID(notifIconName);
                if (notifIconID == 0)
                {
                    FlutterForegroundServiceError.fileNotFoundError(result, notifIconName);
                    break;
                }

                serviceAction.setAction(FlutterForegroundService.START_SERVICE);

                preferencesHandler.put("notifTitleText", notifTitleText);
                preferencesHandler.put("notifBodyText", notifBodyText);
                preferencesHandler.put("notifSubText", notifSubText);
                preferencesHandler.put("notifIconID", notifIconID);
                preferencesHandler.put("notifColor", notifColor);
                preferencesHandler.put("notifEnableSound", notifEnableSound);
                preferencesHandler.put("notifEnableVibration", notifEnableVibration);
                preferencesHandler.put("notifPriority", notifPriority);
                preferencesHandler.put("channelID", channelID);
                preferencesHandler.put("channelNameText", channelNameText);
                preferencesHandler.put("channelDescriptionText", channelDescriptionText);
                preferencesHandler.put("channelImportance", channelImportance);
                preferencesHandler.put("channelLockscreenVisibility", channelLockscreenVisibility);
                preferencesHandler.put("isStartOnBoot", isStartOnBoot);
                preferencesHandler.put("isTaskRunning", false);
                preferencesHandler.put("isServiceShouldRun", true);
                preferencesHandler.apply();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(serviceAction);
                else
                    context.startService(serviceAction);

                result.success(true);
                break;
            case STOP_FOREGROUND_SERVICE:
                if (!isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceNotRunningError(result);
                    break;
                }

                preferencesHandler.put("isServiceShouldRun", false);
                preferencesHandler.apply();

                serviceAction.setAction(FlutterForegroundService.STOP_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(serviceAction);
                else
                    context.startService(serviceAction);

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

                serviceAction.setAction(FlutterForegroundService.REFRESH_CONTENT);
                preferencesHandler.put("notifTitleText", notifTitleText);
                preferencesHandler.put("notifBodyText", notifBodyText);
                preferencesHandler.put("notifSubText", notifSubText);
                preferencesHandler.put("notifIconID", notifIconID);
                preferencesHandler.put("notifColor", notifColor);
                preferencesHandler.put("notifEnableSound", notifEnableSound);
                preferencesHandler.put("notifEnableVibration", notifEnableVibration);
                preferencesHandler.apply();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(serviceAction);
                else
                    context.startService(serviceAction);

                result.success(true);
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

                if (preferencesHandler.get("isTaskRunning"))
                {
                    FlutterForegroundServiceError.taskRunningError(result);
                    break;
                }

                final long delay = Long.parseLong(call.argument("taskDelay") + "");
                final long period = Long.parseLong(call.argument("taskPeriod") + "");
                final long rawTaskHandler = Long.parseLong(call.argument("rawTaskHandler") + "");

                serviceAction.setAction(FlutterForegroundService.START_TASK);
                preferencesHandler.put("isTaskRunning", true);
                preferencesHandler.put("taskDelay", delay);
                preferencesHandler.put("taskPeriod", period);
                preferencesHandler.put("rawTaskHandler", rawTaskHandler);
                preferencesHandler.apply();

                context.startService(serviceAction);

                result.success(true);
                break;
            case STOP_PERIODIC_TASK:
                if (!isServiceRunning(FlutterForegroundService.class))
                {
                    FlutterForegroundServiceError.serviceNotRunningError(result);
                    break;
                }

                if (!(boolean) preferencesHandler.get("isTaskRunning"))
                {
                    FlutterForegroundServiceError.taskNotRunningError(result);
                    break;
                }

                serviceAction.setAction(FlutterForegroundService.STOP_TASK);
                preferencesHandler.put("isTaskRunning", false);
                preferencesHandler.apply();

                context.startService(serviceAction);

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