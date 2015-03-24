package com.knowledgeprice.bandhack;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.math.BigDecimal;


public class SummaryActivity extends ActionBarActivity {

    public enum DiscountLevel { BAD, LOW, MEDIUM, HIGH }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ImageButton nav = (ImageButton)findViewById(R.id.walkingSubmenu);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, WalkingActivity.class);
                startActivity(intent);
            }
        });

        long stepCount = Model.getInstance().getTotalSteps();
        Log.v("DEZ", "stepCount: " + Long.toString(stepCount));
        setDiscountLevel(calculateDiscountLevel(stepCount));
        setCalculatedScore(calculateScore(stepCount));
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

        TextView discountAmount = (TextView)findViewById(R.id.discountAmount);
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
        TextView calculatedScore = (TextView)findViewById(R.id.calculatedScore);
        calculatedScore.setText(score);
    }
}
