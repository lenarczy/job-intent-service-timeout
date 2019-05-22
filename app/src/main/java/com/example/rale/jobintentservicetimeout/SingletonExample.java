package com.example.rale.jobintentservicetimeout;

import android.util.Log;

public class SingletonExample {
    private static final String TAG = "SingletonExample";

    private static final class SingletonHolder {
        private static final BusySingleton INSTANCE = new BusySingleton();
    }

    public static BusySingleton getBusySingleton() {
        return SingletonHolder.INSTANCE;
    }

    public static class BusySingleton {
        private BusySingleton() {
            Log.d(TAG, "Create of Singleton with context " + App.getAppContext().getApplicationContext());
        }

        public String getInfo() {
            return "Info";
        }
    }

}
