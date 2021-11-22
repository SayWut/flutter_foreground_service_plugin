enum NotificationPriority {
  defaultPr,
  low,
  min,
  high,
  max,
}

extension PriorityExtension on NotificationPriority {
  int? get priority {
    switch (this) {
      case NotificationPriority.defaultPr:
        return 0;
      case NotificationPriority.low:
        return -1;
      case NotificationPriority.min:
        return -2;
      case NotificationPriority.high:
        return 1;
      case NotificationPriority.max:
        return 2;
      default:
        return null;
    }
  }
}
