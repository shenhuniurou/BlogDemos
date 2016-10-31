package com.xx.realm;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by wxx on 2016/10/23.
 */

public class RealmApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
    }
}
