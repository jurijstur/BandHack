package com.knowledgeprice.bandhack;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandDeviceInfo;
import com.microsoft.band.BandException;
import com.microsoft.band.ConnectionResult;

import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect();
    }


    public void connect() {
        BandDeviceInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
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
            onRefresh();
        }
    }

    public void onRefresh() {
        TextView info = (TextView) findViewById(R.id.info);
        info.setText("");
    }

    public void onDestroy() {
        try {
            if (Model.getInstance().isConnected()) {
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
