package com.knowledgeprice.bandhack;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandDeviceInfo;
import com.microsoft.band.BandException;
import com.microsoft.band.ConnectionResult;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSensorManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class MainActivity extends ActionBarActivity {

    public final static String DATA_CHANGED = "data_changed";

    private String[] mMenuTitles;
    private TypedArray mMenuIcons;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private AtomicReference<BandPedometerEvent> mPendingPedometerEvent = new AtomicReference<BandPedometerEvent>();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private ScheduledFuture mRefreshHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("AVIVA");

        mMenuTitles = getResources().getStringArray(R.array.menu_array);
        mMenuIcons = getResources().obtainTypedArray(R.array.menu_icon_array);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(createOptionsAdapter());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        connect();

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(position == 0) {
                Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
                startActivity(intent);
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }


    private SimpleAdapter createOptionsAdapter() {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for(int i=0; i<mMenuTitles.length; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("text", mMenuTitles[i]);
            list.add(hm);
        }

        String[] from = {"text"};
        int[] to = {R.id.drawer_item_text};

        SimpleAdapter simpleAdapter = new SimpleAdapter (
                getBaseContext(), list, R.layout.drawer_item, from, to);
        return simpleAdapter;
    }

    public void connect() {
        BandDeviceInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
        if (pairedBands.length  < 1) {
            return;
        }

        if (!Model.getInstance().isConnected()) {
            BandClient client = BandClientManager.getInstance().create(this, pairedBands[0]);
            Model.getInstance().setClient(client);

            // Connect must be called on a background thread.
            new ConnectTask().execute(Model.getInstance().getClient());
        }
    }

    private class ConnectTask extends AsyncTask<BandClient, Void, ConnectionResult> {
        @Override
        protected ConnectionResult doInBackground(BandClient... clientParams) {
            try {
                return clientParams[0].connect().await();
            } catch (InterruptedException e) {
                return ConnectionResult.TIMEOUT;
            } catch (BandException e) {
                return ConnectionResult.INTERNAL_ERROR;
            }
        }

        protected void onPostExecute(ConnectionResult result) {
            onConnect(result);
        }
    }

    protected void onConnect(ConnectionResult result) {
        if (result != ConnectionResult.OK) {
            Util.showExceptionAlert(this, "Connect", new Exception("Connection failed: result=" + result.toString()));
        } else {

            final Runnable refresh = new Runnable() {
                public void run() {
                    onRefresh();
                }
            };
            mRefreshHandle = scheduler.scheduleAtFixedRate(refresh, 0, 1, TimeUnit.MINUTES);
        }
    }

    private BandPedometerEventListener mPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(final BandPedometerEvent event) {
            mPendingPedometerEvent.set(event);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handlePendingSensorReports();
                }
            });
        }
    };

    private void handlePendingSensorReports() {
        BandClient bandClient = Model.getInstance().getClient();
        if (bandClient == null) {
            return;
        }
        BandSensorManager sensorMgr = bandClient.getSensorManager();
        BandPedometerEvent pedometerEvent = mPendingPedometerEvent.getAndSet(null);
        if (pedometerEvent != null) {
            Model.getInstance().setTotalSteps(pedometerEvent.getTotalSteps());
            Intent intent = new Intent(DATA_CHANGED);
            this.sendBroadcast(intent);

            Log.w("Test", String.format("%d", pedometerEvent.getTotalSteps()));

            try {
                sensorMgr.unregisterPedometerEventListener(mPedometerEventListener);
            } catch (BandException ex) {
                Util.showExceptionAlert(this, "Unregister sensor listener", ex);
            }
        }
    }

    public void onRefresh() {
        BandSensorManager sensorMgr = Model.getInstance().getClient().getSensorManager();
        try {
            sensorMgr.registerPedometerEventListener(mPedometerEventListener);
        } catch (BandException ex) {
            Util.showExceptionAlert(this, "Register sensor listener", ex);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            if (Model.getInstance().isConnected()) {
                mRefreshHandle.cancel(true);
                Model.getInstance().getClient().disconnect().await();
            }
        } catch (Exception e) {
            // ignore failures here
        } finally {
            Model.getInstance().setClient(null);
        }
    }

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

        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
