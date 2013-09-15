package com.innmotion.blei;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import com.innmotion.blei.fragment.CentralClientFragment;
import com.innmotion.blei.fragment.PeripheralServerFragment;

public class BLEActivity extends FragmentActivity {
    private final static String TAG = BLEActivity.class.getSimpleName();
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_ENABLE_BT_DISCOVERY = 2;
    private final int POS_SERVER = 0;
    private final int POS_CLIENT = 1;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private PeripheralServerFragment peripheralServerFragment;
    private CentralClientFragment centralClientFragment;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean firstRun = true;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        prefs = getSharedPreferences("com.innmotion.blei", Context.MODE_PRIVATE);
        setupBLE();

        peripheralServerFragment = new PeripheralServerFragment();
        centralClientFragment = new CentralClientFragment();

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mPager.setCurrentItem(tab.getPosition());

//                if (tab.getPosition() == POS_SERVER) {
//                    centralClientFragment.onHidden();
//                    peripheralServerFragment.onShown();
//                } else if (tab.getPosition() == POS_CLIENT) {
//                    centralClientFragment.onShown();
//                    peripheralServerFragment.onHidden();
//                }

            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {  }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {  }
        };

        actionBar.addTab(
                actionBar.newTab()
                    .setText("Broadcast")
                    .setTabListener(tabListener)
        );

        actionBar.addTab(
                actionBar.newTab()
                        .setText("Listen")
                        .setTabListener(tabListener)
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.broadcast, menu);
        return true;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case POS_SERVER:
                    return peripheralServerFragment;
                case POS_CLIENT:
                    return centralClientFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void setupBLE() {
        // Use this check to determine whether BLE is supported on the device.
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "Failed BLE");
            Toast.makeText(this, "(╯°□°）╯︵ ┻━┻) --- Looks like you don't have BLE!", Toast.LENGTH_LONG).show();
        } else {
            // Initialize Bluetooth adapter.
            mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                // Request to enable bluetooth.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Started Bluetooth!");
        } else {
            Log.d(TAG, "Failed to start bluetooth.");
        }
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothManager getmBluetoothManager() {
        return mBluetoothManager;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }
}
