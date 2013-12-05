package com.innmotion.blei.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.innmotion.blei.BLEActivity;
import com.innmotion.blei.R;

import java.util.UUID;

/**
 * Created by greg on 9/12/13.
 */
public class PeripheralServerFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = PeripheralServerFragment.class.getSimpleName();
    private TextView uuidView;
    private ToggleButton gattToggle;
    private ToggleButton connectToggle;
    private EditText inputAddress;
    private BluetoothGattServer mGattServer;
    private UUID uuid;
    private BLEActivity activity;

    private BluetoothDevice connectedDevice;
    private BluetoothGatt connectedGatt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_peripheral_server, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (BLEActivity) getActivity();
        gattToggle = (ToggleButton)activity.findViewById(R.id.broadcast_toggle);
        gattToggle.setOnClickListener(this);
        connectToggle = (ToggleButton)activity.findViewById(R.id.connect_toggle);
        connectToggle.setOnClickListener(this);
        inputAddress = (EditText)getActivity().findViewById(R.id.address_input);

        String inputCachedText = activity.getPrefs().getString("ADDRESS", null);
        if (inputCachedText != null) {
            inputAddress.setText(inputCachedText);
        }
        initUUID();
        String address = BluetoothAdapter.getDefaultAdapter().getAddress();
        TextView addressView = (TextView) activity.findViewById(R.id.address);
        addressView.setText(address);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {

        } else {
            if (gattToggle != null && gattToggle.isChecked()) {
                Toast.makeText(getActivity(), "Stopping Gatt server...", Toast.LENGTH_LONG).show();
                toggleBroadcast(false);
                gattToggle.setChecked(false);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcast_toggle:
                toggleBroadcast(gattToggle.isChecked());
                break;
            case R.id.connect_toggle:
                toggleConnect(v, (connectToggle).isChecked());
                break;
        }
    }

    private void initUUID() {
        uuid = UUID.randomUUID();
        uuidView = (TextView)getActivity().findViewById(R.id.uuid);
        uuidView.setText(uuid.toString());
    }

    private void setupGattServer() {

        Toast.makeText(getActivity(), "Starting Gatt server...", Toast.LENGTH_LONG).show();
        mGattServer = activity.getmBluetoothManager().openGattServer(getActivity(), new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                Log.wtf(TAG, "This gatt server -Connection state changed!");
                Log.wtf(TAG, activity.getString(R.string.gatt_device_status) + status);
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.wtf(TAG, "This gatt server - " + device.getAddress() + " connected");
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.wtf(TAG, "This gatt server - " + device.getAddress() + " disconnected");
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
                Log.wtf(TAG, "onServiceAdded - " + service.getType());
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                Log.wtf(TAG, "onCharacteristicReadRequest");
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                Log.wtf(TAG, "onCharacteristicWriteRequest");
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                Log.wtf(TAG, "onDescriptorReadRequest");
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
                Log.wtf(TAG, "onDescriptorWriteRequest");
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
                Log.wtf(TAG, "onExecuteWrite");

            }
        });

        BluetoothGattService advertiseService = setupGattAdvertisingService();
        if (mGattServer != null) {
            mGattServer.addService(advertiseService);
        } else {
            Log.wtf(TAG, "Tried to add service to server, but it was null!");
        }

    }

    private BluetoothGattService setupGattAdvertisingService() {
        BluetoothGattService service = new BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic broadcastChar = new BluetoothGattCharacteristic(uuid, BluetoothGattCharacteristic.PROPERTY_BROADCAST, 0);
        service.addCharacteristic(broadcastChar);
        return service;
    }

    private void toggleConnect(View v, boolean on) {
        if (on) {
            String address = inputAddress.getText().toString();
            if (activity.getmBluetoothAdapter().checkBluetoothAddress(address)) {
                SharedPreferences prefs = activity.getPrefs();
                prefs.edit().putString("ADDRESS", address).commit();

                connectedDevice = activity.getmBluetoothAdapter().getRemoteDevice(address);
            } else {
                connectedDevice = null;
            }

            if (connectedDevice != null) {
                Log.wtf(TAG, "Connected to device!");
                connectedGatt = connectedDevice.connectGatt(getActivity(), true, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            Log.wtf(TAG, "other device -Connected Gatt!");
                        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                            Log.wtf(TAG, "other device -Disconnected Gatt!");
                        }

                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.wtf(TAG, "other device -Gatt status: GATT_SUCCESS");
                        }

                        gatt.readRemoteRssi();
                    }

                    @Override
                    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                        super.onReadRemoteRssi(gatt, rssi, status);
                        Log.wtf(TAG, "rssi = " + rssi);
                    }
                });
                ((TextView)getActivity().findViewById(R.id.connected_address)).setText(connectedDevice.getAddress());
            } else {
                Toast.makeText(getActivity(), "Couldn't connect to device", Toast.LENGTH_LONG).show();
                connectToggle.setChecked(false);
                ((TextView)getActivity().findViewById(R.id.connected_address)).setText("");
            }
        } else {
            if (connectedGatt != null) {
                Log.wtf(TAG, "Disconnected from other gatt server!");
                connectedGatt.disconnect();
            }
            ((TextView)getActivity().findViewById(R.id.connected_address)).setText("");
        }
    }

    private void toggleBroadcast(boolean on) {
        if (on){
            setupGattServer();
        } else {
            if (mGattServer != null) {
                Log.wtf(TAG, "Closing server");
                mGattServer.close();
            } else {
                Log.wtf(TAG, "Tried to stop Gatt server but failed.");
            }
        }
    }
}
