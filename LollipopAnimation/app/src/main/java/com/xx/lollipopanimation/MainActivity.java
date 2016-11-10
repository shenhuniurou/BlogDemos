package com.xx.lollipopanimation;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends BaseActivity {

    @Override public int getLayoutByResId() {
        return R.layout.activity_main;
    }


    @Override public boolean hasBackButton() {
        return false;
    }


    @Override public void initViews() {

        /*final TextView tv10 = (TextView) findViewById(R.id.tv10);

        tv10.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent, options.toBundle());
            }
        });*/

        ImageView iv = (ImageView) findViewById(R.id.iv);
        Drawable drawable = iv.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        final ImageView ivAvatar = (ImageView) findViewById(R.id.ivAvatar);

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Path path = new Path();
                path.moveTo((v.getRight() - v.getLeft()) / 2, (v.getBottom() - v.getTop()) / 2);
                path.quadTo((v.getRight() - v.getLeft()) / 2, (v.getBottom() - v.getTop()) / 2, 0, 0);
                ObjectAnimator mAnimator = ObjectAnimator.ofFloat(ivAvatar, View.X, View.Y, path);
                Path p = new Path();
                p.lineTo(0.6f, 0.9f);
                p.lineTo(0.75f, 0.2f);
                p.lineTo(1f, 1f);
                mAnimator.setInterpolator(new PathInterpolator(p));
                mAnimator.setDuration(3000);
                mAnimator.start();
            }
        });

        TextView tv11 = (TextView) findViewById(R.id.tv11);
        StateListAnimator stateLAnim = AnimatorInflater.loadStateListAnimator(this,R.drawable.selector_for_button);
        tv11.setStateListAnimator(stateLAnim);

        tv11.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });


        // define a click listener
        /*ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                // create the transition animation - the images in the layouts
                // of both activities are defined with android:transitionName="robot"

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, Pair.create((View) ivAvatar, "robot"), Pair.create((View) tv10, "textview"));
                // start the new activity
                startActivity(intent, options.toBundle());
            }
        });*/

        /*final TextView tv9 = (TextView) findViewById(R.id.tv9);

        findViewById(R.id.tv9).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // get the center for the clipping circle
                int cx = (tv9.getRight() - tv9.getLeft()) / 2;
                int cy = (tv9.getBottom() - tv9.getTop()) / 2;

                // get the final radius for the clipping circle
                int initRadius = Math.max(tv9.getWidth(), tv9.getHeight());

                // create the animator for this view (the start radius is zero)
                final Animator anim = ViewAnimationUtils.createCircularReveal(tv9, cx, cy, initRadius, 0);

                anim.addListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        // make the view visible and start the animation
                        tv9.setVisibility(View.INVISIBLE);
                    }
                });
                anim.start();
            }
        });

        findViewById(R.id.content_main).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // get the center for the clipping circle
                int cx = (tv9.getRight() - tv9.getLeft()) / 2;
                int cy = (tv9.getBottom() - tv9.getTop()) / 2;

                // get the final radius for the clipping circle
                int finalRadius = Math.max(tv9.getWidth(), tv9.getHeight());

                // create the animator for this view (the start radius is zero)
                final Animator anim = ViewAnimationUtils.createCircularReveal(tv9, cx, cy, 0, finalRadius);

                tv9.setVisibility(View.VISIBLE);

                anim.start();
            }
        });*/

        /*findViewById(R.id.tv1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv5).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv6).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv7).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });

        findViewById(R.id.tv8).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });*/
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
