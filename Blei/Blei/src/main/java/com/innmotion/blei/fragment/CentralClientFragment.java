package com.innmotion.blei.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.innmotion.blei.BLEActivity;
import com.innmotion.blei.R;

/**
 * Created by greg on 9/12/13.
 */
public class CentralClientFragment extends Fragment{
    private final static String TAG = CentralClientFragment.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private TextView rssiView;
    private TextView deviceView;
    private BLEActivity activity;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_central_client, container, false);
        rssiView = (TextView) getActivity().findViewById(R.id.rssi);
        deviceView = (TextView) getActivity().findViewById(R.id.device);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (BLEActivity) getActivity();
        mBluetoothAdapter = activity.getmBluetoothAdapter();
        mHandler = new Handler();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (activity != null) {
            if (isVisibleToUser) {
                scanLeDevice(true);
            } else {
                scanLeDevice(false);
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        String msg = "Starting LE Scan for " + SCAN_PERIOD/1000 + " seconds";
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                Log.wtf(TAG, "Found device");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rssiView.setText(Integer.toString(rssi).toString());
                        StringBuilder deviceText = new StringBuilder(device.getName()).append("\n")
                                .append("Address: ").append(device.getAddress()).append("\n")
                                .append("Class: ").append(device.getBluetoothClass()).append("\n")
                                .append("Type: ").append(device.getType()).append("\n");
                        deviceView.setText(deviceText);
                    }
                });
            }
        };
}
