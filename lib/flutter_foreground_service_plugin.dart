export 'content/notification/notificatioin_content.dart';
export 'content/notification/notification_priority.dart';
export 'content/notification_channel/notification_channel_content.dart';
export 'content/notification_channel/notification_channel_importance.dart';
export 'content/notification_channel/notification_channel_lockscreen_visibility.dart';

import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'content/notification/notificatioin_content.dart';
import 'content/notification/notification_priority.dart';
import 'content/notification_channel/notification_channel_content.dart';
import 'content/notification_channel/notification_channel_importance.dart';
import 'content/notification_channel/notification_channel_lockscreen_visibility.dart';

class FlutterForegroundServicePlugin {
  static const String _foregroundChannelName =
      'com.saywut.flutter_foreground_service_plugin/foreground_channel';
  static const String _backgroundChannelName =
      'com.saywut.flutter_foreground_service_plugin/background_channel';

  static const MethodChannel _foreground_channel =
      MethodChannel(_foregroundChannelName);
  static const MethodChannel _background_channel =
      MethodChannel(_backgroundChannelName);

  // foreground channel commands
  static const String _startService = "startForegroundService";
  static const String _stopService = "stopForegroundService";
  static const String _refreshForegroundServiceNotificationContent =
      "refreshForegroundServiceNotificationContent";
  static const String _isServiceRunning = "isForegroundServiceRunning";
  static const String _startPeriodicTask = "startPeriodicTask";
  static const String _stopPeriodicTask = "stopPeriodicTask";

  // background channel commands
  static const String _backgroundChannelInitialize =
      "backgroundChannelInitialize";

  /// Starts the foreground service with the passed [notificationContent] and [notificationChannelContent].
  ///
  /// With the [isStartOnBoot] option you can start the service when the device is finish to boot.
  /// the service will start only if the service was running before the device shutdown, if not the service will not start
  ///
  /// Android limitation:
  ///
  /// `After you create a notification channel, you cannot change the notification
  /// behaviors — the user has complete control at that point. Though you can still
  /// change a channel's name and description.`
  /// [Click here for further reading about the android notification channels](https://developer.android.com/training/notify-user/channels)
  static Future<void> startForegroundService({
    @required NotificationContent notificationContent,
    @required NotificationChannelContent notificationChannelContent,
    bool isStartOnBoot = false,
  }) async {
    assert(notificationContent != null);
    assert(notificationChannelContent != null);
    assert(isStartOnBoot != null);

    int notifColorValue = notificationContent.color != null
        ? notificationContent.color.red << 16 |
            notificationContent.color.green << 8 |
            notificationContent.color.blue
        : null;

    Map<String, dynamic> args = {
      'notifTitleText': notificationContent.titleText,
      'notifBodyText': notificationContent.bodyText,
      'notifSubText': notificationContent.subText,
      'notifIconName': notificationContent.iconName,
      'notifColor': notifColorValue ?? -1,
      'notifEnableSound': notificationContent.enableSound,
      'notifEnableVibration': notificationContent.enableVibration,
      'notifPriority': notificationContent.priority.priority,
      'channelID': notificationChannelContent.id,
      'channelNameText': notificationChannelContent.nameText,
      'channelDescriptionText': notificationChannelContent.descriptionText,
      'channelImportance': notificationChannelContent.importance.importance,
      'channelLockscreenVisibility':
          notificationChannelContent.lockscreenVisibility.visibility,
      'isStartOnBoot': isStartOnBoot,
    };

    await _foreground_channel.invokeMethod(_startService, args);
  }

  /// Stops the running service.
  /// If the service isn't running throwing an error
  static Future<void> stopForegroundService() async {
    await _foreground_channel.invokeMethod(_stopService);
  }

  static Future<void> refreshForegroundServiceContent({
    @required NotificationContent notificationContent,
  }) async {
    assert(notificationContent != null);

    int notifColorValue = notificationContent.color != null
        ? notificationContent.color.red << 16 |
            notificationContent.color.green << 8 |
            notificationContent.color.blue
        : null;

    Map<String, dynamic> args = {
      'notifTitleText': notificationContent.titleText,
      'notifBodyText': notificationContent.bodyText,
      'notifSubText': notificationContent.subText,
      'notifIconName': notificationContent.iconName,
      'notifColor': notifColorValue ?? -1,
      'notifEnableSound': notificationContent.enableSound,
      'notifEnableVibration': notificationContent.enableVibration,
    };

    await _foreground_channel.invokeMethod(
        _refreshForegroundServiceNotificationContent, args);
  }

  /// Checks wether the service is running or not
  /// if it is returns true, false otherwise
  static Future<bool> isForegroundServiceRunning() async {
    return await _foreground_channel.invokeMethod(_isServiceRunning);
  }

  /// Schedules the specified task for repeated fixed-delay execution,
  /// beginning after the specified [delay]. Subsequent executions take place at
  /// approximately regular intervals separated by the specified [period].
  ///
  /// the [delay] in milliseconds has to be bigger than or equals to 0
  ///
  /// the [period] in milliseconds has to be bigger then 0
  ///
  /// the [callbackDispatcher] is the function which be executed. The function
  /// must be a static or top-level method and must run the
  /// [FlutterForegroundServicePlugin.executeTask]
  ///
  /// example:
  ///
  /// ```dart
  /// void periodicTaskFun() {
  ///   FlutterForegroundServicePlugin.executeTask(() {
  ///     print('${DateTime.now()}');
  ///   });
  /// }
  /// ```
  ///
  /// If the service isn't running throwing an error.
  static Future<void> startPeriodicTask({
    Duration delay = const Duration(milliseconds: 0),
    Duration period = const Duration(milliseconds: 1),
    @required void Function() periodicTaskFun,
  }) async {
    assert(delay != null);
    assert(period != null);
    assert(delay.inMilliseconds >= 0);
    assert(period.inMilliseconds > 0);
    assert(periodicTaskFun != null);

    var rawTaskHandler =
        PluginUtilities.getCallbackHandle(periodicTaskFun).toRawHandle();

    Map<String, dynamic> args = {
      'taskDelay': delay.inMilliseconds,
      'taskPeriod': period.inMilliseconds,
      'rawTaskHandler': rawTaskHandler,
    };

    await _foreground_channel.invokeMethod(_startPeriodicTask, args);
  }

  /// Stops the periodic task
  /// If the service isn't running throwing an error
  static Future<void> stopPeriodicTask() async {
    await _foreground_channel.invokeMethod(_stopPeriodicTask);
  }

  /// A helper function to use inside [startPeriodicTask] function
  /// so you only need to implement the [task]
  static void executeTask(void Function() task) {
    WidgetsFlutterBinding.ensureInitialized();
    _background_channel.setMethodCallHandler((call) {
      task();
      return Future.value(true);
    });
    _background_channel.invokeMethod(_backgroundChannelInitialize);
  }
}
