package com.knowledgeprice.bandhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class SummaryActivity extends ActionBarActivity {

    public enum DiscountLevel { BAD, LOW, MEDIUM, HIGH }

    ReceiveMessages mReceiver = null;
    Boolean mReceiverIsRegistered = false;

    private class ReceiveMessages extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equals(MainActivity.DATA_CHANGED)){
                onRefreshData();
            }
        }
    }

    protected void onRefreshData() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mReceiver = new ReceiveMessages();

        ImageButton nav = (ImageButton)findViewById(R.id.walkingSubmenu);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, WalkingActivity.class);
                startActivity(intent);
            }
        });

        setDiscountLevel(DiscountLevel.MEDIUM);
        setCalculatedScore(7.1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary, menu);
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

    private void setDiscountLevel(DiscountLevel level) {
        String discountText = "";

        switch (level) {
            case BAD:
                discountText = "See doctor";
                break;
            case LOW:
                discountText = "See advisor";
                break;
            case MEDIUM:
                discountText = "2%";
                break;
            case HIGH:
                discountText = "4%";
                break;
            default:
                discountText = "No info";
                break;
        }

        TextView discountAmount = (TextView)findViewById(R.id.discountAmount);
        discountAmount.setText(discountText);
    }

    private void setCalculatedScore(Double score) {
        TextView calculatedScore = (TextView)findViewById(R.id.calculatedScore);
        calculatedScore.setText(score.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mReceiverIsRegistered) {
            registerReceiver(mReceiver, new IntentFilter(MainActivity.DATA_CHANGED));
            mReceiverIsRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverIsRegistered) {
            unregisterReceiver(mReceiver);
            mReceiverIsRegistered = false;
        }
    }
}
