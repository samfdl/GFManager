/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */
package com.goodix.gftest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.GFConfig;
import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.goodix.fingerprint.service.GoodixFingerprintManager.TestCmdCallback;
import com.goodix.fingerprint.service.GoodixFingerprintManager.UntrustedAuthenticationCallback;
import com.goodix.fingerprint.service.GoodixFingerprintManager.UntrustedRemovalCallback;
import com.goodix.fingerprint.utils.TestParamEncoder;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.TestHistoryUtils;
import com.goodix.gftest.utils.checker.Checker;
import com.goodix.gftest.utils.checker.TestResultChecker;
import com.goodix.gftest.widget.HoloCircularProgressBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.IOException;

public class TouchTestActivity extends Activity {
    private static final String TAG = "TouchTestActivity";

    public static final int RING_KEY = 1;
    public static final int MENU_KEY = 3;
    public static final int BACK_KEY = 4;
    public static final int KEY_STATUS_DOWN = 1;
    private ListView mListView = null;
    private AlertDialog mCountDownDialog = null;
    private static int[] TEST_ITEM = null;

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

    private HashMap<Integer, Integer> mTestStatus = new HashMap<Integer, Integer>();

    private ProgressDialog mDialog;
    private Handler mHandler = new Handler();
    private MyAdapter mAdapter = new MyAdapter();

    private Toast mToast = null;

    private boolean mIsSensorValidityTested = true;
    private int mSensorValidityTestFlag = 1;
    private boolean mAutoTest = false;
    private int mAutoTestPosition = 0;
    private TextView mAutoTestingView = null;
    private TextView mAutoTestingTitleView = null;
    private long mAutoTestTimeout = Constants.TEST_TIMEOUT_MS;
    private long mMillisStart = 0;

    private long mAutoTestStartTime = 0;
    private long mAutoTestPrevTestEndTime = 0;

    private GoodixFingerprintManager mGoodixFingerprintManager = null;
    private GFConfig mConfig = null;

    private int mEnrollmentSteps = 8;
    private int mEnrollmentRemaining = 8;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private int mFailedAttempts = 0;

    private CancellationSignal mEnrollmentCancel = null;
    private CancellationSignal mAuthenticationCancel = null;

    private HashMap<Integer, Object> mPendingBioDetail = null;

    private static final int GF_MILAN_A_SERIES_CFG_LENGTH = 256;

    private static final int GF_MILAN_AN_SERIES_CFG_LENGTH = 418;

    private static final int INVALID_FW_FILE_LEN = 0;
    private static final int INVALID_FW_FILE_DATA = 1;
    private static final int INVALID_CFG_FILE_LEN = 2;

