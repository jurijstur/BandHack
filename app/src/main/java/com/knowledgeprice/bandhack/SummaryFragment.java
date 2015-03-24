package com.knowledgeprice.bandhack;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandException;
import com.microsoft.band.notification.MessageFlags;
import com.microsoft.band.notification.VibrationType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.UUID;

public class SummaryFragment extends Fragment {

    private final static String LOG_TAG = "DEZ";
    public final static String MAX_SCORE = "10.00";

    public enum DiscountLevel { BAD, LOW, MEDIUM, HIGH }

    RelativeLayout mLayout;

    Model.OnDataChangedListener onDataChangedListener = new Model.OnDataChangedListener() {
        @Override
        public void totalStepsChanged() {
            setData();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Model.getInstance().addOnDataChangedListener(onDataChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Model.getInstance().removeOnDataChangedListener(onDataChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayout = (RelativeLayout) inflater.inflate(R.layout.activity_summary, container, false);

        ImageButton nav = (ImageButton)mLayout.findViewById(R.id.walkingSubmenu);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WalkingActivity.class);
                startActivity(intent);
            }
        });

        ImageView quicktip = (ImageView)mLayout.findViewById(R.id.quicktips_button);
        quicktip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getActivity().getBaseContext(), "Instead of taxi - take a walk", Toast.LENGTH_SHORT);
                toast.show();

            }
        });

        ImageView notifications = (ImageView)mLayout.findViewById(R.id.notification_button);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getActivity().getBaseContext(), "No new notifications", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        setData();

        return mLayout;
    }

    protected void setData() {
        long stepCount = Model.getInstance().getTotalSteps();
        Log.v("DEZ", "stepCount: " + Long.toString(stepCount));
        setDiscountLevel(calculateDiscountLevel(stepCount));
        setCalculatedScore(calculateScore(stepCount));
    }

    private void setDiscountLevel(DiscountLevel level) {
        String discountText = "";

        switch (level) {
            case BAD:
                discountText = "0%";
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
        int maxSteps = 730;

        BigDecimal calculatedScore = new BigDecimal(stepCount).divide(new BigDecimal(maxSteps), 3, RoundingMode.HALF_UP);
        calculatedScore = calculatedScore.multiply(new BigDecimal(10)).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (calculatedScore.compareTo(new BigDecimal(10.00)) > 0) {
            return MAX_SCORE;
        } else {
            return calculatedScore.toString();
        }
    }

    private void setCalculatedScore(String score) {
        TextView calculatedScore = (TextView)mLayout.findViewById(R.id.calculatedScore);
        if(score.equals(MAX_SCORE)) {
            sendNotificationToBand();
        }

        calculatedScore.setText(score);
    }

    private void sendNotificationToBand() {
        try {
            UUID uuid = UUID.fromString(getString(R.string.DEVICE_ID));
            Model
                .getInstance()
                .getClient()
                .getNotificationManager()
                .sendMessage(
                    uuid,
                    "Daily step count",
                    "You've achieved hiking goal",
                    new Date(),
                    MessageFlags.NONE)
                .await();

            Model
                .getInstance()
                .getClient()
                .getNotificationManager()
                .vibrate(VibrationType.NOTIFICATION_ALARM)
                .await();

        } catch (InterruptedException e) {
                Util.showExceptionAlert(getActivity(), "Interrupted connection", e);
        } catch (BandException e) {
                Util.showExceptionAlert(getActivity(), "Band exception", e);
        } catch (Exception e) {
                Util.showExceptionAlert(getActivity(), "Send message", e);
            }
        }
}
