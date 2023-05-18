package com.goodix.gftest;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.GFConfig;
import com.goodix.gftest.R;
import com.goodix.gftest.utils.checker.TestResultChecker;
import com.goodix.gftest.widget.HoloCircularProgressBar;

import java.util.HashMap;

public class TestListAdapter extends BaseAdapter {
    private static final int PROGRESS_BAR_MAX = 10000;

    private static final int TEST_ITEM_STATUS_IDLE = 0;
    private static final int TEST_ITEM_STATUS_TESTING = 1;
    private static final int TEST_ITEM_STATUS_SUCCEED = 2;
    private static final int TEST_ITEM_STATUS_FAILED = 3;
    private static final int TEST_ITEM_STATUS_TIMEOUT = 4;
    private static final int TEST_ITEM_STATUS_CANCELED = 5;
    private static final int TEST_ITEM_STATUS_WAIT_FINGER_INPUT = 6;
    private static final int TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT = 7;
    private static final int TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT = 9;
    private static final int TEST_ITEM_STATUS_ENROLLING = 10;
    private static final int TEST_ITEM_STATUS_AUTHENGICATING = 11;
    private static final int TEST_ITEM_STATUS_WAIT_FINGER_DOWN = 12;
    private static final int TEST_ITEM_STATUS_WAIT_FINGER_UP = 13;
    private static final int TEST_ITEM_STATUS_WAIT_TWILL_INPUT = 14;
    private static final int TEST_ITEM_STATUS_NO_SUPPORT = 15;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private Context mContext;

    private int[] TEST_ITEM;

    private GFConfig mConfig;

    private HashMap<Integer, Integer> mTestStatus;

    private int mFailedAttempts = 0;

    public TestListAdapter(Context context, int[] TEST_ITEM, GFConfig config, HashMap<Integer, Integer> testStatus) {
        mContext = context;
        this.TEST_ITEM = TEST_ITEM;
        mConfig = config;
        mTestStatus = testStatus;
    }

    @Override
    public int getCount() {
        return TEST_ITEM.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_home, null);
            holder.titleView = convertView.findViewById(R.id.test_title);
            holder.resultView = convertView.findViewById(R.id.test_result);
            holder.testingViewNormal = convertView.findViewById(R.id.testing_normal);
            holder.testingViewNormal.setVisibility(View.GONE);
            holder.progressBar = convertView.findViewById(R.id.fingerprint_progress_bar);
            holder.progressBar.setMax(PROGRESS_BAR_MAX);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        switch (TEST_ITEM[position]) {
            case TestResultChecker.TEST_SPI:
                holder.titleView.setText(R.string.test_spi);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_RESET_PIN:
                holder.titleView.setText(R.string.test_reset_pin);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_INTERRUPT_PIN:
                holder.titleView.setText(R.string.test_interrupt_pin);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_PIXEL:
                if (mConfig != null && (mConfig.mChipSeries == Constants.GF_MILAN_F_SERIES
                        || mConfig.mChipSeries == Constants.GF_DUBAI_A_SERIES
                        || mConfig.mChipSeries == Constants.GF_MILAN_A_SERIES
                        || mConfig.mChipSeries == Constants.GF_MILAN_HV
                        || mConfig.mChipSeries == Constants.GF_MILAN_AN_SERIES)) {
                    holder.titleView.setText(R.string.test_pixel_open);
                } else {
                    holder.titleView.setText(R.string.test_sensor);
                }
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_BAD_POINT:
                holder.titleView.setText(R.string.test_bad_point);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_CAPTURE:
                holder.titleView.setText(R.string.test_capture);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_ALGO:
                holder.titleView.setText(R.string.test_algo);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_FW_VERSION:
                holder.titleView.setText(R.string.test_fw_version);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_PIXEL_SHORT_STREAK:
                if (mConfig != null
                        && (mConfig.mChipSeries == Constants.GF_MILAN_F_SERIES
                        || mConfig.mChipSeries == Constants.GF_DUBAI_A_SERIES
                        || mConfig.mChipSeries == Constants.GF_MILAN_A_SERIES
                        || mConfig.mChipSeries == Constants.GF_MILAN_HV
                        || mConfig.mChipSeries == Constants.GF_MILAN_AN_SERIES)) {
                    holder.titleView.setText(R.string.test_pixel_short_streak);
                } else {
                    holder.titleView.setText(R.string.test_sensor);
                }
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
            case TestResultChecker.TEST_PERFORMANCE:
                holder.titleView.setText(R.string.test_performance);
                updateTestView(holder, mTestStatus.get(TEST_ITEM[position]));
                break;
        }

