package com.innmotion.blei.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.innmotion.blei.R;

import java.util.List;
import java.util.UUID;

/**
 * Created by greg on 9/12/13.
 */
public class BroadcastFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = BroadcastFragment.class.getSimpleName();
    private final static int REQUEST_ENABLE_BT = 1;
    private TextView uuidView;
    private ToggleButton toggle;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer gattServer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_broadcast, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggle = (ToggleButton)getActivity().findViewById(R.id.broadcast_toggle);
        toggle.setOnClickListener(this);

        initUUID();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcast_toggle:
                setupBLE();
                break;
        }

    }

    private void initUUID() {
        UUID uuid = UUID.randomUUID();
        uuidView = (TextView)getActivity().findViewById(R.id.uuid);
        uuidView.setText(uuid.toString());
    }

    private void setupBLE() {
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.wtf(TAG, "Failed BLE");
            Toast.makeText(getActivity(), "(╯°□°）╯︵ ┻━┻) --- Looks like you don't have BLE!", Toast.LENGTH_LONG).show();
        } else {
            Log.wtf(TAG, "Initializing GATT Server");
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            gattServer = bluetoothManager.openGattServer(getActivity(), new BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    super.onConnectionStateChange(device, status, newState);
                    Log.wtf(TAG, "connection changed!");
                }
            });
            gattServer.getServices();
        }
    }

    private void setup() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Log.wtf(TAG, "Started Bluetooth!");
            setup();
        }
    }
}
