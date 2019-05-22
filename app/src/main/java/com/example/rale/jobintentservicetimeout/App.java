package com.example.rale.jobintentservicetimeout;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {

    private static String TAG = "App";
    private static Context appContext;
    @Override
    public void onCreate() {
        Log.d(TAG, "Before super.onCreate");
        super.onCreate();
        Log.d(TAG, "After super.onCreate");
        appContext = this;
        Log.d(TAG, "After setting appContext");
    }

    public static Context getAppContext() {
        return appContext;
    }
}
