enum NotificationChannelImportance {
  unspecified,
  none,
  min,
  low,
  defaultIm,
  high,
  max,
}

extension ImportanceExtension on NotificationChannelImportance {
  int? get importance {
    switch (this) {
      case NotificationChannelImportance.unspecified:
        return -1000;
      case NotificationChannelImportance.none:
        return 0;
      case NotificationChannelImportance.min:
        return 1;
      case NotificationChannelImportance.low:
        return 2;
      case NotificationChannelImportance.defaultIm:
        return 3;
      case NotificationChannelImportance.high:
        return 4;
      case NotificationChannelImportance.max:
        return 5;
      default:
        return null;
    }
  }
}
