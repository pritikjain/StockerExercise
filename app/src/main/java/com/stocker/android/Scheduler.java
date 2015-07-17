package com.stocker.android;

import android.os.Handler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by pritijain on 05/05/15.
 */
public class Scheduler {

    private static Handler mHandler = new Handler();
    private static Executor mBackgroundExecutor = Executors.newSingleThreadScheduledExecutor();

    public static void runOnUiThread(Runnable r){
        mHandler.post(r);
    }

    public static void runInBackground(Runnable r){
        mBackgroundExecutor.execute(r);
    }
}
