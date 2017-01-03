package com.xx.crash;

import android.app.Activity;

/**
 * Created by shenhuniurou
 * on 2017/1/3.
 */

public class PatchBaseActivity extends Activity {

    @Override final protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