        return convertView;
    }

    private void updateTestView(Holder holder, int status) {
        switch (status) {
            case TEST_ITEM_STATUS_IDLE:
                holder.resultView.setVisibility(View.INVISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                break;
            case TEST_ITEM_STATUS_TESTING:
                holder.resultView.setVisibility(View.INVISIBLE);
                holder.testingViewNormal.setVisibility(View.VISIBLE);
                break;
            case TEST_ITEM_STATUS_SUCCEED:
                holder.resultView.setText(R.string.test_succeed);
                holder.resultView.setTextColor(mContext.getColor(R.color.test_succeed_color));
                holder.resultView.setVisibility(View.VISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                break;
            case TEST_ITEM_STATUS_FAILED:
                holder.resultView.setText(R.string.test_failed);
                holder.resultView.setTextColor(mContext.getColor(R.color.test_failed_color));
                holder.resultView.setVisibility(View.VISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                break;
            case TEST_ITEM_STATUS_TIMEOUT:
                holder.resultView.setText(R.string.timeout);
                holder.resultView.setTextColor(mContext.getColor(R.color.test_failed_color));
                holder.resultView.setVisibility(View.VISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                break;
            case TEST_ITEM_STATUS_WAIT_FINGER_INPUT:
                holder.resultView.setText(R.string.normal_touch_sensor);
                holder.resultView.setTextColor(mContext.getColor(R.color.fg_color));
                holder.resultView.setVisibility(View.VISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                break;
            case TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT:
                holder.resultView.setText(R.string.bad_point_touch_sensor);
                holder.resultView.setTextColor(mContext.getColor(R.color.fg_color));
                holder.resultView.setVisibility(View.VISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                break;
            case TEST_ITEM_STATUS_CANCELED:
                holder.resultView.setVisibility(View.VISIBLE);
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                holder.resultView.setText(R.string.canceled);
                holder.resultView.setTextColor(mContext.getColor(R.color.test_failed_color));
                Log.d("updateTestView", "TEST_ITEM_STATUS_CANCELED" + status);
                break;
            case TEST_ITEM_STATUS_WAIT_TWILL_INPUT:
                holder.resultView.setVisibility(View.VISIBLE);
                holder.resultView.setText(R.string.snr_touch_sensor);
                holder.resultView.setTextColor(mContext.getColor(R.color.fg_color));
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                Log.d("updateTestView", "TEST_ITEM_STATUS_WAIT_TWILL_INPUT" + status);
                break;
            case TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT:
                holder.resultView.setVisibility(View.VISIBLE);
                holder.resultView.setText(R.string.real_finger_touch_sensor);
                holder.resultView.setTextColor(mContext.getColor(R.color.fg_color));
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                Log.d("updateTestView", "TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT" + status);
                break;
            case TEST_ITEM_STATUS_ENROLLING:
                holder.resultView.setVisibility(View.INVISIBLE);
                holder.testingViewNormal.setVisibility(View.VISIBLE);
                holder.iconAnimationDrawable.start();
                // set default enrolling min templates
                holder.progressBar.setProgress(PROGRESS_BAR_MAX * 0);
                Log.d("updateTestView", "TEST_ITEM_STATUS_ENROLLING" + status);
                break;
            case TEST_ITEM_STATUS_AUTHENGICATING: {
                StringBuilder sb = new StringBuilder();
                sb.append(mFailedAttempts);
                sb.append("/");
                sb.append(MAX_FAILED_ATTEMPTS);
                holder.resultView.setVisibility(View.INVISIBLE);
                holder.testingViewNormal.setVisibility(View.VISIBLE);
                holder.iconAnimationDrawable.start();
                holder.retryView.setText(sb.toString());
                Log.d("updateTestView", "TEST_ITEM_STATUS_AUTHENGICATING" + status);
                break;
            }
            case TEST_ITEM_STATUS_WAIT_FINGER_DOWN: {
                holder.resultView.setVisibility(View.VISIBLE);
                holder.resultView.setText(R.string.wait_finger_down_tip);
                holder.resultView.setTextColor(mContext.getColor(R.color.fg_color));
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                Log.d("updateTestView", "TEST_ITEM_STATUS_WAIT_FINGER_DOWN" + status);
                break;
            }
            case TEST_ITEM_STATUS_WAIT_FINGER_UP: {
                holder.resultView.setVisibility(View.VISIBLE);
                holder.resultView.setText(R.string.wait_finger_up_tip);
                holder.resultView.setTextColor(mContext.getColor(R.color.fg_color));
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                Log.d("updateTestView", "TEST_ITEM_STATUS_WAIT_FINGER_UP" + status);
                break;
            }
            case TEST_ITEM_STATUS_NO_SUPPORT: {
                holder.resultView.setVisibility(View.VISIBLE);
                holder.resultView.setText(R.string.test_no_support);
                holder.resultView.setTextColor(mContext.getColor(R.color.test_succeed_color));
                holder.testingViewNormal.setVisibility(View.INVISIBLE);
                Log.d("updateTestView", "TEST_ITEM_STATUS_NO_SUPPORT" + status);
                break;
            }
            default:
                break;
        }
    }

    private class Holder {
        TextView titleView;
        TextView resultView;
        ProgressBar testingViewNormal;
        HoloCircularProgressBar progressBar;
        AnimatedVectorDrawable iconAnimationDrawable;
        TextView retryView;
    }
}