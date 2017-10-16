package ch.epfl.chili.cellulo.localizationbt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends FragmentActivity implements
        OnChartValueSelectedListener {

    private ArrayList<Integer> iterationsSinceConnect = null;

    protected Typeface mTfRegular;
    protected Typeface mTfLight;
    private LineChart mChart;
    private static final int MY_PERMISSION_REQUEST_CONSTANT = 1;
    public static ArrayList<String> macs = new ArrayList<String>();
    private ArrayList<BluetoothLeService> LEs = null;
    private Thread t = null;


    private int[] mColors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };

    public void do_scan(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void reconnect1(int i) {
        if(LEs == null)
            return;
        iterationsSinceConnect.set(i, 0);
        LEs.get(i).disconnect();
        LEs.get(i).close();
        LEs.get(i).initialize(getApplicationContext());
        LEs.get(i).connect(macs.get(i));
        LEs.get(i).last_rssi_success = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void reconnect(View v) {
        if(LEs == null)
            return;
        for(int i = 0; i < LEs.size(); i++)
            reconnect1(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void refresh1(int i) {
        if(LEs == null)
            return;
        if(iterationsSinceConnect.get(i) > 5 && LEs.get(i).last_rssi_success == 0) {
            reconnect1(i);
            return;
        }

        addEntry(i, LEs.get(i).last_rssi);

        if(iterationsSinceConnect.get(i) >= 3) {
            LEs.get(i).readRssi();
        }

        iterationsSinceConnect.set(i, iterationsSinceConnect.get(i) + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void refresh(View v) {
        if(LEs == null)
            return;
        for(int i = 0; i < LEs.size(); i++)
            refresh1(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CONSTANT);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("Signal level vs time");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.LTGRAY);

        Legend l = mChart.getLegend();
        l.setForm(LegendForm.LINE);
        l.setTypeface(mTfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(mTfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(20f);
        leftAxis.setAxisMinimum(-50f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        try {
            macs = (ArrayList<String>) getIntent().getSerializableExtra("macs");
        }
        catch(Exception e)
        {
            macs = null;
        }

        Log.d("STATE", "MAIN" + macs);
        if(macs != null) {
            int L = macs.size();
            iterationsSinceConnect = new ArrayList<Integer>();
            for(int i = 0; i < L; i++) iterationsSinceConnect.add(0);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            for (int z = 0; z < L; z++) {

                ArrayList<Entry> values = new ArrayList<Entry>();
                values.add(new Entry(0, (float) 0));

                LineDataSet d = new LineDataSet(values, macs.get(z));
                d.setLineWidth(2.5f);
                d.setCircleRadius(4f);

                int color = mColors[z % mColors.length];
                d.setColor(color);
                d.setCircleColor(color);
                dataSets.add(d);
            }

            LineData data = new LineData(dataSets);
            mChart.setData(data);

            LEs = new ArrayList<BluetoothLeService>();
            for (int i = 0; i < L; i++) {
                LEs.add(new BluetoothLeService());
                reconnect1(i);
            }

            spawnUpdateThread();
        }
    }

    public void stopUpdateThread(View v)
    {
        if(t != null)
            t.interrupt();
    }

    private void spawnUpdateThread() {
        if(LEs == null)
            return;
        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                            @Override
                            public void run() {
                                refresh(null);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }

    private void addEntry(int i, int value) {
        if(LEs == null)
            return;

        mChart.getData().addEntry(new Entry(mChart.getData().getDataSetByIndex(i).getEntryCount(), (float) value), i);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.moveViewToX(mChart.getData().getDataSetByIndex(i).getEntryCount());
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

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