package com.stocker.android;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Created by pritijain on 04/05/15.
 */
public class StockerApp extends Application {

    NetworkController mNetworkController;
    NotificationController mNotificationController;

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);

        mNetworkController = new NetworkController(this);
        mNetworkController.start();

        mNotificationController = new NotificationController(this);
        mNotificationController.start();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mNetworkController.stop();
        mNotificationController.stop();
        SugarContext.terminate();
    }
}
