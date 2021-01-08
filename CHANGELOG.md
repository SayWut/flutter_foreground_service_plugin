## 0.1.7 (08.01.2021)

### BUG FIX
* Fixed task delay to start after service restart [`#4`](https://github.com/SayWut/flutter_foreground_service_plugin/issues/4)
* Fixed waiting to `refreshForegroundServiceContent` to finish execute

## 0.1.6 (06.01.2021)

### Added
* Added start on boot option [`#2`](https://github.com/SayWut/flutter_foreground_service_plugin/issues/2)
* Added new property to the `NotificationContent`, can now set the notification priority 

### Changed
* `RestartForegroundService` now named `ForegroundServiceReceiver` 

## 0.1.5 (22.11.2020)

### BUG FIX
* Fixed memory overflow [`#1`](https://github.com/SayWut/flutter_foreground_service_plugin/issues/1)

## 0.1.4 (29.11.2020)

### BUG FIX
* Fixed service crush on auto restart when the OS kills the service
* The service will run for longer period before the OS kills it

## 0.1.3 (29.11.2020)

### BUG FIX (ya I am stupid)
* Fixed service crush on restart after the OS kills it
* Fixed service restart task after service got killed by the OS

## 0.1.2 (29.11.2020)

### BUG FIX
* Fixed service crush on restart after the OS kills it

## 0.1.1 (29.11.2020)

### BUG FIX
* Fixed `NotifiacationContant`'s `bodyText` prop, now it is not required it is optional
* Fixed `NotifiacationContant`'s `enableVibration` prop, (a semicolon sliped in LOL)
* Fixed `NotifiacationContant`'s `subText` and `bodyText` props, now if it is not set (`null`) then nothing is shown

## 0.1.0 (28.11.2020)

* First upload of the plugin
