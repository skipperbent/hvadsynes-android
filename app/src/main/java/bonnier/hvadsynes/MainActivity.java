package bonnier.hvadsynes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import bonnier.android.MD5Util;
import bonnier.android.models.CategoryModel;
import bonnier.android.tasks.DownloadImageTask;
import bonnier.hvadsynes.actionbar.fragments.TabQuestions;
import bonnier.hvadsynes.actionbar.fragments.TabActivity;
import bonnier.hvadsynes.actionbar.views.TabBarView;
import bonnier.hvadsynes.adapters.DrawerAdapter;
import bonnier.hvadsynes.adapters.MainActivityPagerAdapter;
import bonnier.hvadsynes.models.DrawerItemModel;
import bonnier.hvadsynes.models.MessageItemModel;

public class MainActivity extends AppCompatActivity{

    private String TAG = getClass().getName();

    //Interfaces
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TabBarView mTabBarView;
    private MainActivityPagerAdapter mainActivityPagerAdapter;
    private ViewPager mViewPager;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "MainScreenActivity: onCreate()");

        setContentView(R.layout.activity_main);

        settings = Settings.getInstance(getApplicationContext());

        setUpCustomTabs();
        setPagerListener();
        setUpNavigationDrawer();

        TextView name = (TextView)findViewById(R.id.drawerUserName);
        name.setText(settings.getName());

        int backgroundDrawable = (settings.getGender() > 1) ? R.drawable.question_bg_f : R.drawable.question_bg_m;
        LinearLayout profileInfo = (LinearLayout)findViewById(R.id.profileinfo);

        ImageView profilePicture = (ImageView)findViewById(R.id.picture);

        int noPhoto = (settings.getGender() > 1) ? R.drawable.female : R.drawable.male;
        profilePicture.setImageResource(noPhoto);

        // TODO: store images on device
        if (profilePicture != null && settings.getEmail() != "") {
            String url = "http://www.gravatar.com/avatar/" + MD5Util.md5(settings.getEmail()) + "?s=80&d=404";
            new DownloadImageTask(profilePicture, false).execute(url);
        }

        profileInfo.setBackground(ContextCompat.getDrawable(getApplicationContext(), backgroundDrawable));

        TextView info = (TextView)findViewById(R.id.drawerUserInfo);
        info.setText(String.format("%s %s", (settings.getGender() > 1) ? "K" : "M", settings.getAge()));
    }

    private void setUpCustomTabs() {
        LayoutInflater mLayoutInflater = this.getLayoutInflater();

        View customTabView = mLayoutInflater.inflate(R.layout.custom_tab_view, null);
        mTabBarView = (TabBarView) customTabView.findViewById(R.id.customTabBar);
        mTabBarView.setStripHeight(6);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setCustomView(customTabView);

        mainActivityPagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager());

        mainActivityPagerAdapter.addFragment(new TabQuestions());
        mainActivityPagerAdapter.addFragment(new TabActivity());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mainActivityPagerAdapter);
        mTabBarView.setViewPager(mViewPager);
    }

    private void setUpNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        ListView menu = (ListView)mDrawerLayout.findViewById(R.id.drawerItems);

        DrawerAdapter drawerAdapter = new DrawerAdapter(getApplicationContext(), getLayoutInflater());

        if(settings.getCategories() != null) {
            // Add categories to drawer
            for (final CategoryModel category : settings.getCategories()) {
                DrawerItemModel item = new DrawerItemModel(category.getName());

                item.setEventListener(new DrawerItemModel.DrawerItemEventListener() {
                    @Override
                    public void onClick(DrawerItemModel item, int position) {
                        Intent categoryIntent = new Intent(getApplicationContext(), CategoryActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(CategoryActivity.ARG_CATEGORY, category);

                        categoryIntent.putExtras(bundle);

                        startActivity(categoryIntent);
                        mDrawerLayout.closeDrawers();
                    }
                });

                drawerAdapter.add(item);
            }
        }

        // Add settings to drawer
        DrawerItemModel settingsItem = new DrawerItemModel("Indstillinger");
        settingsItem.setEventListener(new DrawerItemModel.DrawerItemEventListener() {
            @Override
            public void onClick(DrawerItemModel item, int position) {
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                mDrawerLayout.closeDrawers();
            }
        });

        drawerAdapter.add(settingsItem);

        menu.setAdapter(drawerAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG,"Drawer Opened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "Drawer Closed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Hide message toolbar when opening drawer
                MessageToolbarPopup.GetInstance().hidePopup();

                super.onDrawerStateChanged(newState);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPagerListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTabBarView.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "Page: " + position);
                mTabBarView.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mTabBarView.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:

                // Back to first view if the back button is pressed
                if(mTabBarView.getSelectedTab() > 0) {
                    mTabBarView.setSelectedTab(0);
                    mViewPager.setCurrentItem(0);
                    return true;
                }

                // Close drawer on back if it's open as the last resort
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }
                break;
        }

        return super.onKeyUp(keyCode, event);
    }
}