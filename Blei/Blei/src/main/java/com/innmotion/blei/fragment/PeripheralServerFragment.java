package com.innmotion.blei.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.innmotion.blei.R;

import java.util.UUID;

/**
 * Created by greg on 9/12/13.
 */
public class PeripheralServerFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = PeripheralServerFragment.class.getSimpleName();
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_ENABLE_BT_DISCOVERY = 2;
    private TextView uuidView;
    private ToggleButton toggle;
    private BluetoothManager mManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer mGattServer;
    private UUID uuid;

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
        setupBLE();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcast_toggle:
                toggleBroadcast(toggle.isChecked());
                break;
        }

    }

    private void initUUID() {
        uuid = UUID.randomUUID();
        uuidView = (TextView)getActivity().findViewById(R.id.uuid);
        uuidView.setText(uuid.toString());
    }

    private void setupBLE() {
        // Use this check to determine whether BLE is supported on the device.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "Failed BLE");
            Toast.makeText(getActivity(), "(╯°□°）╯︵ ┻━┻) --- Looks like you don't have BLE!", Toast.LENGTH_LONG).show();
        } else {
            // Initialize Bluetooth adapter.
            mManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                // Request to enable bluetooth.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                setupGattServer();
            }
        }
    }

    private void setupGattServer() {
        mGattServer = mManager.openGattServer(getActivity(), new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                Log.wtf(TAG, "connection changed!");
            }
        });

        BluetoothGattService advertiseService = setupAdvertisingService();
        mGattServer.addService(advertiseService);
    }

    private BluetoothGattService setupAdvertisingService() {
        BluetoothGattService service = new BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic broadcastChar = new BluetoothGattCharacteristic(uuid, BluetoothGattCharacteristic.PROPERTY_BROADCAST, 0);
        service.addCharacteristic(broadcastChar);
        return service;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Log.wtf(TAG, "Started Bluetooth!");
            setupGattServer();
        } else if (requestCode == REQUEST_ENABLE_BT_DISCOVERY) {
            if(resultCode == Activity.RESULT_CANCELED) {
                toggle.setChecked(false);
            } else {
                Log.wtf(TAG, "Now in discoveryMode!");
            }
        }
    }

    private void toggleBroadcast(boolean on) {
        if (on){
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT_DISCOVERY);
        } else {
            Log.wtf(TAG, "Should stop discovery here?");
        }
    }
}
