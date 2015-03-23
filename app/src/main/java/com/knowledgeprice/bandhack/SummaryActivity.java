package com.knowledgeprice.bandhack;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


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
}
