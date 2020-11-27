# flutter_foreground_service_plugin

<h2>What is this plugin?</h2>

----

This plugin lets you create a foreground service in Android.

__Caution:__<br>
I have no idea what will happen if you try to run this plugin on IOS

<h2>What can you control?</h2>

------

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

App example:

<img src="https://raw.githubusercontent.com/SayWut/flutter_foreground_service_plugin/master/resources/app_example.gif" height=700 />