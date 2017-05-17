package com.bridgefy.samples.alerts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.client.RegistrationListener;
import com.bridgefy.sdk.client.StateListener;
import com.bridgefy.sdk.samples.alerts.R;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.received_alerts)
    TextView receivedAlerts;
    @BindView(R.id.device_text)
    TextView deviceText;
    @BindView(R.id.device_id)
    TextView deviceId;
    @BindView(R.id.sent_alerts)
    TextView sentAlerts;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private int sentAlertCounter = 0;
    private int receivedAlertCounter = 0;
    Unbinder unbinder;
    private String TAG = "BRIDGEFY_SAMPLE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        deviceText.setText(Build.MANUFACTURER + " " + Build.MODEL);


        Bridgefy.initialize(this, "68898033-3dce-4c80-843e-e10982b942ac", new RegistrationListener() {
            @Override
            public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
                super.onRegistrationSuccessful(bridgefyClient);

                //Important data can be fetched from the BridgefyClient object
                deviceId.setText(bridgefyClient.getUserUuid());

                //Once the registration process has been successful, we can start operations
                Bridgefy.start(messageListener, stateListener);
            }

            @Override
            public void onRegistrationFailed(int i, String s) {
                super.onRegistrationFailed(i, s);
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    StateListener stateListener = new StateListener() {
        @Override
        public void onStarted() {
            super.onStarted();
            Log.i(TAG, "onStarted: Bridgefy started");
        }

        @Override
        public void onStartError(String s, int i) {
            super.onStartError(s, i);


            switch (i) {

                case (StateListener.INSUFFICIENT_PERMISSIONS):

                    //starting operations will fail if you haven't granted the necessary permissions
                    //request them and try again after they have been granted
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    break;
                case (StateListener.LOCATION_SERVICES_DISABLED):
                    //location in the device has been disabled
                    break;


            }
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //retry again after permissions have been granted
            Bridgefy.start(messageListener, stateListener);
        }
    }

    @OnClick(R.id.fab)
    public void onViewClicked() {
        sentAlertCounter++;


        //assemble the data that we are about to send

        HashMap<String, Object> data = new HashMap<>();
        data.put("number", sentAlertCounter);
        data.put("date_sent", System.currentTimeMillis());
        data.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
        Message message = Bridgefy.createMessage(data);


        //Broadcast messages are sent to anyone that can receive it
        Bridgefy.sendBroadcastMessage(message);
        sentAlerts.setText(String.valueOf(sentAlertCounter));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();


        //always stop Bridgefy when it's no longer necessary
        Bridgefy.stop();
    }

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onBroadcastMessageReceived(Message message) {
            super.onBroadcastMessageReceived(message);

            receivedAlertCounter++;
            receivedAlerts.setText(String.valueOf(receivedAlertCounter));
        }

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_alerts) {


            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
