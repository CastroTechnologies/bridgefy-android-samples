package com.bridgefy.samples.chatify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.DeviceListener;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.framework.controller.Session;
import com.bridgefy.sdk.framework.entities.ForwardPacket;
import com.bridgefy.sdk.framework.exceptions.MessageException;
import com.bridgefy.sdk.framework.network.ConnectionType;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DevicesActivity extends AppCompatActivity implements DeviceListener, MessageListener {

    private final String TAG = "DevicesActivity";

    @Bind(R.id.devices_recycler_view)
    RecyclerView    devicesRecyclerView;
    DevicesAdapter  devicesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        ButterKnife.bind(this);

        // initialize the DevicesAdapter and the RecyclerView
        devicesAdapter = new DevicesAdapter();
        devicesRecyclerView.setAdapter(devicesAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // check that we have Location permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initializeBridgefy();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }
    }



    /**
     *      BRIDGEFY LISTENERS
     */

    @Override
    public void onDeviceConnected(Device device, ConnectionType deviceType) {
        Log.d(TAG, "Device connected: " + device.getUuid());

        // crear el mensaje
        HashMap<String, Object> name = new HashMap<>();
        name.put("name", Build.MANUFACTURER + " " + Build.MODEL);
        Message message = new Message(name, device.getUuid(),
                Bridgefy.getInstance().getBridgefyClient().getUserUuid(), System.currentTimeMillis());

        // enviar mensaje al dispositivo encontrado
        Bridgefy.sendMessage(message);
    }

    @Override
    public void onDeviceLost(Device device, ConnectionType deviceType) {
        Log.w(TAG, "Device lost: " + device.getUuid());
        devicesAdapter.removeDevice(device);
    }

    @Override
    public void onMessageReceived(Session session, Message message) {
        Log.d(TAG, "Message Received, userId: " + session.getDevice().getUuid() + ", content: " + message.getContent().toString());
        Device device = session.getDevice();
        device.setDeviceName((String) message.getContent().get("name"));
        devicesAdapter.addDevice(device);
    }

    @Override
    public void onMessageSent(Session session, Message message) {
        Log.d(TAG, "Message sent to userId: " + session.getUuid() + ", content: " + message.getContent().toString());
    }

    @Override
    public void onMessageFailed(Message message, MessageException e) {
        Log.e(TAG, "Message failed", e);
    }

    @Override
    public void onMessageReceivedException(ForwardPacket forwardPacket, MessageException e) {
        Log.e(TAG, "Message failed", e);
    }

    @Override
    public void onBroadcastMessageReceived(Message message) {
        Log.d(TAG, "Broadcast Message Received: " + message.getContent().toString());
    }


    /**
     *      OTHER STUFF
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeBridgefy();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start devices discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void initializeBridgefy() {
        Bridgefy.initialize(getApplicationContext());
        Bridgefy.start(this, this);
    }

    public class DevicesAdapter extends RecyclerView.Adapter<DeviceViewHolder> {
        // the list that holds our incoming devices
        ArrayList<Device> devices;

        public DevicesAdapter() {
            devices = new ArrayList<Device>();
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        void addDevice(Device device) {
            if (!devices.contains(device)) {
                devices.add(device);
                notifyItemInserted(devices.size() - 1);
            }
        }

        void removeDevice(Device device) {
            int position = devices.indexOf(device);
            if (position > -1) {
                devices.remove(position);
                notifyItemRemoved(position);
            }
        }

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View deviceView = LayoutInflater.from(viewGroup.getContext()).
                    inflate((R.layout.device_row), viewGroup, false);
            return new DeviceViewHolder(deviceView);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder deviceViewHolder, int position) {
            deviceViewHolder.setDevice(devices.get(position));
        }
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.txt_device)
        TextView deviceView;

        DeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void setDevice(Device device) {
            deviceView.setText(device.getDeviceName());
        }
    }
}
