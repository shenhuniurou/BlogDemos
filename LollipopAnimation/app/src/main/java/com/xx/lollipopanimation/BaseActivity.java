package com.xx.lollipopanimation;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

abstract class BaseActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (hasBackButton()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FrameLayout container = (FrameLayout) findViewById(R.id.content_base);
        container.addView(LayoutInflater.from(this).inflate(getLayoutByResId(), null));
        initViews();
    }

    public abstract int getLayoutByResId();

    public abstract void initViews();

    public abstract boolean hasBackButton();


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
