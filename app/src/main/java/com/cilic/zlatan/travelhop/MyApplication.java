package com.cilic.zlatan.travelhop;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
        super.onCreate();
    }
}
