package ch.epfl.chili.cellulo.localizationbt;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSION_REQUEST_CONSTANT = 1;
    public static final String deviceAddress = "00:06:66:74:43:01";
    private BluetoothAdapter BTAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeService LE = new BluetoothLeService();

    public void reconnect(View v) {
        LE.disconnect();
        LE.close();
        LE.initialize(getApplicationContext());
        LE.connect(deviceAddress);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter == null) {
            Log.d("STATE", "NO BLUETOOTH");
        }

        if (!BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                updateDevices();
            }
        });*/

        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
                                      public void onClick(View v) {
                                          LE.readRssi();
                                      }
                                  });

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CONSTANT);
    }

    public void updateDevices() {
        BTAdapter.startDiscovery();
        Log.d("STATE", "Starting discovery");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_rssi, menu);
        return true;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("STATE", "Got devices");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String deviceAddress =  device.getAddress();
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = (TextView) findViewById(R.id.textView1);
                rssi_msg.setText(rssi_msg.getText() + deviceAddress + "/" + name + " => " + rssi + "dBm\n");
            }
        }
    };

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CONSTANT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("STATE", "Permission OK");
                }
                return;
            }
        }
    }
}