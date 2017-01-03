package com.xx.dynamicload;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int BOTTOM_ITEM_TITLE_HOME_INDEX = 0;
    private static final int BOTTOM_ITEM_TITLE_CHAT_INDEX = 1;
    private static final int BOTTOM_ITEM_TITLE_PLATFORM_INDEX = 2;
    private static final int BOTTOM_ITEM_TITLE_DISCOVER_INDEX = 3;
    private static final int BOTTOM_ITEM_TITLE_ME_INDEX = 4;

    Toolbar toolbar;

    private BottomBar mBottomBar;
    private MainHomePageFragment mMainHomePageFragment;
    private MainChatPageFragment mMainChatPageFragment;
    private MainSquarePageFragment mMainSquarePageFragment;
    private MainDiscoverPageFragment mMainDiscoverPageFragment;
    private MainMinePageFragment mMainMinePageFragment;
    private int mCurrentFragmentIndex = 0;
    private boolean mHomeFragmentAdded = false;
    private boolean mChatFragmentAdded = false;
    private boolean mPlatformFragmentAdded = false;
    private boolean mDiscoverFragmentAdded = false;
    private boolean mMeFragmentAdded = false;
    private String SDCARD_FOLDER;
    private String ROOT_FOLDER;
    private String DOWNLOAD_FOLDER;
    private String pluginPath;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.main_home);
        setSupportActionBar(toolbar);

        //初始化应用文件夹
        SDCARD_FOLDER = Environment.getExternalStorageDirectory().toString();
        ROOT_FOLDER = SDCARD_FOLDER + "/dynamicload/";
        DOWNLOAD_FOLDER = ROOT_FOLDER + "download/";
        pluginPath = DOWNLOAD_FOLDER + "skin-plugin.apk";

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        File downloadFile = new File(DOWNLOAD_FOLDER);
        if (!downloadFile.exists())
            downloadFile.mkdirs();


        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.noNavBarGoodness();
        mBottomBar.useFixedMode();
        //不要缩放动画了
        mBottomBar.noScalingGoodness();

        if (Util.getPluginPackagename(this, pluginPath) == null) {
            BottomBarTab bottomBarTab1 = new BottomBarTab(R.drawable.selector_rb_home, R.string.main_home);
            BottomBarTab bottomBarTab2 = new BottomBarTab(R.drawable.selector_rb_chat, R.string.main_chat);
            BottomBarTab bottomBarTab3 = new BottomBarTab(R.drawable.selector_rb_square, R.string.main_square);
            BottomBarTab bottomBarTab4 = new BottomBarTab(R.drawable.selector_rb_discover, R.string.main_discover);
            BottomBarTab bottomBarTab5 = new BottomBarTab(R.drawable.selector_rb_mine, R.string.main_mine);
            mBottomBar.setItems(bottomBarTab1, bottomBarTab2, bottomBarTab3, bottomBarTab4, bottomBarTab5);
        } else {
            Drawable homeDraw = Util.getPluginResources(this, pluginPath).getDrawable(Util.getResId(this, pluginPath, Util.getPluginPackagename(this, pluginPath), "selector_rb_home"));
            BottomBarTab bottomBarTab1 = new BottomBarTab(homeDraw, R.string.main_home);
            Drawable chatDraw = Util.getPluginResources(this, pluginPath).getDrawable(Util.getResId(this, pluginPath, Util.getPluginPackagename(this, pluginPath), "selector_rb_chat"));
            BottomBarTab bottomBarTab2 = new BottomBarTab(chatDraw, R.string.main_chat);
            Drawable squareDraw = Util.getPluginResources(this, pluginPath).getDrawable(Util.getResId(this, pluginPath, Util.getPluginPackagename(this, pluginPath), "selector_rb_square"));
            BottomBarTab bottomBarTab3 = new BottomBarTab(squareDraw, R.string.main_square);
            Drawable discoverDraw = Util.getPluginResources(this, pluginPath).getDrawable(Util.getResId(this, pluginPath, Util.getPluginPackagename(this, pluginPath), "selector_rb_discover"));
            BottomBarTab bottomBarTab4 = new BottomBarTab(discoverDraw, R.string.main_discover);
            Drawable mineDraw = Util.getPluginResources(this, pluginPath).getDrawable(Util.getResId(this, pluginPath, Util.getPluginPackagename(this, pluginPath), "selector_rb_mine"));
            BottomBarTab bottomBarTab5 = new BottomBarTab(mineDraw, R.string.main_mine);
            mBottomBar.setItems(bottomBarTab1, bottomBarTab2, bottomBarTab3, bottomBarTab4, bottomBarTab5);
        }

        mBottomBar.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position) {
                    case BOTTOM_ITEM_TITLE_HOME_INDEX:
                        switchToFragment(BOTTOM_ITEM_TITLE_HOME_INDEX);
                        break;
                    case BOTTOM_ITEM_TITLE_CHAT_INDEX:
                        switchToFragment(BOTTOM_ITEM_TITLE_CHAT_INDEX);
                        break;
                    case BOTTOM_ITEM_TITLE_PLATFORM_INDEX:
                        switchToFragment(BOTTOM_ITEM_TITLE_PLATFORM_INDEX);
                        break;
                    case BOTTOM_ITEM_TITLE_DISCOVER_INDEX:
                        switchToFragment(BOTTOM_ITEM_TITLE_DISCOVER_INDEX);
                        break;
                    case BOTTOM_ITEM_TITLE_ME_INDEX:
                        switchToFragment(BOTTOM_ITEM_TITLE_ME_INDEX);
                        break;
                    default:
                        break;
                }
            }


            @Override
            public void onTabReSelected(int position) {}
        });
        
        mBottomBar.mapColorForTab(BOTTOM_ITEM_TITLE_HOME_INDEX, ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.mapColorForTab(BOTTOM_ITEM_TITLE_CHAT_INDEX, ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.mapColorForTab(BOTTOM_ITEM_TITLE_PLATFORM_INDEX, ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.mapColorForTab(BOTTOM_ITEM_TITLE_DISCOVER_INDEX, ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.mapColorForTab(BOTTOM_ITEM_TITLE_ME_INDEX, ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.setActiveTabColor("#009688");
    }

    private void switchToFragment(int i) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (mCurrentFragmentIndex == i) {
            fragmentTransaction.add(R.id.fragmentContainer, getFragment(i));
            fragmentTransaction.commitAllowingStateLoss();
            mCurrentFragmentIndex = i;
            if (i == BOTTOM_ITEM_TITLE_HOME_INDEX) {
                mHomeFragmentAdded = true;
            }
            return;
        }

        fragmentTransaction.hide(getFragment(mCurrentFragmentIndex));

        switch (i) {
            case BOTTOM_ITEM_TITLE_HOME_INDEX:
                toolbar.setTitle(R.string.main_home);
                if (mHomeFragmentAdded) {
                    fragmentTransaction.show(getFragment(i));
                } else {
                    fragmentTransaction.add(R.id.fragmentContainer, getFragment(i));
                    mHomeFragmentAdded = true;
                }
                break;
            case BOTTOM_ITEM_TITLE_CHAT_INDEX:
                toolbar.setTitle(R.string.main_chat);
                if (mChatFragmentAdded) {
                    fragmentTransaction.show(getFragment(i));
                } else {
                    fragmentTransaction.add(R.id.fragmentContainer, getFragment(i));
                    mChatFragmentAdded = true;
                }
                break;
            case BOTTOM_ITEM_TITLE_PLATFORM_INDEX:
                toolbar.setTitle(R.string.main_square);
                if (mPlatformFragmentAdded) {
                    fragmentTransaction.show(getFragment(i));
                } else {
                    fragmentTransaction.add(R.id.fragmentContainer, getFragment(i));
                    mPlatformFragmentAdded = true;
                }
                break;
            case BOTTOM_ITEM_TITLE_DISCOVER_INDEX:
                toolbar.setTitle(R.string.main_discover);
                if (mDiscoverFragmentAdded) {
                    fragmentTransaction.show(getFragment(i));
                } else {
                    fragmentTransaction.add(R.id.fragmentContainer, getFragment(i));
                    mDiscoverFragmentAdded = true;
                }
                break;
            case BOTTOM_ITEM_TITLE_ME_INDEX:
                toolbar.setTitle(R.string.main_mine);
                if (mMeFragmentAdded) {
                    fragmentTransaction.show(getFragment(i));
                } else {
                    fragmentTransaction.add(R.id.fragmentContainer, getFragment(i));
                    mMeFragmentAdded = true;
                }
                break;
            default:
                break;
        }

        fragmentTransaction.commitAllowingStateLoss();
        mCurrentFragmentIndex = i;
    }

    private Fragment getFragment(int menuItemId) {
        switch (menuItemId) {
            case BOTTOM_ITEM_TITLE_HOME_INDEX:
                if (mMainHomePageFragment == null) {
                    mMainHomePageFragment = new MainHomePageFragment();
                }
                return mMainHomePageFragment;
            case BOTTOM_ITEM_TITLE_CHAT_INDEX:
                if (mMainChatPageFragment == null) {
                    mMainChatPageFragment = new MainChatPageFragment();
                }
                return mMainChatPageFragment;
            case BOTTOM_ITEM_TITLE_PLATFORM_INDEX:
                if (mMainSquarePageFragment == null) {
                    mMainSquarePageFragment = new MainSquarePageFragment();
                }
                return mMainSquarePageFragment;
            case BOTTOM_ITEM_TITLE_DISCOVER_INDEX:
                if (mMainDiscoverPageFragment == null) {
                    mMainDiscoverPageFragment = new MainDiscoverPageFragment();
                }
                return mMainDiscoverPageFragment;
            case BOTTOM_ITEM_TITLE_ME_INDEX:
                if (mMainMinePageFragment == null) {
                    mMainMinePageFragment = new MainMinePageFragment();
                }
                return mMainMinePageFragment;
            default:
                break;
        }
        return null;
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
