package com.saywut.flutter_foreground_service_plugin;

import io.flutter.plugin.common.MethodChannel;

public class FlutterForegroundServiceError
{
    public static void serviceRunningError(MethodChannel.Result result)
    {
        String errorCode = "0";
        String errorMessage = "ServiceRunningError";
        String errorDetails = "the service is already running";
        result.error(errorCode, errorMessage, errorDetails);
    }

    public static void serviceNotRunningError(MethodChannel.Result result)
    {
        String errorCode = "1";
        String errorMessage = "ServiceNotRunningError";
        String errorDetails = "the service has to run first to execute the requested command";
        result.error(errorCode, errorMessage, errorDetails);
    }

    public static void fileNotFoundError(MethodChannel.Result result, String fileName)
    {
        String errorCode = "2";
        String errorMessage = "FileNotFoundError: " + fileName;
        String errorDetails = String.format("can't find the file %s, check if you did put it inside the drawable or mipmap res folders", fileName);
        result.error(errorCode, errorMessage, errorDetails);
    }

    public static void taskRunningError(MethodChannel.Result result)
    {
        String errorCode = "3";
        String errorMessage = "TaskRunningError";
        String errorDetails = "the task is already running";
        result.error(errorCode, errorMessage, errorDetails);
    }

    public static void taskNotRunningError(MethodChannel.Result result)
    {
        String errorCode = "4";
        String errorMessage = "TaskNotRunningError";
        String errorDetails = "the task isn't running";
        result.error(errorCode, errorMessage, errorDetails);
    }
}
