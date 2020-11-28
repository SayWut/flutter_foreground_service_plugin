package com.saywut.flutter_foreground_service_plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;

public class FlutterForegroundService extends Service implements MethodChannel.MethodCallHandler
{
    private static final String BACKGROUND_CHANNEL_NAME = "com.saywut.flutter_foreground_service_plugin/background_channel";
    private static final String BACKGROUND_CHANNEL_INITIALIZE = "backgroundChannelInitialize";

    // service commands
    public static final String START_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_SERVICE = "STOP_FOREGROUND_SERVICE";
    public static final String REFRESH_CONTENT = "REFRESH_CONTENT";
    public static final String START_TASK = "START_FOREGROUND_TASK";
    public static final String STOP_TASK = "STOP_FOREGROUND_TASK";

    private String action;
    private Bundle saveBundle;
    private Timer timer;
    private FlutterEngine engine;
    private MethodChannel androidToFlutterChannel;
    private PendingIntent mainActivityIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if (intent != null)
            action = intent.getAction();

        switch (action)
        {
            case START_SERVICE:
                mainActivityIntent = getLaunchPendingIntent();
                saveBundle = intent.getExtras();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundOreo();
                else
                    startForeground(1, buildNotification());

                if (saveBundle.getBoolean("isTaskRunning"))
                    setFlutterEngine();

                return START_STICKY;
            case STOP_SERVICE:
                stopPeriodicTask();
                stopForeground(true);
                stopSelf();

                return START_NOT_STICKY;
            case REFRESH_CONTENT:
                saveBundle.putAll(intent.getExtras());
                startForeground(1, buildNotification());

                return START_STICKY;
            case START_TASK:
                saveBundle.putAll(intent.getExtras());
                setFlutterEngine();

                return START_STICKY;
            case STOP_TASK:
                saveBundle.putBoolean("isTaskRunning", false);
                stopPeriodicTask();

                return START_STICKY;
            default:
                return START_NOT_STICKY;
        }
    }

    /**
     * starts foreground service for android 8 and above.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public void startForegroundOreo()
    {
        // gets the notification channel params
        final boolean notifEnableSound = saveBundle.getBoolean("notifEnableSound", false);
        final boolean notifEnableVibration = saveBundle.getBoolean("notifEnableVibration", false);
        final String channelID = saveBundle.getString("channelID");
        final String channelNameText = saveBundle.getString("channelNameText");
        final String channelDescriptionText = saveBundle.getString("channelDescriptionText");
        final int channelImportance = saveBundle.getInt("channelImportance", NotificationManager.IMPORTANCE_DEFAULT);
        final int channelLockscreenVisibility = saveBundle.getInt("channelLockscreenVisibility", Notification.VISIBILITY_SECRET);

        // creates a notification channel
        NotificationChannel notifChannel = new NotificationChannel(channelID, channelNameText, channelImportance);
        notifChannel.setShowBadge(false);
        notifChannel.setDescription(channelDescriptionText);
        notifChannel.setLockscreenVisibility(channelLockscreenVisibility);

        if (!notifEnableSound)
        {
            notifChannel.setSound(null, null);
        }
        if (!notifEnableVibration)
        {
            notifChannel.enableVibration(false);
            notifChannel.setVibrationPattern(null);
        }

        // sets the channel in the notification service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.createNotificationChannel(notifChannel);

        startForeground(1, buildNotification());
    }

    /**
     * builds a notification based on the received param
     *
     * @return the build notification
     */
    public Notification buildNotification()
    {
        final String notifTitleText = saveBundle.getString("notifTitleText");
        final String notifBodyText = saveBundle.getString("notifBodyText");
        final String notifSubText = saveBundle.getString("notifSubText");
        final int notifIconID = saveBundle.getInt("notifIconID", 0);
        final int notifColor = saveBundle.getInt("notifColor", -1);
        final boolean notifEnableSound = saveBundle.getBoolean("notifEnableSound", false);
        final boolean notifEnableVibration = saveBundle.getBoolean("notifEnableVibration", false);
        final String channelID = saveBundle.getString("channelID");

        NotificationCompat.Builder notifBuild = new NotificationCompat.Builder(this, channelID)
                .setContentTitle(notifTitleText)
                .setSmallIcon(notifIconID)
                .setContentIntent(mainActivityIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true);

        if (!notifEnableSound)
            notifBuild.setSound(null);
        if (!notifEnableVibration)
            notifBuild.setVibrate(null);
        if (notifColor != -1)
            notifBuild.setColor(notifColor);
        if (notifSubText != null)
            notifBuild.setSubText(notifSubText);
        if (notifBodyText != null)
        {
            notifBuild.setContentText(notifBodyText);
            notifBuild.setStyle(new NotificationCompat.BigTextStyle().bigText(notifBodyText));
        }

        return notifBuild.build();
    }

    /**
     * creates a pending intent that points to the main app activity
     *
     * @return pending intent that points to the main app activity
     */
    public PendingIntent getLaunchPendingIntent()
    {
        String packageName = getApplicationInfo().packageName;
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);

        PendingIntent launchPendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return launchPendingIntent;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * sets the flutter engine that used to communicate with the flutter code
     * when the app is terminated
     */
    public void setFlutterEngine()
    {
        engine = new FlutterEngine(this);
        FlutterMain.ensureInitializationComplete(this, null);

        long rawTaskHandler = saveBundle.getLong("rawTaskHandler");
        FlutterCallbackInformation callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(rawTaskHandler);
        String dartBundlePath = FlutterMain.findAppBundlePath();

        String pluginKey = "com.saywut.flutter_foreground_service_plugin.FlutterForegroundServicePlugin";
        FlutterForegroundServicePlugin.registerWith(new ShimPluginRegistry(engine).registrarFor(pluginKey));
        engine.getDartExecutor().executeDartCallback(new DartExecutor.DartCallback(getAssets(), dartBundlePath, callbackInfo));

        androidToFlutterChannel = new MethodChannel(engine.getDartExecutor(), BACKGROUND_CHANNEL_NAME);
        androidToFlutterChannel.setMethodCallHandler(FlutterForegroundService.this);
    }

    /**
     * Schedules the specified task for repeated fixed-delay execution, beginning after the
     * specified delay. Subsequent executions take place at approximately regular intervals
     * separated by the specified period.
     *
     * @param delay  sets the delay to wait before starting the task first time
     * @param period the period between each execute
     */
    public void startPeriodicTask(long delay, long period)
    {
        if (timer == null)
        {
            final Handler handler = new Handler(Looper.getMainLooper());

            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            androidToFlutterChannel.invokeMethod("invokeFlutterFunction", null);
                        }
                    });
                }
            }, delay, period);
        }
    }

    /**
     * stops the periodic
     */
    public void stopPeriodicTask()
    {
        if (timer != null)
        {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        stopPeriodicTask();

        // this is to check if the service got stopped by the system
        // if it is then restart it
        // you can read farther explanation in the RestartForegroundService class
        if (!action.equals(STOP_SERVICE))
        {
            Intent restartForegroundServiceReceiver = new Intent(this, RestartForegroundService.class);
            restartForegroundServiceReceiver.putExtras(saveBundle);
            sendBroadcast(restartForegroundServiceReceiver);
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result)
    {
        switch (call.method)
        {
            case BACKGROUND_CHANNEL_INITIALIZE:
                long taskDelay = saveBundle.getLong("taskDelay");
                long taskPeriod = saveBundle.getLong("taskPeriod");

                startPeriodicTask(taskDelay, taskPeriod);
                result.success(true);
                break;
            default:
                result.notImplemented();
                break;
        }
    }
}