    private Checker mTestResultChecker;
    private boolean mIsPrevStablePassed = false;
    private final int[] TEST_ITEM_DUBAI_A_SERIES_AUTO = {
            TestResultChecker.TEST_SPI,
            TestResultChecker.TEST_RESET_PIN,
            TestResultChecker.TEST_INTERRUPT_PIN,
            TestResultChecker.TEST_PIXEL,
            TestResultChecker.TEST_PERFORMANCE,
    };
    private int spiTestR = 0;
    private int resetTestR = 0;
    private int intTestR = 0;
    private int pixelTestR = 0;
    private int pixelShortTestR = 0;
    private int perfTestR = 0;
    private int mTestR = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mGoodixFingerprintManager = GoodixFingerprintManager.getFingerprintManager(TouchTestActivity.this);
        mGoodixFingerprintManager.registerTestCmdCallback(mTestCmdCallback);
    }

    public void initView() {
        if (TEST_ITEM == null) {
            return;
        }

        for (Integer test_item : TEST_ITEM) {
            mTestStatus.put(test_item, TEST_ITEM_STATUS_IDLE);
        }

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (0 == mSensorValidityTestFlag) {
                    return;
                }

                // header view
                if (position == 0) {
                    Log.d(TAG, "onItemClick mAutoTest = " + mAutoTest);
                    if (mAutoTest) {
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(TouchTestActivity.this, R.string.busy,
                                Toast.LENGTH_SHORT);
                        mToast.show();
                    } else {
                        startAutoTest();
                    }
                    return;
                }

                if (position - 1 >= TEST_ITEM.length && mAutoTest) {
                    Log.d(TAG, "onItemClick mAutoTest = " + mAutoTest);
                    return;
                }

                mAutoTestPosition = position;
                startTest(TEST_ITEM[position - 1]);
            }
        });

        mListView.setAdapter(mAdapter);

        View header = LayoutInflater.from(TouchTestActivity.this)
                .inflate(R.layout.item_home, null, false);
        if (null != header) {
            header.findViewById(R.id.testing_normal).setVisibility(View.INVISIBLE);
        }

        mAutoTestingTitleView = (TextView) header.findViewById(R.id.test_title);
        mAutoTestingTitleView.setText(R.string.test_auto);

        mAutoTestingView = (TextView) header.findViewById(R.id.test_result);
        mAutoTestingView.setText(R.string.testing);
        mAutoTestingView.setVisibility(View.INVISIBLE);
        mListView.addHeaderView(header);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume start");

        if (null != mConfig && (mConfig.mChipSeries == Constants.GF_MILAN_F_SERIES
                || Constants.GF_MILAN_HV == mConfig.mChipSeries || mConfig.mChipSeries == Constants.GF_DUBAI_A_SERIES)) {
            Log.d(TAG, "TEST_CHECK_SENSOR_TEST_INFO start");
            if (mIsSensorValidityTested == false) {
                mSensorValidityTestFlag = 0;
                mAutoTestingTitleView.setEnabled(false);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_SENSOR_VALIDITY);
                mDialog = new ProgressDialog(this);
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.setCancelable(true);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setMessage(TouchTestActivity.this.getString(R.string.sensor_checking));
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                });
                mDialog.show();
            }
        }

        //if can't get sensor chip_id ,no need to start runnable
        if (TEST_ITEM != null) {
            getTimeout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mEnrollmentCancel != null && !mEnrollmentCancel.isCanceled()) {
            mEnrollmentCancel.cancel();
            mEnrollmentCancel = null;
            saveTestResult(TestResultChecker.TEST_UNTRUSTED_ENROLL, TEST_ITEM_STATUS_CANCELED);
            saveTestDetail(TestResultChecker.TEST_UNTRUSTED_ENROLL, null);
            mAdapter.notifyDataSetChanged();
        } else if (mAuthenticationCancel != null && !mAuthenticationCancel.isCanceled()) {
            mAuthenticationCancel.cancel();
            mAuthenticationCancel = null;
            saveTestResult(TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, TEST_ITEM_STATUS_CANCELED);
            saveTestDetail(TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, null);
            mAdapter.notifyDataSetChanged();
        } else {
            stopTest(TEST_ITEM_STATUS_CANCELED);
            mAdapter.notifyDataSetChanged();
        }

        if (mGoodixFingerprintManager.hasEnrolledUntrustedFingerprint()) {
            mGoodixFingerprintManager.untrustedRemove(new UntrustedRemovalCallback() {
                @Override
                public void onRemovalSucceeded(int fingerId) {
                }

                @Override
                public void onRemovalError(int errMsgId, CharSequence errString) {
                }
            });
        }

        mHandler.removeCallbacks(mTimeoutRunnable);
        mHandler.removeCallbacks(mAutoTestRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoodixFingerprintManager.unregisterTestCmdCallback(mTestCmdCallback);
    }

    private void startAutoTest() {
        Log.d(TAG, "startAutoTest");
        mAutoTest = true;
        Log.d(TAG, "startAutoTest mAutoTest = " + mAutoTest);

        TestHistoryUtils.clearHistory();
        TestHistoryUtils.init(getFilesDir().getPath(), "testtool.txt", "testdetail.txt");

        for (Integer test_item : TEST_ITEM) {
            mTestStatus.put(test_item, TEST_ITEM_STATUS_IDLE);
        }
        mAdapter.notifyDataSetChanged();

        mAutoTestStartTime = System.currentTimeMillis();
        mAutoTestPrevTestEndTime = mAutoTestStartTime;
        mAutoTestPosition = 0;

        startTest(TEST_ITEM[mAutoTestPosition]);
        mAutoTestPosition++;
        mAutoTestingView.setVisibility(View.VISIBLE);
        spiTestR = 0;
        resetTestR = 0;
        intTestR = 0;
        pixelTestR = 0;
        pixelShortTestR = 0;
        perfTestR = 0;
        mTestR = 0;
    }

    private void saveTestResult(int testId, int reason) {
        mTestStatus.put(testId, reason);
        if (reason == TEST_ITEM_STATUS_TIMEOUT) {
            TestHistoryUtils.addResult(testId, "result=TIMEOUT");
            mTestR = 1;
        } else if (reason == TEST_ITEM_STATUS_CANCELED) {
            TestHistoryUtils.addResult(testId, "result=CANCELED");
            mTestR = 2;
        } else if (reason == TEST_ITEM_STATUS_FAILED) {
            TestHistoryUtils.addResult(testId, "result=FAILED");
            mTestR = 3;
        } else if (reason == TEST_ITEM_STATUS_SUCCEED) {
            TestHistoryUtils.addResult(testId, "result=SUCCEED");
        } else if (reason == TEST_ITEM_STATUS_NO_SUPPORT) {
            TestHistoryUtils.addResult(testId, "result=NO SUPPORT");
        }

        mAdapter.notifyDataSetChanged();
        mHandler.removeCallbacks(mTimeoutRunnable);
        autoNextTest();
    }

    private void saveTestResultOnly(int testId, int reason) {
        mTestStatus.put(testId, reason);
        if (reason == TEST_ITEM_STATUS_TIMEOUT) {
            TestHistoryUtils.addResult(testId, "result=TIMEOUT");
        } else if (reason == TEST_ITEM_STATUS_CANCELED) {
            TestHistoryUtils.addResult(testId, "result=CANCELED");
        } else if (reason == TEST_ITEM_STATUS_FAILED) {
            TestHistoryUtils.addResult(testId, "result=FAILED");
        } else if (reason == TEST_ITEM_STATUS_SUCCEED) {
            TestHistoryUtils.addResult(testId, "result=SUCCEED");
        } else if (reason == TEST_ITEM_STATUS_NO_SUPPORT) {
            TestHistoryUtils.addResult(testId, "result=NO SUPPORT");
        }
    }

    private void saveTestDetail(int testId, HashMap<Integer, Object> result) {
        TestHistoryUtils.addDetail(testId, result);
        TestHistoryUtils.addDetail("time:"
                + (System.currentTimeMillis() - mAutoTestPrevTestEndTime)
                + "ms");
    }

    // dedicated patch function for TEST_BIO_ASSAY
    private void saveTestDetail(int testId, HashMap<Integer, Object> result1,
                                HashMap<Integer, Object> result2) {
        if (testId == TestResultChecker.TEST_BIO_CALIBRATION
                || testId == TestResultChecker.TEST_HBD_CALIBRATION) {
            TestHistoryUtils.addDetail(testId, result1, result2);
            TestHistoryUtils.addDetail("time:" + (System.currentTimeMillis() - mAutoTestPrevTestEndTime)
                    + "ms");
        }
    }

    private void enableBioAssay() {
        if (mConfig.mChipType == Constants.GF_CHIP_5206 || mConfig.mChipType == Constants.GF_CHIP_5208) {
            toggleBioAssay(true);
        }
    }

    private void disableBioAssay() {
        if (mConfig.mChipType == Constants.GF_CHIP_5206
                || mConfig.mChipType == Constants.GF_CHIP_5208) {
            toggleBioAssay(false);
        }
    }

    private void resetBioAssay() {
        Log.d(TAG, "support bio assay : " + mConfig.mSupportBioAssay);
        if (mConfig.mSupportBioAssay == 0) {
            disableBioAssay();
        } else {
            enableBioAssay();
        }
    }

    private void toggleBioAssay(boolean enabled) {
        byte[] byteArray = new byte[TestParamEncoder.TEST_ENCODE_SIZEOF_INT32];
        int offset = 0;
        int value = 0;

        if (true == enabled) {
            value = 1;
        }

        TestParamEncoder.encodeInt32(byteArray, offset, TestResultParser.TEST_TOKEN_SUPPORT_BIO_ASSAY, value);
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_SET_CONFIG, byteArray);
    }

    private void stopTest(int reason) {
        Log.d(TAG, "stopTest reason: " + reason);

        if (TEST_ITEM == null) {
            return;
        }

        stopCountDownForSwitchFinger();
        for (Integer test_item : TEST_ITEM) {
            if (mTestStatus.get(test_item) == TEST_ITEM_STATUS_TESTING
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_FINGER_INPUT
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_ENROLLING
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_AUTHENGICATING
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_FINGER_DOWN
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_FINGER_UP
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_TWILL_INPUT) {
                switch (test_item) {
                    case TestResultChecker.TEST_SPI:
                        Log.d(TAG, "TEST_SPI " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_PIXEL:
                        Log.d(TAG, "TEST_PIXEL " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_PIXEL_SHORT_STREAK:
                        Log.d(TAG, "TEST_PIXEL_SHORT_STREAK " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_RESET_PIN:
                        Log.d(TAG, "TEST_RESET_PIN " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_INTERRUPT_PIN:
                        Log.d(TAG, "TEST_INTERRUPT_PIN " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_BAD_POINT:
                        Log.d(TAG, "TEST_BAD_POINT " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_PERFORMANCE:
                        Log.d(TAG, "TEST_PERFORMANCE " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_CAPTURE:
                        Log.d(TAG, "TEST_CAPTURE " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_ALGO:
                        Log.d(TAG, "TEST_ALGO " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_BIO_CALIBRATION:
                        Log.d(TAG, "TEST_BIO_CALIBRATION " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_HBD_CALIBRATION:
                        Log.d(TAG, "TEST_HBD_CALIBRATION " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                    case TestResultChecker.TEST_FW_VERSION:
                        Log.d(TAG, "TEST_FW_VERSION " + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_RAWDATA_SATURATED:
                        Log.d(TAG, "TEST_RAWDATA_SATURATED "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_UNTRUSTED_ENROLL:
                        Log.d(TAG, "TEST_UNTRUSTED_ENROLL "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE:
                        Log.d(TAG, "TEST_UNTRUSTED_AUTHENTICATE "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_SENSOR_FINE:
                        Log.d(TAG, "TEST_SENSOR_FINE "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_FPC_MENU_KEY:
                        Log.d(TAG, "TEST_FPC_MENU_KEY "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        saveTestResultOnly(test_item, reason);
                        break;

                    case TestResultChecker.TEST_FPC_BACK_KEY:
                        Log.d(TAG, "TEST_FPC_BACK_KEY "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        saveTestResultOnly(test_item, reason);
                        break;

                    case TestResultChecker.TEST_FPC_RING_KEY:
                        Log.d(TAG, "TEST_FPC_RING_KEY "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        saveTestResultOnly(test_item, reason);
                        break;

                    case TestResultChecker.TEST_STABLE_FACTOR:
                        Log.d(TAG, "TEST_STABLE_FACTOR "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_TWILL_BADPOINT:
                        Log.d(TAG, "TEST_TWILL_BADPOINT "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;

                    case TestResultChecker.TEST_SNR:
                        Log.d(TAG, "TEST_SNR "
                                + (reason == TEST_ITEM_STATUS_TIMEOUT ? "timeout" : "canceled"));
                        break;
                }

                resetBioAssay();
                if (TestResultChecker.TEST_SPI == test_item) {
                    mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PRIOR_CANCEL);
                } else if (TestResultChecker.TEST_UNTRUSTED_ENROLL == test_item) {
                    if (mEnrollmentCancel != null && !mEnrollmentCancel.isCanceled()) {
                        mEnrollmentCancel.cancel();
                        mEnrollmentCancel = null;
                    }
                } else if (TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE == test_item) {
                    if (mAuthenticationCancel != null && !mAuthenticationCancel.isCanceled()) {
                        mAuthenticationCancel.cancel();
                        mAuthenticationCancel = null;
                    }
                } else {
                    mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);
                }
                if (TestResultChecker.TEST_FPC_MENU_KEY != test_item
                        && TestResultChecker.TEST_FPC_BACK_KEY != test_item && TestResultChecker.TEST_FPC_RING_KEY != test_item) {
                    saveTestResult(test_item, reason);
                    saveTestDetail(test_item, null);
                } else {
                    mAdapter.notifyDataSetChanged();
                    mHandler.removeCallbacks(mTimeoutRunnable);

                    autoNextTest();
                }

                break;
            }
        }
    }

    private boolean startTest(int testCmd) {
        byte[] fpcKeyType = new byte[1];
        Log.d(TAG, "startTest cmd: " + testCmd);
        for (Integer test_item : TEST_ITEM) {
            if (mTestStatus.get(test_item) == TEST_ITEM_STATUS_TESTING
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_FINGER_INPUT
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_ENROLLING
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_AUTHENGICATING
                    || mTestStatus.get(test_item) == TEST_ITEM_STATUS_WAIT_TWILL_INPUT) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(TouchTestActivity.this, R.string.busy, Toast.LENGTH_SHORT);
                mToast.show();

                Log.d(TAG, "startTest " + test_item + " busy");
                return false;
            }
        }

        if (mAutoTestPrevTestEndTime == 0) {
            mAutoTestStartTime = System.currentTimeMillis();
        }
        switch (testCmd) {
            case TestResultChecker.TEST_SPI:
                Log.d(TAG, "TEST_SPI start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_SPI);
                break;

            case TestResultChecker.TEST_PIXEL:
                Log.d(TAG, "TEST_PIXEL start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PIXEL_OPEN);
                break;

            case TestResultChecker.TEST_PIXEL_SHORT_STREAK:
                Log.d(TAG, "TEST_PIXEL_SHORT_STREAK start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PIXEL_SHORT_STREAK);
                break;

            case TestResultChecker.TEST_RESET_PIN:
                Log.d(TAG, "TEST_RESET_PIN start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_RESET_PIN);
                break;

            case TestResultChecker.TEST_INTERRUPT_PIN:
                Log.d(TAG, "TEST_INTERRUPT_PIN start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_INTERRUPT_PIN);
                break;

            case TestResultChecker.TEST_BAD_POINT:
                Log.d(TAG, "TEST_BAD_POINT start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_BAD_POINT);
                break;

            case TestResultChecker.TEST_PERFORMANCE:
                if (mAutoTest) {
                    TestHistoryUtils.addResult("fingerdown 0");
                }

                Log.d(TAG, "TEST_PERFORMANCE start");
                disableBioAssay();
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_INPUT);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PERFORMANCE);
                break;

            case TestResultChecker.TEST_CAPTURE:
                Log.d(TAG, "TEST_CAPTURE start");
                disableBioAssay();
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_INPUT);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PERFORMANCE);
                break;

            case TestResultChecker.TEST_ALGO:
                Log.d(TAG, "TEST_ALGO start");
                disableBioAssay();
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_INPUT);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PERFORMANCE);
                break;

            case TestResultChecker.TEST_BIO_CALIBRATION:
                enableBioAssay();
                mTestStatus.put(TestResultChecker.TEST_BIO_CALIBRATION,
                        TEST_ITEM_STATUS_TESTING);
                mAdapter.notifyDataSetChanged();
                if (mTestResultChecker.getTestItemsByStatus(1) == TEST_ITEM) {
                    startCountDownForSwitchFinger();
                } else {
                    startTestBioCalibration();
                }
                break;

            case TestResultChecker.TEST_HBD_CALIBRATION:

                enableBioAssay();
                mTestStatus.put(TestResultChecker.TEST_HBD_CALIBRATION,
                        TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT);
                startCheckFingerDownStatus();
                break;
            case TestResultChecker.TEST_FW_VERSION:
                Log.d(TAG, "TEST_FW_VERSION start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_SPI);
                break;

            case TestResultChecker.TEST_RAWDATA_SATURATED:
                Log.d(TAG, "TEST_RAWDATA_SATURATED start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_INPUT);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_RAWDATA_SATURATED);
                break;

            case TestResultChecker.TEST_UNTRUSTED_ENROLL:
                Log.d(TAG, "TEST_UNTRUSTED_ENROLL start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_ENROLLING);
                mEnrollmentCancel = new CancellationSignal();
                mGoodixFingerprintManager.untrustedEnroll(mEnrollmentCancel, mEnrollmentCallback);
                mEnrollmentRemaining = mEnrollmentSteps;
                break;

            case TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE: {
                Log.d(TAG, "TEST_UNTRUSTED_AUTHENTICATE start");
                if (mGoodixFingerprintManager.hasEnrolledUntrustedFingerprint()) {
                    mTestStatus.put(testCmd, TEST_ITEM_STATUS_AUTHENGICATING);
                    mAuthenticationCancel = new CancellationSignal();
                    mGoodixFingerprintManager.untrustedAuthenticate(mAuthenticationCancel,
                            mAuthCallback);
                    mFailedAttempts = 0;
                } else {
                    if (!mAutoTest) {
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(TouchTestActivity.this, R.string.not_enrolled,
                                Toast.LENGTH_SHORT);
                        mToast.show();
                        return false;
                    }
                }
                break;
            }

            case TestResultChecker.TEST_SENSOR_FINE:
                Log.d(TAG, "TEST_SENSOR_FINE start");
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_SENSOR_FINE);
                break;

            case TestResultChecker.TEST_FPC_MENU_KEY:
                fpcKeyType[0] = 0x01;
                downLoadCfgForFPCKey();
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY, fpcKeyType);
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_DOWN);
                break;

            case TestResultChecker.TEST_FPC_BACK_KEY:
                fpcKeyType[0] = 0x02;
                downLoadCfgForFPCKey();
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY, fpcKeyType);
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_DOWN);
                break;

            case TestResultChecker.TEST_FPC_RING_KEY:
                fpcKeyType[0] = 0x04;
                downLoadCfgForFPCKey();
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY, fpcKeyType);
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_FINGER_DOWN);
                break;

            case TestResultChecker.TEST_STABLE_FACTOR:
                Log.d(TAG, "TEST_STABLE_FACTOR start");
                mIsPrevStablePassed = false;
                mTestStatus.put(testCmd, TEST_ITEM_STATUS_WAIT_TWILL_INPUT);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_STABLE_FACTOR);
                break;

            case TestResultChecker.TEST_TWILL_BADPOINT:
                Log.d(TAG, "TEST_TWILL_BADPOINT start");
                if (mIsPrevStablePassed == true) {
                    mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                    mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_TWILL_BADPOINT);
                } else {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(TouchTestActivity.this, R.string.stable_factor_first,
                            Toast.LENGTH_SHORT);
                    mToast.show();
                    return false;
                }
                break;

            case TestResultChecker.TEST_SNR:
                Log.d(TAG, "TEST_SNR start");
                if (mIsPrevStablePassed == true) {
                    mTestStatus.put(testCmd, TEST_ITEM_STATUS_TESTING);
                    mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_NOISE);
                } else {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(TouchTestActivity.this, R.string.stable_factor_first,
                            Toast.LENGTH_SHORT);
                    mToast.show();
                    return false;
                }
                break;

        }

        mAdapter.notifyDataSetChanged();

        mHandler.removeCallbacks(mTimeoutRunnable);
        mHandler.postDelayed(mTimeoutRunnable, mAutoTestTimeout);
        return true;
    }

    private void autoNextTest() {
        if (mAutoTest) {
            boolean canceled = false;
            if (mAutoTestPosition > 0) {
                int status = mTestStatus.get(TEST_ITEM[mAutoTestPosition - 1]);
                if (status == TEST_ITEM_STATUS_CANCELED) {
                    canceled = true;
                }
            }

            if (mAutoTestPosition < mAdapter.getCount() && !canceled/* && !timeout && !failed */) {
                mAutoTestPrevTestEndTime = System.currentTimeMillis();
                if (TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE == TEST_ITEM[mAutoTestPosition]
                        && !mGoodixFingerprintManager.hasEnrolledUntrustedFingerprint()) {
                    mTestStatus.put(TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE,
                            TEST_ITEM_STATUS_FAILED);
                    mAdapter.notifyDataSetChanged();
                    mHandler.removeCallbacks(mTimeoutRunnable);
                    mAutoTestPosition++;
                    autoNextTest();
                } else if (startTest(TEST_ITEM[mAutoTestPosition])) {
                    mAutoTestPosition++;
                }
                mListView.smoothScrollToPosition(mAutoTestPosition);
                Log.d(TAG, "autoNextTest mAutoTestPosition = " + mAutoTestPosition);
            } else {
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL, null);

                checkResult();

                mAutoTest = false;
                Log.d(TAG, "autoNextTest mAutoTest = " + mAutoTest);
                mAutoTestPosition = 0;
                mAutoTestingView.setVisibility(View.INVISIBLE);

                //设置返回数据
                setResult(-1, null);
                finish();
            }
        } else {
            mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL, null);
        }
    }

    private class MyAdapter extends BaseAdapter {
        private static final int ITEM_VIEW_TYPE_UNTRUSTED_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_UNTRUSTED_ENROLL = 1;
        private static final int ITEM_VIEW_TYPE_UNTRUSTED_AUTHENTICATED = 2;
        private static final int ITEM_VIEW_TYPE_MAX = 3;

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
        public int getItemViewType(int position) {
            int type = ITEM_VIEW_TYPE_UNTRUSTED_NORMAL;
            switch (TEST_ITEM[position]) {
                case TestResultChecker.TEST_UNTRUSTED_ENROLL:
                    type = ITEM_VIEW_TYPE_UNTRUSTED_ENROLL;
                    break;
                case TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE:
                    type = ITEM_VIEW_TYPE_UNTRUSTED_AUTHENTICATED;
                    break;
                default:
                    type = ITEM_VIEW_TYPE_UNTRUSTED_NORMAL;
                    break;
            }
            return type;
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_MAX;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            Holder holder = null;

            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(TouchTestActivity.this).inflate(
                        R.layout.item_home, null);
                holder.testingViewNormal = (ProgressBar) convertView
                        .findViewById(R.id.testing_normal);
                holder.testingViewUntrustAuthenticate = (LinearLayout) convertView
                        .findViewById(R.id.testing_untrust_authenticate);
                holder.testingViewUntrustEnroll = (RelativeLayout) convertView
                        .findViewById(R.id.testing_untrust_enroll);
                holder.testingViewNormal.setVisibility(View.GONE);
                holder.testingViewUntrustAuthenticate.setVisibility(View.GONE);
                holder.testingViewUntrustEnroll.setVisibility(View.VISIBLE);
                holder.progressBar = (HoloCircularProgressBar) convertView
                        .findViewById(R.id.fingerprint_progress_bar);
                holder.progressBar.setMax(PROGRESS_BAR_MAX);
                holder.titleView = (TextView) convertView.findViewById(R.id.test_title);
                holder.resultView = (TextView) convertView.findViewById(R.id.test_result);

                switch (type) {
                    case ITEM_VIEW_TYPE_UNTRUSTED_ENROLL: {
                        final ImageView fingerprintAnimator = (ImageView) convertView
                                .findViewById(R.id.fingerprint_animator_untrust_enroll);
                        final AnimatedVectorDrawable iconAnimationDrawable = (AnimatedVectorDrawable) fingerprintAnimator
                                .getDrawable();
                        fingerprintAnimator.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                iconAnimationDrawable.start();
                                fingerprintAnimator.removeCallbacks(this);
                                fingerprintAnimator.postDelayed(this, 2000);
                            }
                        }, 2000);

                        holder.iconAnimationDrawable = iconAnimationDrawable;
                        break;
                    }

                    case ITEM_VIEW_TYPE_UNTRUSTED_AUTHENTICATED: {
                        final ImageView fingerprintAnimator = (ImageView) convertView
                                .findViewById(R.id.fingerprint_animator_untrust_authenticate);
                        final AnimatedVectorDrawable iconAnimationDrawable = (AnimatedVectorDrawable) fingerprintAnimator
                                .getDrawable();

                        fingerprintAnimator.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                iconAnimationDrawable.start();
                                fingerprintAnimator.removeCallbacks(this);
                                fingerprintAnimator.postDelayed(this, 2000);
                            }
                        }, 2000);

                        holder.iconAnimationDrawable = iconAnimationDrawable;
                        holder.retryView = (TextView) convertView
                                .findViewById(R.id.authenticate_retry_count);
                        break;
                    }
                    default: {
                        break;
                    }
                }

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            if (mSensorValidityTestFlag == 0) {
                holder.titleView.setEnabled(false);
            } else {
                holder.titleView.setEnabled(true);
            }

            switch (TEST_ITEM[position]) {
                case TestResultChecker.TEST_SPI:
                    holder.titleView.setText(R.string.test_spi);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_PIXEL:
                    if (mConfig != null
                            && (mConfig.mChipSeries == Constants.GF_MILAN_F_SERIES
                            || mConfig.mChipSeries == Constants.GF_DUBAI_A_SERIES
                            || mConfig.mChipSeries == Constants.GF_MILAN_A_SERIES
                            || mConfig.mChipSeries == Constants.GF_MILAN_HV
                            || mConfig.mChipSeries == Constants.GF_MILAN_AN_SERIES)) {
                        holder.titleView.setText(R.string.test_pixel_open);
                    } else {
                        holder.titleView.setText(R.string.test_sensor);
                    }
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
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
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_RESET_PIN:
                    holder.titleView.setText(R.string.test_reset_pin);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_INTERRUPT_PIN:
                    holder.titleView.setText(R.string.test_interrupt_pin);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_BAD_POINT:
                    holder.titleView.setText(R.string.test_bad_point);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_PERFORMANCE:
                    holder.titleView.setText(R.string.test_performance);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_CAPTURE:
                    holder.titleView.setText(R.string.test_capture);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_ALGO:
                    holder.titleView.setText(R.string.test_algo);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_BIO_CALIBRATION:
                    holder.titleView.setText(R.string.test_bio_assay);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_HBD_CALIBRATION:
                    holder.titleView.setText(R.string.test_hbd_feature);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_FW_VERSION:
                    holder.titleView.setText(R.string.test_fw_version);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_RAWDATA_SATURATED:
                    holder.titleView.setText(R.string.test_rawdata_saturated);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_UNTRUSTED_ENROLL:
                    holder.titleView.setText(R.string.untrusted_enroll);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE:
                    holder.titleView.setText(R.string.untrusted_authenticate);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_SENSOR_FINE:
                    holder.titleView.setText(R.string.test_sensor_fine);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_FPC_MENU_KEY:
                    holder.titleView.setText(R.string.fpc_menu_key_title);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_FPC_BACK_KEY:
                    holder.titleView.setText(R.string.fpc_back_key_title);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_FPC_RING_KEY:
                    holder.titleView.setText(R.string.fpc_ring_key_title);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_STABLE_FACTOR:
                    holder.titleView.setText(R.string.test_stable_factor);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_TWILL_BADPOINT:
                    holder.titleView.setText(R.string.test_twill_badpoint);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;

                case TestResultChecker.TEST_SNR:
                    holder.titleView.setText(R.string.snr_test);
                    updateTestView(holder, type, mTestStatus.get(TEST_ITEM[position]));
                    break;
            }

            return convertView;
        }

        private void updateHolderTestingView(Holder holder, int type, int visibility) {
            if (ITEM_VIEW_TYPE_UNTRUSTED_NORMAL == type) {
                holder.testingViewNormal.setVisibility(visibility);
                holder.testingViewUntrustEnroll.setVisibility(View.GONE);
                holder.testingViewUntrustAuthenticate.setVisibility(View.GONE);
            } else if (ITEM_VIEW_TYPE_UNTRUSTED_ENROLL == type) {
                holder.testingViewNormal.setVisibility(View.GONE);
                holder.testingViewUntrustEnroll.setVisibility(visibility);
                holder.testingViewUntrustAuthenticate.setVisibility(View.GONE);
            } else if (ITEM_VIEW_TYPE_UNTRUSTED_AUTHENTICATED == type) {
                holder.testingViewNormal.setVisibility(View.GONE);
                holder.testingViewUntrustEnroll.setVisibility(View.GONE);
                holder.testingViewUntrustAuthenticate.setVisibility(visibility);
            }
        }

        private void updateTestView(Holder holder, int type, int status) {
            switch (status) {
                case TEST_ITEM_STATUS_IDLE:
                    holder.resultView.setVisibility(View.INVISIBLE);
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;

                case TEST_ITEM_STATUS_TESTING:
                    holder.resultView.setVisibility(View.INVISIBLE);
                    updateHolderTestingView(holder, type, View.VISIBLE);
                    break;

                case TEST_ITEM_STATUS_SUCCEED:
                    holder.resultView.setVisibility(View.VISIBLE);
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    holder.resultView.setText(R.string.test_succeed);
                    holder.resultView
                            .setTextColor(getResources().getColor(R.color.test_succeed_color));
                    break;

                case TEST_ITEM_STATUS_FAILED:
                    holder.resultView.setVisibility(View.VISIBLE);
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    holder.resultView.setText(R.string.test_failed);
                    holder.resultView
                            .setTextColor(getResources().getColor(R.color.test_failed_color));
                    break;

                case TEST_ITEM_STATUS_TIMEOUT:
                    holder.resultView.setVisibility(View.VISIBLE);
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    holder.resultView.setText(R.string.timeout);
                    holder.resultView
                            .setTextColor(getResources().getColor(R.color.test_failed_color));
                    break;

                case TEST_ITEM_STATUS_CANCELED:
                    holder.resultView.setVisibility(View.VISIBLE);
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    holder.resultView.setText(R.string.canceled);
                    holder.resultView
                            .setTextColor(getResources().getColor(R.color.test_failed_color));
                    break;

                case TEST_ITEM_STATUS_WAIT_FINGER_INPUT:
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.normal_touch_sensor);
                    holder.resultView.setTextColor(getResources().getColor(R.color.fg_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;

                case TEST_ITEM_STATUS_WAIT_TWILL_INPUT:
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.snr_touch_sensor);
                    holder.resultView.setTextColor(getResources().getColor(R.color.fg_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;

                case TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT:
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.bad_point_touch_sensor);
                    holder.resultView.setTextColor(getResources().getColor(R.color.fg_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;

                case TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT:
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.real_finger_touch_sensor);
                    holder.resultView.setTextColor(getResources().getColor(R.color.fg_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;

                case TEST_ITEM_STATUS_ENROLLING:
                    holder.resultView.setVisibility(View.INVISIBLE);
                    updateHolderTestingView(holder, type, View.VISIBLE);
                    holder.iconAnimationDrawable.start();
                    holder.progressBar.setProgress(PROGRESS_BAR_MAX
                            * (mConfig.mEnrollingMinTemplates - mEnrollmentRemaining)
                            / mEnrollmentSteps);
                    break;

                case TEST_ITEM_STATUS_AUTHENGICATING: {
                    StringBuilder sb = new StringBuilder();
                    sb.append(mFailedAttempts);
                    sb.append("/");
                    sb.append(MAX_FAILED_ATTEMPTS);
                    holder.resultView.setVisibility(View.INVISIBLE);
                    updateHolderTestingView(holder, type, View.VISIBLE);
                    holder.iconAnimationDrawable.start();
                    holder.retryView.setText(sb.toString());
                    break;
                }

                case TEST_ITEM_STATUS_WAIT_FINGER_DOWN: {
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.wait_finger_down_tip);
                    holder.resultView.setTextColor(getResources().getColor(R.color.fg_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;
                }

                case TEST_ITEM_STATUS_WAIT_FINGER_UP: {
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.wait_finger_up_tip);
                    holder.resultView.setTextColor(getResources().getColor(R.color.fg_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
                    break;
                }

                case TEST_ITEM_STATUS_NO_SUPPORT: {
                    holder.resultView.setVisibility(View.VISIBLE);
                    holder.resultView.setText(R.string.test_no_support);
                    holder.resultView.setTextColor(getResources().getColor(R.color.test_succeed_color));
                    updateHolderTestingView(holder, type, View.INVISIBLE);
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
            RelativeLayout testingViewUntrustEnroll;
            LinearLayout testingViewUntrustAuthenticate;
            HoloCircularProgressBar progressBar;
            AnimatedVectorDrawable iconAnimationDrawable;
            TextView retryView;
        }
    }

    public void getTimeout() {
        try {
            Class<?> systemPropertiesClazz = Class.forName("android.os.SystemProperties");
            Method method = systemPropertiesClazz.getMethod("getLong", new Class[]{
                    String.class, long.class
            });
            mAutoTestTimeout = (Long) method.invoke(null, new Object[]{
                    Constants.PROPERTY_TEST_ITME_TIMEOUT,
                    Constants.TEST_TIMEOUT_MS
            });
            Log.i(TAG, "getTimeout mAutoTestTimeout = " + mAutoTestTimeout);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException");
        }
    }

    private void onTestSpi(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_SPI end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_PRIOR_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_SPI) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_SPI failed1");
            spiTestR = 2;
            saveTestResult(TestResultChecker.TEST_SPI, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_SPI, result);

        boolean success = mTestResultChecker.checkSpiTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_SPI succeed");
            spiTestR = 1;
            saveTestResult(TestResultChecker.TEST_SPI, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_SPI failed2");
            spiTestR = 2;
            saveTestResult(TestResultChecker.TEST_SPI, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestResetPin(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_RESET_PIN end");
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_RESET_PIN) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_RESET_PIN failed1");
            resetTestR = 2;
            saveTestResult(TestResultChecker.TEST_RESET_PIN, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_RESET_PIN, result);

        boolean success = mTestResultChecker.checkResetPinTestReuslt(result);

        if (success) {
            Log.d(TAG, "TEST_RESET_PIN succeed");
            resetTestR = 1;
            saveTestResult(TestResultChecker.TEST_RESET_PIN, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_RESET_PIN failed2");
            resetTestR = 2;
            saveTestResult(TestResultChecker.TEST_RESET_PIN, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestInterruptPin(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_INTERRUPT_PIN end");
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_INTERRUPT_PIN) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_INTERRUPT_PIN failed1");
            intTestR = 2;
            saveTestResult(TestResultChecker.TEST_INTERRUPT_PIN, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_INTERRUPT_PIN, result);

        boolean success = mTestResultChecker.checkInterruptPinTestReuslt(result);

        if (success) {
            Log.d(TAG, "TEST_INTERRUPT_PIN succeed");
            intTestR = 1;
            saveTestResult(TestResultChecker.TEST_INTERRUPT_PIN, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_INTERRUPT_PIN failed2");
            intTestR = 2;
            saveTestResult(TestResultChecker.TEST_INTERRUPT_PIN, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestSensor(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_PIXEL end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_PIXEL) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_PIXEL failed1");
            pixelTestR = 2;
            saveTestResult(TestResultChecker.TEST_PIXEL, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_PIXEL, result);

        boolean success = mTestResultChecker.checkPixelTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_PIXEL succeed");
            pixelTestR = 1;
            saveTestResult(TestResultChecker.TEST_PIXEL, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_PIXEL failed2");
            pixelTestR = 2;
            saveTestResult(TestResultChecker.TEST_PIXEL, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestSensorShortStreak(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_PIXEL_SHORT_STREAK end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_PIXEL_SHORT_STREAK) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_PIXEL_SHORT_STREAK failed1");
            pixelShortTestR = 2;
            saveTestResult(TestResultChecker.TEST_PIXEL_SHORT_STREAK, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_PIXEL_SHORT_STREAK, result);

        boolean success = mTestResultChecker.checkPixelShortStreakTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_PIXEL_SHORT_STREAK succeed");
            pixelShortTestR = 1;
            saveTestResult(TestResultChecker.TEST_PIXEL_SHORT_STREAK, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_PIXEL_SHORT_STREAK failed2");
            pixelShortTestR = 2;
            saveTestResult(TestResultChecker.TEST_PIXEL_SHORT_STREAK, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestSensorFine(HashMap<Integer, Object> result) {
        Log.d(TAG, "onTestSensorFine");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_SENSOR_FINE) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "result is null");
            saveTestResult(TestResultChecker.TEST_SENSOR_FINE, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_SENSOR_FINE, result);

        boolean success = mTestResultChecker.checkSensorFineTestResult(result);

        if (success) {
            Log.d(TAG, "test sensor fine succeed");
            saveTestResult(TestResultChecker.TEST_SENSOR_FINE, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "test sensor fine failed");
            saveTestResult(TestResultChecker.TEST_SENSOR_FINE, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestFWVersion(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_FW_VERSION end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_FW_VERSION) != TEST_ITEM_STATUS_TESTING) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_FW_VERSION failed1");
            saveTestResult(TestResultChecker.TEST_FW_VERSION, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_FW_VERSION, result);

        boolean success = mTestResultChecker.checkFwVersionTestResult(result);
        if (success) {
            Log.d(TAG, "TEST_FW_VERSION succeed");
            saveTestResult(TestResultChecker.TEST_FW_VERSION, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_FW_VERSION failed2");
            saveTestResult(TestResultChecker.TEST_FW_VERSION, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestBadPoint(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_BAD_POINT end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus
                .get(TestResultChecker.TEST_BAD_POINT) != TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_BAD_POINT failed1");
            saveTestResult(TestResultChecker.TEST_BAD_POINT, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_BAD_POINT, result);

        boolean success = mTestResultChecker.checkBadPointTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_BAD_POINT succeed");
            saveTestResult(TestResultChecker.TEST_BAD_POINT, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_BAD_POINT failed2");
            saveTestResult(TestResultChecker.TEST_BAD_POINT, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestPerformance(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_PERFORMANCE end");

        resetBioAssay();
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus
                .get(TestResultChecker.TEST_PERFORMANCE) != TEST_ITEM_STATUS_WAIT_FINGER_INPUT) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_PERFORMANCE failed1");
            perfTestR = 2;
            saveTestResult(TestResultChecker.TEST_PERFORMANCE, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_PERFORMANCE, result);

        boolean success = mTestResultChecker.checkPerformanceTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_PERFORMANCE succeed");
            perfTestR = 1;
            saveTestResult(TestResultChecker.TEST_PERFORMANCE, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_PERFORMANCE failed2");
            perfTestR = 2;
            saveTestResult(TestResultChecker.TEST_PERFORMANCE, TEST_ITEM_STATUS_FAILED);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void onTestCapture(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_CAPTURE end");

        resetBioAssay();
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_CAPTURE) != TEST_ITEM_STATUS_WAIT_FINGER_INPUT) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_CAPTURE failed1");
            saveTestResult(TestResultChecker.TEST_CAPTURE, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_CAPTURE, result);

        boolean success = mTestResultChecker.checkCaptureTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_CAPTURE succeed");
            saveTestResult(TestResultChecker.TEST_CAPTURE, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_CAPTURE failed2");
            saveTestResult(TestResultChecker.TEST_CAPTURE, TEST_ITEM_STATUS_FAILED);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void onTestAlgo(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_ALGO end");

        resetBioAssay();
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(TestResultChecker.TEST_ALGO) != TEST_ITEM_STATUS_WAIT_FINGER_INPUT) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_ALGO failed1");
            saveTestResult(TestResultChecker.TEST_ALGO, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_ALGO, result);

        boolean success = mTestResultChecker.checkAlgoTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_ALGO succeed");
            saveTestResult(TestResultChecker.TEST_ALGO, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_ALGO failed2");
            saveTestResult(TestResultChecker.TEST_ALGO, TEST_ITEM_STATUS_FAILED);
        }

        if (mAutoTest) {
            TestHistoryUtils.addResult("fingerup");
        }

        mAdapter.notifyDataSetChanged();
    }

    private void onTestBioCalibrationWithFingerTouch(HashMap<Integer, Object> result) {
        resetBioAssay();
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (result == null) {
            Log.e(TAG, "TEST_BIO_CALIBRATION failed1");
            saveTestResult(TestResultChecker.TEST_BIO_CALIBRATION, TEST_ITEM_STATUS_FAILED);
            saveTestDetail(TestResultChecker.TEST_BIO_CALIBRATION, mPendingBioDetail);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_BIO_CALIBRATION, mPendingBioDetail, result);
        mPendingBioDetail = null;

        boolean success = mTestResultChecker.checkBioTestResultWithTouched(result);
        if (!success) {
            Log.e(TAG, "TEST_BIO_CALIBRATION step 2 failed1");
            saveTestResult(TestResultChecker.TEST_BIO_CALIBRATION, TEST_ITEM_STATUS_FAILED);
        } else {
            saveTestResult(TestResultChecker.TEST_BIO_CALIBRATION, TEST_ITEM_STATUS_SUCCEED);
        }
    }

    private void onTestBioCalibrationWithFingerUntouch(HashMap<Integer, Object> result) {
        if (result == null) {
            Log.e(TAG, "TEST_BIO_CALIBRATION failed1");
            saveTestResult(TestResultChecker.TEST_BIO_CALIBRATION, TEST_ITEM_STATUS_FAILED);
            return;
        }

        boolean success = mTestResultChecker.checkBioTestResultWithoutTouched(result);

        if (!success) {
            Log.e(TAG, "TEST_BIO_CALIBRATION step 1 failed1");
            saveTestResult(TestResultChecker.TEST_BIO_CALIBRATION, TEST_ITEM_STATUS_FAILED);
            saveTestDetail(TestResultChecker.TEST_BIO_CALIBRATION, result, null);
        } else {
            mPendingBioDetail = result;
            mTestStatus.put(TestResultChecker.TEST_BIO_CALIBRATION,
                    TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT);
            mAdapter.notifyDataSetChanged();
            startCheckFingerDownStatus();
        }
        if (mAutoTest) {
            TestHistoryUtils.addResult("finger down 2");
        }
    }

    private void onTestHbdCalibrationStep1(HashMap<Integer, Object> result) {
        if (result == null) {
            Log.e(TAG, "TEST_HBD_CALIBRATION failed1");
            saveTestResult(TestResultChecker.TEST_HBD_CALIBRATION, TEST_ITEM_STATUS_FAILED);
            return;
        }

        boolean success = mTestResultChecker.checkBioTestResultWithTouched(result);

        if (!success) {
            Log.e(TAG, "TEST_HBD_CALIBRATION step 1 failed1");
            saveTestResult(TestResultChecker.TEST_HBD_CALIBRATION, TEST_ITEM_STATUS_FAILED);
            saveTestDetail(TestResultChecker.TEST_HBD_CALIBRATION, result, null);
        } else {
            mPendingBioDetail = result;
            mAdapter.notifyDataSetChanged();
            startTestHbdCalibrationStep2();
        }
    }

    private void onTestHbdCalibrationStep2(HashMap<Integer, Object> result) {
        resetBioAssay();
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (result == null) {
            Log.e(TAG, "TEST_HBD_CALIBRATION failed1");
            saveTestResult(TestResultChecker.TEST_HBD_CALIBRATION, TEST_ITEM_STATUS_FAILED);
            saveTestDetail(TestResultChecker.TEST_HBD_CALIBRATION, mPendingBioDetail);
            return;
        }
        Log.d(TAG, "onTestHbdCalibrationStep2 -------------------------------");
        saveTestDetail(TestResultChecker.TEST_HBD_CALIBRATION, mPendingBioDetail, result);
        mPendingBioDetail = null;

        if (mAutoTest) {
            TestHistoryUtils.addResult("fingerup");
        }

        boolean success = mTestResultChecker.checkHBDTestResultWithTouched(result);
        if (!success) {
            Log.e(TAG, "TEST_HBD_CALIBRATION step 2 failed1");
            saveTestResult(TestResultChecker.TEST_HBD_CALIBRATION, TEST_ITEM_STATUS_FAILED);
        } else {
            saveTestResult(TestResultChecker.TEST_HBD_CALIBRATION, TEST_ITEM_STATUS_SUCCEED);
        }
    }

    private void onTestRawdataSaturated(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_RAWDATA_SATURATED end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus.get(
                TestResultChecker.TEST_RAWDATA_SATURATED) != TEST_ITEM_STATUS_WAIT_FINGER_INPUT) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_RAWDATA_SATURATED failed1");
            saveTestResult(TestResultChecker.TEST_RAWDATA_SATURATED, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_RAWDATA_SATURATED, result);

        boolean success = mTestResultChecker.checkRawdataSaturatedTestResult(result);
        int underSaturatedPixelCount = 0;
        int overSaturatedPixelCount = 0;
        int saturatedPixelThreshold = 0;

        if (success) {
            if (result.containsKey(TestResultParser.TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT)) {
                underSaturatedPixelCount = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT)) {
                overSaturatedPixelCount = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_SATURATED_PIXEL_THRESHOLD)) {
                saturatedPixelThreshold = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_SATURATED_PIXEL_THRESHOLD);
            }

            if (underSaturatedPixelCount > saturatedPixelThreshold
                    || overSaturatedPixelCount > saturatedPixelThreshold) {
                Log.e(TAG, "TEST_RAWDATA_CHECK failed2!");
                saveTestResult(TestResultChecker.TEST_RAWDATA_SATURATED, TEST_ITEM_STATUS_FAILED);
            } else {
                Log.e(TAG, "TEST_RAWDATA_CHECK succeed!");
                saveTestResult(TestResultChecker.TEST_RAWDATA_SATURATED, TEST_ITEM_STATUS_SUCCEED);
            }
        } else {
            Log.e(TAG, "TEST_RAWDATA_CHECK failed3!");
            saveTestResult(TestResultChecker.TEST_RAWDATA_SATURATED, TEST_ITEM_STATUS_FAILED);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void onTestFpcKeyEnStatus(final HashMap<Integer, Object> result) {
        int fpcKeyEn = 0;
        int fpcKeyFailStat = 0;
        int fpcCanTest = 0;
        int currentCmd = mAutoTestPosition - 1;

        if ((currentCmd > TEST_ITEM.length) || (currentCmd < 0)) {
            Log.d(TAG, "[onTestFpcKeyEnStatus] currentCmd out = " + currentCmd);
            return;
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_FAIL_STATUS)) {
            fpcKeyFailStat = 0x0FF & (Byte) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_FAIL_STATUS);
            Log.d(TAG, "test fpcKey fail status: " + fpcKeyFailStat);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_CAN_TEST)) {
            fpcCanTest = 0x0FF & (Byte) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_CAN_TEST);
            Log.d(TAG, "test fpcCanTest: " + fpcCanTest);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_EN_FLAG)) {
            fpcKeyEn = 0x0FF & (Byte) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_EN_FLAG);
            Log.d(TAG, "test fpc key en: " + Integer.toHexString(fpcKeyEn)
                    + "cmd index=" + currentCmd);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_RAWDATA)) {
            byte[] fpcKeyRawData = (byte[]) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_RAWDATA);
            short rawdata = (short) (fpcKeyRawData[0] & 0x00FF | fpcKeyRawData[1] << 8);
            Log.d(TAG, "test fpcKeyRawData: " + rawdata);
            for (int i = 0; i < fpcKeyRawData.length; i++) {
                Log.d(TAG, " " + Integer.toHexString(0xFF & fpcKeyRawData[i]));
            }
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_CANCELDATA)) {
            byte[] fpcKeyCancel = (byte[]) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_CANCELDATA);
            short canceldata = (short) (fpcKeyCancel[0] & 0x00FF | fpcKeyCancel[1] << 8);
            Log.d(TAG, "test canceldata: " + canceldata);
            for (int i = 0; i < fpcKeyCancel.length; i++) {
                Log.d(TAG, " " + Integer.toHexString(0xFF & fpcKeyCancel[i]));
            }
        }

        if (fpcCanTest == 0 || fpcKeyFailStat == 1) {
            saveTestResultOnly(TEST_ITEM[currentCmd], fpcCanTest == 0 ? TEST_ITEM_STATUS_NO_SUPPORT : TEST_ITEM_STATUS_FAILED);
            mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY_RESET_FWCFG, null);
            mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL, null);
            mHandler.removeCallbacks(mTimeoutRunnable);
            autoNextTest();
        }
        saveTestDetail(TEST_ITEM[currentCmd], result);

        mAdapter.notifyDataSetChanged();
    }

    private void onTestFpcKeyEvent(final HashMap<Integer, Object> result) {
        int event = 0;
        int status = 0;
        int currentCmd = mAutoTestPosition - 1;
        if ((currentCmd > TEST_ITEM.length) || (currentCmd < 0)) {
            Log.d(TAG, "[onTestFpcKeyEvent] currentCmd out = " + currentCmd);
            return;
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_EVENT)) {
            event = (Integer) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_EVENT);
        }
        if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_STATUS)) {
            status = (Integer) result.get(TestResultParser.TEST_TOKEN_FPC_KEY_STATUS);
        }

        Log.d(TAG, "onTestFpcKey event: " + event + " status: " + status + " currentcmd: "
                + currentCmd + " test item: " + TEST_ITEM[currentCmd]);
        if (event == MENU_KEY && TEST_ITEM[currentCmd] == TestResultChecker.TEST_FPC_MENU_KEY) {
            if (status == KEY_STATUS_DOWN) {
                saveTestResultOnly(TEST_ITEM[currentCmd], TEST_ITEM_STATUS_SUCCEED);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY_RESET_FWCFG, null);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL, null);
                autoNextTest();
            }
        } else if (event == BACK_KEY && TEST_ITEM[currentCmd] == TestResultChecker.TEST_FPC_BACK_KEY) {
            if (status == KEY_STATUS_DOWN) {
                saveTestResultOnly(TEST_ITEM[currentCmd], TEST_ITEM_STATUS_SUCCEED);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY_RESET_FWCFG, null);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL, null);
                autoNextTest();
            }
        } else if (event == RING_KEY && TEST_ITEM[currentCmd] == TestResultChecker.TEST_FPC_RING_KEY) {
            if (status == KEY_STATUS_DOWN) {
                saveTestResultOnly(TEST_ITEM[currentCmd], TEST_ITEM_STATUS_SUCCEED);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY_RESET_FWCFG, null);
                mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL, null);
                autoNextTest();
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void onTestStableFactor(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_STABLE_FACTOR end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (mTestStatus
                .get(TestResultChecker.TEST_STABLE_FACTOR) != TEST_ITEM_STATUS_WAIT_TWILL_INPUT) {
            return;
        }

        if (result == null) {
            Log.e(TAG, "TEST_STABLE_FACTOR failed1");
            saveTestResult(TestResultChecker.TEST_STABLE_FACTOR, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_STABLE_FACTOR, result);

        boolean success = mTestResultChecker.checkStableFactorTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_STABLE_FACTOR succeed");
            mIsPrevStablePassed = true;
            saveTestResult(TestResultChecker.TEST_STABLE_FACTOR, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_STABLE_FACTOR failed2");
            saveTestResult(TestResultChecker.TEST_STABLE_FACTOR, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestTwillBadpoint(final HashMap<Integer, Object> result) {
        Log.d(TAG, "onTestTwillBadpoint end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (result == null) {
            Log.e(TAG, "TEST_TWILL_BADPOINT failed1");
            saveTestResult(TestResultChecker.TEST_TWILL_BADPOINT, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_TWILL_BADPOINT, result);

        boolean success = mTestResultChecker.checkTwillBadpointResult(result);

        if (success) {
            Log.d(TAG, "TEST_TWILL_BADPOINT succeed");
            saveTestResult(TestResultChecker.TEST_TWILL_BADPOINT, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_TWILL_BADPOINT failed2");
            saveTestResult(TestResultChecker.TEST_TWILL_BADPOINT, TEST_ITEM_STATUS_FAILED);
        }
    }

    private void onTestSnr(final HashMap<Integer, Object> result) {
        Log.d(TAG, "TEST_SNR end");

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CANCEL);

        if (result == null) {
            Log.e(TAG, "TEST_SNR failed1");
            saveTestResult(TestResultChecker.TEST_SNR, TEST_ITEM_STATUS_FAILED);
            return;
        }

        saveTestDetail(TestResultChecker.TEST_SNR, result);

        boolean success = mTestResultChecker.checkSnrTestResult(result);

        if (success) {
            Log.d(TAG, "TEST_SNR succeed");
            saveTestResult(TestResultChecker.TEST_SNR, TEST_ITEM_STATUS_SUCCEED);
        } else {
            Log.e(TAG, "TEST_SNR failed2");
            saveTestResult(TestResultChecker.TEST_SNR, TEST_ITEM_STATUS_FAILED);
        }
    }

    private Runnable mCheckFingerupRunnable = new Runnable() {
        @Override
        public void run() {
            // get switch finger time as system property
            int switchTime = Constants.AUTO_TEST_BIO_PREPARE_TIME;
            try {
                switchTime = Integer.parseInt(getSystemPropertyAsString(Constants.PROPERTY_SWITCH_FINGER_TIME));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            Log.d(TAG, "switch finger time: " + switchTime);

            long millisUntilFinished = switchTime - mMillisStart;
            mCountDownDialog.setMessage(
                    getString(R.string.test_bio_waiting_start, millisUntilFinished / 1000));
            mMillisStart += 1000;

            String fingerStatus = getSystemPropertyAsString(Constants.PROPERTY_FINGER_STATUS);
            if ((fingerStatus != null && fingerStatus.equals("up")) || mMillisStart >= switchTime) {
                if (fingerStatus != null && fingerStatus.equals("up")) {
                    mCountDownDialog.setMessage(getString(R.string.test_bio_recived_up_message));
                }

                Log.d(TAG, "TEST_BIO_ASSAY start step 1");
                mMillisStart = 0;

                stopCountDownForSwitchFinger();
                startTestBioCalibration();
            } else {
                mHandler.postDelayed(mCheckFingerupRunnable, 1000);
            }
        }
    };

    private Runnable mCheckFingerDownRunnable = new Runnable() {
        @Override
        public void run() {
            String status = getSystemPropertyAsString(Constants.PROPERTY_FINGER_STATUS);
            if (status != null && status.equals("down")) {
                if (mTestStatus
                        .get(TestResultChecker.TEST_BIO_CALIBRATION) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT
                        || mTestStatus.get(
                        TestResultChecker.TEST_HBD_CALIBRATION) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT) {
                    startTestBioCalibration();
                }
            } else {
                Log.d(TAG, "check system properties again");
                mHandler.postDelayed(mCheckFingerDownRunnable, 500);
            }
        }
    };

    private void startCountDownForSwitchFinger() {
        mCountDownDialog = new AlertDialog.Builder(TouchTestActivity.this)
                .setTitle(getString(R.string.sytem_info))
                .setMessage(getString(R.string.test_bio_waiting_start,
                        Constants.AUTO_TEST_BIO_PREPARE_TIME) + "\n"
                        + getString(R.string.test_bio_no_finger_tips)).create();
        mCountDownDialog.setCancelable(false);
        mCountDownDialog.show();
        mHandler.post(mCheckFingerupRunnable);
    }

    private void stopCountDownForSwitchFinger() {
        if (mCountDownDialog != null) {
            mCountDownDialog.dismiss();
        }
        mHandler.removeCallbacks(mCheckFingerupRunnable);
    }

    private void startCheckFingerDownStatus() {
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_CHECK_FINGER_EVENT);
        mHandler.post(mCheckFingerDownRunnable);
    }

    private void startTestBioCalibration() {
        mHandler.removeCallbacks(mCheckFingerDownRunnable);
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_BIO_CALIBRATION);
    }

    private void startTestHbdCalibrationStep2() {
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_HBD_CALIBRATION);
    }

    private void checkResult() {
        if (!mAutoTest) {
            return;
        }

        boolean allSuccess = true;
        for (Integer test_item : TEST_ITEM) {
            if (mTestStatus.get(test_item) != TEST_ITEM_STATUS_SUCCEED) {
                allSuccess = false;
                break;
            }
        }

        if (allSuccess) {
            TestHistoryUtils.addResult("pass");
        } else {
            TestHistoryUtils.addResult("fail");
        }
        TestHistoryUtils.addResult("total time:"
                + (System.currentTimeMillis() - mAutoTestStartTime)
                + "ms");
    }

    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            stopTest(TEST_ITEM_STATUS_TIMEOUT);
        }

    };

    private TestCmdCallback mTestCmdCallback = new TestCmdCallback() {
        @Override
        public void onTestCmd(final int cmdId, final HashMap<Integer, Object> result) {
            Log.d(TAG, "onTestCmd " + Constants.testCmdIdToString(cmdId));

            if ((result == null || mTestResultChecker == null) && cmdId != Constants.CMD_INIT_CALLBACK) {
                Log.e(TAG, "GFManager may be wrong");
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (cmdId) {
                        case Constants.CMD_TEST_SPI:
                            if (mTestStatus.get(TestResultChecker.TEST_SPI) == TEST_ITEM_STATUS_TESTING) {
                                onTestSpi(result);
                            } else if (mTestStatus.get(
                                    TestResultChecker.TEST_FW_VERSION) == TEST_ITEM_STATUS_TESTING) {
                                onTestFWVersion(result);
                            }
                            break;

                        case Constants.CMD_TEST_PIXEL_OPEN:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_PIXEL) == TEST_ITEM_STATUS_TESTING) {
                                onTestSensor(result);
                            }
                            break;

                        case Constants.CMD_TEST_PIXEL_SHORT_STREAK:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_PIXEL_SHORT_STREAK) == TEST_ITEM_STATUS_TESTING) {
                                onTestSensorShortStreak(result);
                            }
                            break;

                        case Constants.CMD_TEST_RESET_PIN:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_RESET_PIN) == TEST_ITEM_STATUS_TESTING) {
                                onTestResetPin(result);
                            }
                            break;

                        case Constants.CMD_TEST_SENSOR_FINE:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_SENSOR_FINE) == TEST_ITEM_STATUS_TESTING) {
                                onTestSensorFine(result);
                            }
                            break;

                        case Constants.CMD_TEST_INTERRUPT_PIN:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_INTERRUPT_PIN) == TEST_ITEM_STATUS_TESTING) {
                                onTestInterruptPin(result);
                            }
                            break;

                        case Constants.CMD_TEST_BAD_POINT:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_BAD_POINT) == TEST_ITEM_STATUS_WAIT_BAD_POINT_INPUT) {
                                onTestBadPoint(result);
                            }
                            break;
                        case Constants.CMD_TEST_PERFORMANCE:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_ALGO) != null && (mTestStatus.get(
                                    TestResultChecker.TEST_ALGO) == TEST_ITEM_STATUS_WAIT_FINGER_INPUT)) {
                                onTestAlgo(result);
                            } else if (mTestStatus.get(
                                    TestResultChecker.TEST_CAPTURE) != null && (mTestStatus.get(
                                    TestResultChecker.TEST_CAPTURE) == TEST_ITEM_STATUS_WAIT_FINGER_INPUT)) {
                                onTestCapture(result);
                            } else if (mTestStatus.get(
                                    TestResultChecker.TEST_PERFORMANCE) != null && (mTestStatus.get(
                                    TestResultChecker.TEST_PERFORMANCE) == TEST_ITEM_STATUS_WAIT_FINGER_INPUT)) {
                                onTestPerformance(result);
                            }
                            break;

                        case Constants.CMD_TEST_SENSOR_VALIDITY:
                            if (null != mDialog) {
                                mDialog.cancel();
                            }
                            if (result.containsKey(TestResultParser.TEST_TOKEN_SENSOR_VALIDITY)) {
                                mSensorValidityTestFlag = (Integer) result
                                        .get(TestResultParser.TEST_TOKEN_SENSOR_VALIDITY);
                            }

                            mIsSensorValidityTested = true;
                            if (0 == mSensorValidityTestFlag) {
                                Log.i(TAG, "Sensor validity test Fail");
                                new AlertDialog.Builder(TouchTestActivity.this)
                                        .setTitle(TouchTestActivity.this.getString(R.string.sytem_info))
                                        .setMessage(TouchTestActivity.this
                                                .getString(R.string.sensor_validity_test_fail))
                                        .setPositiveButton(TouchTestActivity.this.getString(R.string.ok),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        // finish();
                                                    }
                                                })
                                        .show();
                            } else {
                                // the trick to update view to gray
                                mAutoTestingTitleView.setEnabled(true);
                                mAdapter.notifyDataSetChanged();
                                Log.i(TAG, "Sensor validity test Pass");
                            }
                            break;

                        case Constants.CMD_TEST_BIO_CALIBRATION:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_BIO_CALIBRATION) == TEST_ITEM_STATUS_TESTING) {
                                // step 1
                                onTestBioCalibrationWithFingerUntouch(result);
                            } else if (mTestStatus.get(
                                    TestResultChecker.TEST_BIO_CALIBRATION) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT) {
                                // step 2
                                onTestBioCalibrationWithFingerTouch(result);
                            } else if (mTestStatus.get(
                                    TestResultChecker.TEST_HBD_CALIBRATION) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT) {
                                onTestHbdCalibrationStep1(result);
                            }
                            break;
                        case Constants.CMD_TEST_HBD_CALIBRATION:
                            onTestHbdCalibrationStep2(result);
                            break;
                        case Constants.CMD_TEST_CHECK_FINGER_EVENT:
                            if (mTestStatus
                                    .get(TestResultChecker.TEST_BIO_CALIBRATION) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT
                                    || mTestStatus.get(
                                    TestResultChecker.TEST_HBD_CALIBRATION) == TEST_ITEM_STATUS_WAIT_REAL_FINGER_INPUT) {
                                startTestBioCalibration();
                            }
                            break;
                        case Constants.CMD_TEST_RAWDATA_SATURATED:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_RAWDATA_SATURATED) == TEST_ITEM_STATUS_WAIT_FINGER_INPUT) {
                                onTestRawdataSaturated(result);
                            }
                            break;

                        case Constants.CMD_TEST_FPC_KEY: {
                            if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_EN_FLAG)) {
                                onTestFpcKeyEnStatus(result);
                            } else if (result.containsKey(TestResultParser.TEST_TOKEN_FPC_KEY_EVENT)) {
                                onTestFpcKeyEvent(result);
                            }
                            break;
                        }

                        case Constants.CMD_TEST_STABLE_FACTOR:
                            if (mTestStatus.get(
                                    TestResultChecker.TEST_STABLE_FACTOR) == TEST_ITEM_STATUS_WAIT_TWILL_INPUT) {
                                onTestStableFactor(result);
                            }
                            break;

                        case Constants.CMD_TEST_TWILL_BADPOINT:
                            onTestTwillBadpoint(result);
                            break;

                        case Constants.CMD_TEST_NOISE:
                            onTestSnr(result);
                            break;

                        case Constants.CMD_INIT_CALLBACK: {
                            mConfig = mGoodixFingerprintManager.getConfig();

                            Log.i(TAG, "mConfig.mChipSeries = " + mConfig.mChipSeries + "; mConfig.mChipType = " + mConfig.mChipType);
                            if (null != mConfig && mConfig.mChipType != Constants.GF_CHIP_UNKNOWN) {
                                mTestResultChecker = TestResultChecker.getInstance().getTestResultCheckerFactory().createCheckerByChip(mConfig.mChipSeries, mConfig.mChipType);
                                TEST_ITEM = TEST_ITEM_DUBAI_A_SERIES_AUTO;
                                // set default enrolling min templates
                                mEnrollmentSteps = mConfig.mEnrollingMinTemplates;
                                mEnrollmentRemaining = mEnrollmentSteps;
                            }
                            initView();

                            // save result to "/data/data/com.goodix.gftest/files/testtool.txt"
                            TestHistoryUtils.init(getFilesDir().getPath(), "testtool.txt", "testdetail.txt");


                            if (null != mConfig && (mConfig.mChipSeries == Constants.GF_MILAN_F_SERIES
                                    || Constants.GF_MILAN_HV == mConfig.mChipSeries || mConfig.mChipSeries == Constants.GF_DUBAI_A_SERIES)) {
                                Log.d(TAG, "TEST_CHECK_SENSOR_TEST_INFO start");
                                if (mIsSensorValidityTested == false) {
                                    mSensorValidityTestFlag = 0;
                                    mAutoTestingTitleView.setEnabled(false);
                                    mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_SENSOR_VALIDITY);
                                    mDialog = new ProgressDialog(TouchTestActivity.this);
                                    mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    mDialog.setCancelable(true);
                                    mDialog.setCanceledOnTouchOutside(false);
                                    mDialog.setMessage(TouchTestActivity.this.getString(R.string.sensor_checking));
                                    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                        }
                                    });
                                    mDialog.show();
                                }
                            }

                            //if can't get sensor chip_id ,no need to start runnable
                            if (TEST_ITEM != null) {
                                getTimeout();
                            }
                        }
                    }
                }
            });
        }
    };

    private Runnable mAutoTestRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "check system properties");
            String status = "start";
            TEST_ITEM = TEST_ITEM_DUBAI_A_SERIES_AUTO;
            if (status != null && status.equals("start")) {
                mAutoTest = true;
                Log.d(TAG, "mAutoTestRunnable mAutoTest = " + mAutoTest);

                startAutoTest();
            } else {
                Log.d(TAG, "check system properties again");
                mHandler.postDelayed(mAutoTestRunnable, Constants.AUTO_TEST_TIME_INTERVAL);
            }
        }
    };

    private GoodixFingerprintManager.UntrustedEnrollmentCallback mEnrollmentCallback = new GoodixFingerprintManager.UntrustedEnrollmentCallback() {
        @Override
        public void onEnrollmentProgress(int fingerId, int remaining) {
            Log.d(TAG,
                    "onEnrollmentProgress fingerId = " + fingerId + ", remaining = " + remaining);
            mEnrollmentRemaining = remaining;
            mTestStatus.put(TestResultChecker.TEST_UNTRUSTED_ENROLL, TEST_ITEM_STATUS_ENROLLING);
            mAdapter.notifyDataSetChanged();

            if (mConfig.mEnrollingMinTemplates - remaining >= mEnrollmentSteps) {
                mEnrollmentCancel = null;
                saveTestResult(TestResultChecker.TEST_UNTRUSTED_ENROLL, TEST_ITEM_STATUS_SUCCEED);
                saveTestDetail(TestResultChecker.TEST_SPI, null);
            }
        }

        @Override
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            Log.d(TAG, "onEnrollmentHelp helpMsgId = " + helpMsgId);
        }

        @Override
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            Log.d(TAG, "onEnrollmentError errMsgId = " + errMsgId);

            mTestStatus.put(TestResultChecker.TEST_UNTRUSTED_ENROLL, TEST_ITEM_STATUS_FAILED);
            mAdapter.notifyDataSetChanged();
            mHandler.removeCallbacks(mTimeoutRunnable);

            autoNextTest();
        }

        @Override
        public void onEnrollmentAcquired(int acquireInfo) {
            Log.d(TAG, "onEnrollmentAcquired acquireInfo = " + acquireInfo);
        }
    };

    private UntrustedAuthenticationCallback mAuthCallback = new UntrustedAuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(int fingerId) {
            mAuthenticationCancel = null;
            saveTestResult(TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, TEST_ITEM_STATUS_SUCCEED);
            saveTestDetail(TestResultChecker.TEST_SPI, null);
        }

        @Override
        public void onAuthenticationFailed() {
            mFailedAttempts++;
            if (mFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                mAuthenticationCancel.cancel();
                mAuthenticationCancel = null;
                saveTestResult(TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE,
                        TEST_ITEM_STATUS_FAILED);
                saveTestDetail(TestResultChecker.TEST_SPI, null);
            } else {
                mTestStatus.put(TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE,
                        TEST_ITEM_STATUS_AUTHENGICATING);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        @Override
        public void onAuthenticationAcquired(int acquireInfo) {
        }
    };

    private String getSystemPropertyAsString(String propertyName) {
        String value = null;

        try {
            Class<?> systemPropertiesClazz = Class.forName("android.os.SystemProperties");
            Method method = systemPropertiesClazz.getMethod("get", String.class);
            value = (String) method.invoke(null, propertyName);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        }
        return value;
    }

    private void downLoadCfgForFPCKey() {
        byte[] cfgData = readFwCfgFile("GF5236_FpcEn.cfg");
        if (null == cfgData) {
            Log.e(TAG, "fail to read FW cfg file");
            return;
        }
        if (cfgData.length != GF_MILAN_A_SERIES_CFG_LENGTH && cfgData.length != GF_MILAN_AN_SERIES_CFG_LENGTH) {
            Log.e(TAG, "invalid cfg file, length err, len " + cfgData.length);
            showInvalidFileDialog(INVALID_CFG_FILE_LEN);
            return;
        }
        byte[] byteArray = new byte[TestParamEncoder.TEST_ENCODE_SIZEOF_INT32 + TestParamEncoder.testEncodeSizeOfArray(cfgData.length)];
        int offset = 0;
        offset = TestParamEncoder.encodeInt32(byteArray, offset, TestResultParser.TEST_TOKEN_FPC_DOWNLOAD_CFG, 1);
        TestParamEncoder.encodeArray(byteArray, offset, TestResultParser.TEST_PARAM_TOKEN_CFG_DATA, cfgData, cfgData.length);
        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_FPC_KEY_DOWNLOAD_CFG, byteArray);
    }

    public byte[] readFwCfgFile(String fileName) {
        byte[] buffer = null;
        InputStream fin = null;
        try {
            fin = getResources().getAssets().open(fileName);
            int length = fin.available();

            buffer = new byte[length];
            fin.read(buffer);
            fin.close();
        } catch (IOException e) {
            try {
                if (null != fin)
                    fin.close();
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            Log.e(TAG, "Failed to open " + fileName);
        }
        return buffer;
    }

    private void showInvalidFileDialog(int reason) {
        int message = R.string.invalid_fw_file;

        switch (reason) {
            case INVALID_FW_FILE_LEN:
                message = R.string.invalid_fw_file;
                break;

            case INVALID_FW_FILE_DATA:
                message = R.string.invalid_fw_file;
                break;

            case INVALID_CFG_FILE_LEN:
                message = R.string.invalid_cfg_file;
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.sytem_info))
                .setMessage(this.getString(message))
                .setPositiveButton(this.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }
}