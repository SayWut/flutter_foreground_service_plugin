enum NotificationChannelLockscreenVisibility {
  public,
  private,
  secret,
}

extension LockscreenVisibilityExtension
    on NotificationChannelLockscreenVisibility {
  int? get visibility {
    switch (this) {
      case NotificationChannelLockscreenVisibility.public:
        return 1;
      case NotificationChannelLockscreenVisibility.private:
        return 0;
      case NotificationChannelLockscreenVisibility.secret:
        return -1;
      default:
        return null;
    }
  }
}
