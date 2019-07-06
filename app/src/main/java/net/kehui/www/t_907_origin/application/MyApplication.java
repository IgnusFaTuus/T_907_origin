package net.kehui.www.t_907_origin.application;

import android.app.Application;

import android.content.res.Configuration;

/**
 * @author IF
 */
public class MyApplication extends Application {
    public static MyApplication instances;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static MyApplication getInstances() {
        return instances;
    }

}
