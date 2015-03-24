package com.knowledgeprice.bandhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;

public class SummaryFragment extends Fragment {

    public enum DiscountLevel { BAD, LOW, MEDIUM, HIGH }

    RelativeLayout mLayout;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayout = (RelativeLayout) inflater.inflate(R.layout.activity_summary, container, false);

        mReceiver = new ReceiveMessages();

        ImageButton nav = (ImageButton)mLayout.findViewById(R.id.walkingSubmenu);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WalkingActivity.class);
                startActivity(intent);
            }
        });

        long stepCount = Model.getInstance().getTotalSteps();
        Log.v("DEZ", "stepCount: " + Long.toString(stepCount));
        setDiscountLevel(calculateDiscountLevel(stepCount));
        setCalculatedScore(calculateScore(stepCount));
        return mLayout;
    }

    private void setDiscountLevel(DiscountLevel level) {
        String discountText = "";

        switch (level) {
            case BAD:
                discountText = "BAD";
                break;
            case LOW:
                discountText = "0%";
                break;
            case MEDIUM:
                discountText = "2%";
                break;
            case HIGH:
                discountText = "4%";
                break;
            default:
                discountText = "No Info";
                break;
        }

        TextView discountAmount = (TextView)mLayout.findViewById(R.id.discountAmount);
        discountAmount.setText(discountText);
    }

    private DiscountLevel calculateDiscountLevel(long stepCount) {
        int[] stepCountRow = new int[] {
                5000,
                10000,
                15000,
                999999999};

        DiscountLevel[] discountLevelRow = new DiscountLevel[] {
                DiscountLevel.BAD,
                DiscountLevel.LOW,
                DiscountLevel.MEDIUM,
                DiscountLevel.HIGH};

        for(int i = 0; i < stepCountRow.length; i++) {
            if(stepCount > stepCountRow[i]) {
                continue;
            } else {
                return discountLevelRow[i];
            }
        }

        return null; // TODO this never happens =]
    }

    private String calculateScore(long stepCount) {
        int maxSteps = 15000;

       BigDecimal calculatedScore = new BigDecimal(stepCount).divide(new BigDecimal(maxSteps));
       calculatedScore.setScale(2, BigDecimal.ROUND_UP);
       if (calculatedScore.compareTo(new BigDecimal(1.0)) > 0) {
           return "1.00";
       } else {
           return calculatedScore.toString();
       }
    }

    private void setCalculatedScore(String score) {
        TextView calculatedScore = (TextView)mLayout.findViewById(R.id.calculatedScore);
        calculatedScore.setText(score);
    }
}
