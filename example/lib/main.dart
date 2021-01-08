import 'package:flutter/material.dart';
import 'package:flutter_foreground_service_plugin/flutter_foreground_service_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Builder(
          builder: (context) {
            return Center(
              child: Column(
                children: [
                  TextButton(
                    child: Text('Start service'),
                    onPressed: () async {
                      await FlutterForegroundServicePlugin
                          .startForegroundService(
                        notificationContent: NotificationContent(
                          iconName: 'ic_launcher',
                          titleText: 'Title Text',
                          color: Colors.red,
                          priority: NotificationPriority.high,
                        ),
                        notificationChannelContent: NotificationChannelContent(
                          id: 'some_id',
                          nameText: 'settings title',
                          descriptionText: 'settings description text',
                        ),
                        isStartOnBoot: true,
                      );
                    },
                  ),
                  TextButton(
                    child: Text('Stop service'),
                    onPressed: () async {
                      await FlutterForegroundServicePlugin
                          .stopForegroundService();
                    },
                  ),
                  TextButton(
                    child: Text('Is service running'),
                    onPressed: () async {
                      var isRunning = await FlutterForegroundServicePlugin
                          .isForegroundServiceRunning();
                      print(isRunning);
                      var snackbar = SnackBar(
                        content: Text('$isRunning'),
                        duration: Duration(milliseconds: 500),
                      );
                      Scaffold.of(context).showSnackBar(snackbar);
                    },
                  ),
                  TextButton(
                    child: Text('Start task'),
                    onPressed: () async {
                      await FlutterForegroundServicePlugin.startPeriodicTask(
                        periodicTaskFun: periodicTaskFun,
                        period: const Duration(minutes: 20),
                      );
                    },
                  ),
                  TextButton(
                    child: Text('Stop task'),
                    onPressed: () async {
                      await FlutterForegroundServicePlugin.stopPeriodicTask();
                    },
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

void periodicTaskFun() {
  FlutterForegroundServicePlugin.executeTask(() async {
    // this will refresh the notification content each time the task is fire
    // if you want to refresh the notification content too each time
    // so don't set a low period duretion because android isn't handling it very well
    await FlutterForegroundServicePlugin.refreshForegroundServiceContent(
      notificationContent: NotificationContent(
        iconName: 'ic_launcher',
        titleText: 'Title Text',
        bodyText: '${DateTime.now()}',
        subText: 'subText',
        color: Colors.red,
      ),
    );

    print(DateTime.now());
  });
}
