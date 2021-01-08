# flutter_foreground_service_plugin

## What is this plugin?

This plugin lets you create a foreground service in Android.

### Tested devices:
- Android 8, OxygenOS 5.0.8
- Android 8 (emulator)
- Android 6 (emulator)

__Caution:__<br>
1. I have no idea what will happen if you try to run this plugin on IOS (I can't check this because I am not own any apple product its probably will crush your app)
2. Some android operation systems have a strong battery optimization which sometimes kills foreground services, to handel this the plugin will automatically restart the service

## What can you control?

1. Set the notification content:
    - title
    - body
    - icon
    - sub text
    - enable / disable sound
    - enable / disable vibration
    - color
2. Set the notification channel content:
    - id
    - name
    - description
    - importance
    - lockscreen visibility
3. Refresh the notification content
4. Execute a task inside the service (runs even when the app is closed)
5. Start service on boot automatically

App example:

<img src="https://raw.githubusercontent.com/SayWut/flutter_foreground_service_plugin/master/resources/app_example.gif" height=700 />