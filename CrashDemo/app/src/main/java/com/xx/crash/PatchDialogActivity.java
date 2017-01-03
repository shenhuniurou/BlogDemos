package com.xx.crash;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by shenhuniurou
 * on 2017/1/3.
 */

public class PatchDialogActivity extends PatchBaseActivity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_ULTIMATE_MESSAGE = "extra_ultimate_message";

    private String title, ultimateMessage;

    public static Intent newIntent(Context context, String title, String ultimateMessage) {

        Intent intent = new Intent();
        intent.setClass(context, PatchDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ULTIMATE_MESSAGE, ultimateMessage);
        return intent;
    }


    private void parseIntent(Intent intent) {
        title = intent.getStringExtra(EXTRA_TITLE);
        ultimateMessage = intent.getStringExtra(EXTRA_ULTIMATE_MESSAGE);
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        parseIntent(getIntent());
        if (ultimateMessage == null) {
            ultimateMessage = getString(R.string.error_message);
        }
        if (title == null) {
            title = getString(R.string.error_title);
        }
        ultimateSolution();
    }


    private void ultimateSolution() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
            .setTitle(title)
            .setMessage(ultimateMessage)
            .setCancelable(true)
            .setIcon(R.drawable.ic_error)
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override public void onCancel(DialogInterface dialog) {
                    finish();
                }
            })
            .setNegativeButton("退出", new DialogInterface.OnClickListener() {

                @Override public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            })
            .setPositiveButton(R.string.action_restart, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    restart();
                }
            }).show();
    }


    private void restart() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onDestroy();
    }


    @Override public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
