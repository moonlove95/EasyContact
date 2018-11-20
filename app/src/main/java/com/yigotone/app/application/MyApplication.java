package com.yigotone.app.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.library.utils.U;
import com.ebupt.ebauth.biz.EbAuthDelegate;
import com.ebupt.ebjar.EbDelegate;
import com.ebupt.ebjar.EbLoginDelegate;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yigotone.app.R;
import com.yigotone.app.ui.activity.MainActivity;
import com.yigotone.app.ui.call.CallActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ZMM on 2018/2/5.
 */

public class MyApplication extends MultiDexApplication {
    private static Context mAppContext;
    @SuppressLint("StaticFieldLeak") private static MyApplication instance;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver mOfflineReceiver;
    private List<Activity> activities = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
        Logger.addLogAdapter(new AndroidLogAdapter());
        U.init(this);

        EbLoginDelegate.setMiPushParams(getString(R.string.MiPush_AppId), getString(R.string.MiPush_AppKey));
        if (EbDelegate.init(this, CallActivity.class) == EbDelegate.InitStat.Meb_INIT_SUCCESS &&
                EbAuthDelegate.init(this) == EbAuthDelegate.InitStat.Meb_INIT_SUCCESS) {
            Log.i("EbDelegate ", "sdk init ok");
            //注册一个鉴权下线广播
            broadcastManager = LocalBroadcastManager.getInstance(this);
            if (mOfflineReceiver == null) {
                mOfflineReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        Intent tent = new Intent(MyApplication.this, MainActivity.class);
                        tent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(tent);
                    }
                };
            }
            broadcastManager.registerReceiver(mOfflineReceiver, new IntentFilter("mebofflinenotification"));
        } else {
            Log.i("EbDelegate ", "sdk init fail");
            return;
        }
    }
    public static MyApplication getInstance() {
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;

    }
    public static Context getAppContext() {
        return mAppContext;
    }

    public void addActivity(Activity activity) {
        if (!activities.contains(activity)) {
            activities.add(activity);
        }
    }

    public void exit() {
        for (Activity activity : activities) {
            activity.finish();
        }
       // System.exit(0);
    }

}
