package com.saywut.flutter_foreground_service_plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
    // flutter background channel
    private static final String BACKGROUND_CHANNEL_NAME = "com.saywut.flutter_foreground_service_plugin/background_channel";
    private static final String BACKGROUND_CHANNEL_INITIALIZE = "backgroundChannelInitialize";

    // service intent action
    public static final String RESTART_FOREGROUND_SERVICE_ACTION = "RESTART_FOREGROUND_SERVICE_ACTION";

    // service commands
    public static final String START_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_SERVICE = "STOP_FOREGROUND_SERVICE";
    public static final String REFRESH_CONTENT = "REFRESH_CONTENT";
    public static final String START_TASK = "START_FOREGROUND_TASK";
    public static final String STOP_TASK = "STOP_FOREGROUND_TASK";

    // service properties
    private String action = START_SERVICE;
    private SharedPreferencesHandler preferencesHandler;
    private Timer taskTimer;
    private FlutterEngine engine;
    private MethodChannel androidToFlutterChannel;
    private PendingIntent mainActivityIntent;
    private long lastTimeTaskExecute;
    private long taskStartTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        preferencesHandler = new SharedPreferencesHandler(getApplicationContext());

        if (intent != null)
            action = intent.getAction();


        switch (action)
        {
            case START_SERVICE:
                mainActivityIntent = getLaunchPendingIntent();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundOreo();
                else
                    startForeground(1, buildNotification());

                if (preferencesHandler.get("isTaskRunning"))
                {
                    createFlutterEngineAndBackgroundChannel();
                    executeFlutterTaskCode();
                }

                return START_STICKY;
            case STOP_SERVICE:
                stopPeriodicTask();
                stopForeground(true);
                stopSelf();

                return START_NOT_STICKY;
            case REFRESH_CONTENT:
                startForeground(1, buildNotification());

                return START_STICKY;
            case START_TASK:
                createFlutterEngineAndBackgroundChannel();
                executeFlutterTaskCode();

                return START_STICKY;
            case STOP_TASK:
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
        final boolean notifEnableSound = preferencesHandler.get("notifEnableSound");
        final boolean notifEnableVibration = preferencesHandler.get("notifEnableVibration");
        final String channelID = preferencesHandler.get("channelID");
        final String channelNameText = preferencesHandler.get("channelNameText");
        final String channelDescriptionText = preferencesHandler.get("channelDescriptionText");
        final int channelImportance = preferencesHandler.get("channelImportance");
        final int channelLockscreenVisibility = preferencesHandler.get("channelLockscreenVisibility");

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
        // gets the notification params
        final String notifTitleText = preferencesHandler.get("notifTitleText");
        final String notifBodyText = preferencesHandler.get("notifBodyText");
        final String notifSubText = preferencesHandler.get("notifSubText");
        final int notifIconID = preferencesHandler.get("notifIconID");
        final int notifColor = preferencesHandler.get("notifColor");
        final boolean notifEnableSound = preferencesHandler.get("notifEnableSound");
        final boolean notifEnableVibration = preferencesHandler.get("notifEnableVibration");
        final int notifPriority = preferencesHandler.get("notifPriority");
        final String channelID = preferencesHandler.get("channelID");

        NotificationCompat.Builder notifBuild = new NotificationCompat.Builder(this, channelID)
                .setContentTitle(notifTitleText)
                .setSmallIcon(notifIconID)
                .setContentIntent(mainActivityIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(notifPriority)
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
     * creates the flutter engine and background channel that used to communicate with the flutter code
     * when the app is terminated
     */
    public void createFlutterEngineAndBackgroundChannel()
    {
        engine = new FlutterEngine(this);
        FlutterMain.ensureInitializationComplete(this, null);

        String pluginKey = "com.saywut.flutter_foreground_service_plugin.FlutterForegroundServicePlugin";
        FlutterForegroundServicePlugin.registerWith(new ShimPluginRegistry(engine).registrarFor(pluginKey));

        androidToFlutterChannel = new MethodChannel(engine.getDartExecutor(), BACKGROUND_CHANNEL_NAME);
        androidToFlutterChannel.setMethodCallHandler(FlutterForegroundService.this);
    }

    /**
     * Execute the flutter code of the periodic task function
     */
    public void executeFlutterTaskCode()
    {
        long rawTaskHandler = preferencesHandler.get("rawTaskHandler");
        FlutterCallbackInformation callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(rawTaskHandler);
        String dartBundlePath = FlutterMain.findAppBundlePath();

        engine.getDartExecutor().executeDartCallback(new DartExecutor.DartCallback(getAssets(), dartBundlePath, callbackInfo));
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
        if (taskTimer == null)
        {
            taskStartTime = System.currentTimeMillis();
            final Handler handler = new Handler(Looper.getMainLooper());

            taskTimer = new Timer();
            taskTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            lastTimeTaskExecute = System.currentTimeMillis();
                            androidToFlutterChannel.invokeMethod("invokeFlutterFunction", null);
                        }
                    });
                }
            }, delay, period);
        }
    }

    /**
     * stops the periodic task
     */
    public void stopPeriodicTask()
    {
        if (taskTimer != null)
        {
            taskTimer.cancel();
            taskTimer.purge();
            taskTimer = null;
        }

        // destroying the engine to clear some of the used memory
        if (engine != null)
        {
            engine.destroy();
            engine = null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // this is to check if the service got stopped by the system
        // if it is then restart it
        // you can read farther explanation in the RestartForegroundService class
        if (!action.equals(STOP_SERVICE))
        {
            if (taskTimer != null)
            {
                // if the task is running and the OS kills the service the task is stopped
                // this is to calculate the task delay on restart to continue from where it stopped
                long serviceStopTime = System.currentTimeMillis();
                long taskDelay = preferencesHandler.get("taskDelay");
                long taskPeriod = preferencesHandler.get("taskPeriod");

                long passedTaskTimeFromStart = serviceStopTime - taskStartTime;
                long passedTaskTimeFromDelay = taskDelay - passedTaskTimeFromStart;

                long passedTaskTimeFromLast = serviceStopTime - lastTimeTaskExecute;
                long passedTaskTimeFromPeriod = taskPeriod - passedTaskTimeFromLast;

                long restartServiceTaskDelay = passedTaskTimeFromDelay >= 0 ? passedTaskTimeFromDelay : passedTaskTimeFromPeriod;
                restartServiceTaskDelay = Math.max(restartServiceTaskDelay, 0);

                preferencesHandler.put("taskDelay", restartServiceTaskDelay);
                preferencesHandler.apply();
            }

            Intent restartForegroundServiceReceiver = new Intent(this, ForegroundServiceReceiver.class);
            restartForegroundServiceReceiver.setAction(RESTART_FOREGROUND_SERVICE_ACTION);
            sendBroadcast(restartForegroundServiceReceiver);
        }

        stopPeriodicTask();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result)
    {
        switch (call.method)
        {
            case BACKGROUND_CHANNEL_INITIALIZE:
                long taskDelay = preferencesHandler.get("taskDelay");
                long taskPeriod = preferencesHandler.get("taskPeriod");

                startPeriodicTask(taskDelay, taskPeriod);
                result.success(true);
                break;
            default:
                result.notImplemented();
                break;
        }
    }
}