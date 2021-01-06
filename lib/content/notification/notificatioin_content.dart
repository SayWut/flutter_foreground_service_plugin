import 'package:flutter/material.dart';

import 'notification_priority.dart';

class NotificationContent {
  /// sets the title of the notification
  final String titleText;

  /// sets the notification text body
  final String bodyText;

  /// sets the sub text of the notification
  final String subText;

  /// the notification icon name to use.
  ///
  /// you need to put the icon in the drawable or mipmap res folders,
  /// the plugin searches first in the drawable folder and then in the mipmap folder
  /// if nothing is found throw an error
  final String iconName;

  /// enable or disable the notification sound once pop
  final bool enableSound;

  /// enable or disable the notification vibration once pop
  final bool enableVibration;

  /// the notification color
  /// if `null` the default color is used
  final Color color;

  /// the notification priority
  /// can't be `null`
  final NotificationPriority priority;

  /// used for setting the notification content
  /// for further reading click [here](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder)
  NotificationContent({
    @required this.titleText,
    @required this.iconName,
    this.bodyText,
    this.subText,
    this.enableSound = false,
    this.enableVibration = false,
    this.color,
    this.priority = NotificationPriority.defaultPr,
  })  : assert(titleText != null),
        assert(iconName != null),
        assert(priority != null);
}
