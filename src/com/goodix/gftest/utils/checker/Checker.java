/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils.checker;

import java.util.HashMap;

import android.util.Log;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.checker.TestResultChecker.CheckPoint;
import com.goodix.gftest.utils.checker.TestResultChecker.Threshold;

public abstract class Checker {

    private static final String TAG = "Checker";
    protected Threshold mThresHold;
    protected int mChipType;
    protected int mProductId;

    public Threshold getThresHold() {
        return mThresHold;
    }

    public abstract int[] getTestItems(int chipType);

    public abstract int[] getTestItemsByStatus(int index);

    public abstract int[] getDefaultTestItems();

    public void setChipTypeAndInitThreshold(int chipType) {
        mChipType = chipType;
        mThresHold = TestResultChecker.getInstance().getThreshold(mChipType);
    }

    private boolean checkErrcode(final HashMap<Integer, Object> result) {
        int errorCode = Constants.FINGERPRINT_ERROR_VENDOR_BASE;
        if (result.containsKey(TestResultParser.TEST_TOKEN_ERROR_CODE)) {
            errorCode = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_ERROR_CODE);
        }

        return (errorCode == 0);
    }

    public boolean checkSpiTestResult(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkSpiTestResult(int errCode, String fwVersion, int ChipId, int sensorOtpType) {
        return (errCode == 0);
    }

    public boolean checkResetPinTestReuslt(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int resetFlag = 0;
            if (result.containsKey(TestResultParser.TEST_TOKEN_RESET_FLAG)) {
                resetFlag = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_RESET_FLAG);
            } else if (result.containsKey("reset_flag")) {
                resetFlag = (Integer) result.get("reset_flag");
            }

            if (checkResetPinTestReuslt(0, resetFlag)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkResetPinTestReuslt(int errCode, int resetFlag) {
        return (errCode == 0) & (resetFlag > 0);
    }

    public boolean checkInterruptPinTestReuslt(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int errorCode = Constants.FINGERPRINT_ERROR_VENDOR_BASE;
            if (result.containsKey(TestResultParser.TEST_TOKEN_ERROR_CODE)) {
                errorCode = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_ERROR_CODE);
            }
            if (checkResetPinTestReuslt(errorCode, 1)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkInterruptPinTestReuslt(int errCode, int resetFlag) {
        return (errCode == 0) & (resetFlag > 0);
    }

    public boolean checkPixelTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int badPixelNum = 999;
            if (result.containsKey(TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM)) {
                badPixelNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM);
            }

            if (checkPixelTestResult(0, badPixelNum)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPixelTestResult(int errCode, int badPixelNum) {
        return (errCode == 0) & (badPixelNum <= mThresHold.badBointNum);
    }

    public boolean checkPixelShortStreakTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int badPixelShortStreakNum = 999;
            if (result.containsKey(TestResultParser.TEST_TOKEN_BAD_PIXEL_SHORT_STREAK_NUM)) {
                badPixelShortStreakNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_BAD_PIXEL_SHORT_STREAK_NUM);
            }

            if (checkPixelShortStreakTestResult(0, badPixelShortStreakNum)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPixelShortStreakTestResult(int errCode, int badPixelShortStreakNum) {
        return (errCode == 0) & (badPixelShortStreakNum <= mThresHold.badPixelShortStreakNum);
    }

    public boolean checkLocalPixelTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int localBadBointNum = 999;
            if (result.containsKey(TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM)) {
                localBadBointNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM);
            }

            if (checkLocalPixelTestResult(0, localBadBointNum)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkLocalPixelTestResult(int errCode, int localBadBointNum) {
        return (errCode == 0) & (localBadBointNum <= mThresHold.localBadBointNum);
    }

    public boolean checkSensorFineTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int averagePixelDiff = 2000;
            if (result.containsKey(TestResultParser.TEST_TOKEN_AVERAGE_PIXEL_DIFF)) {
                averagePixelDiff = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_AVERAGE_PIXEL_DIFF);
            }

            if (checkSensorFineTestResult(0, averagePixelDiff)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkSensorFineTestResult(int errCode, int averagePixelDiff) {
        return (errCode == 0) && (averagePixelDiff <= mThresHold.averagePixelDiff);
    }

    public boolean checkFwVersionTestResult(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkFwVersionTestResult(int errCode, String fwVersion, String codeFwVersion) {
        return (errCode == 0);
    }

    public boolean checkFwVersionTestResult(int errCode, String fwVersion, int sensorOtpType) {
        return (errCode == 0);
    }
    public boolean checkFwVersionTestResult(int errCode, String fwVersion, String codeFwVersion, int sensorOtpType) {
        return (errCode == 0);
    }

    public boolean checkPerformanceTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int totalTime = 0;
            if (result.containsKey(TestResultParser.TEST_TOKEN_TOTAL_TIME)) {
                totalTime = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_TOTAL_TIME);
            }

            if (checkPerformanceTestResult(0, totalTime)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkPerformanceTestResult(int errCode, int totalTime) {
        return (errCode == 0) && (totalTime < 500);
    }

    public boolean checkCaptureTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int imageQuality = 0;
            int validArea = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_IMAGE_QUALITY)) {
                imageQuality = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_IMAGE_QUALITY);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_VALID_AREA)) {
                validArea = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_VALID_AREA);
            }

            Log.d(TAG, "image quality: " + imageQuality + ", " + "validArea: " + validArea);

            if (checkCaptureTestResult(0, imageQuality, validArea)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkCaptureTestResult(int errCode, int imageQuality, int validArea) {
        return (errCode == 0) && (imageQuality >= mThresHold.imageQuality)
                && (validArea >= mThresHold.validArea);
    }

    public boolean checkAlgoTestResult(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkAlgoTestResult(int errCode) {
        return (errCode == 0);
    }

    public boolean checkStableFactorTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            float stableValue= 0.0f;
            if (result.containsKey(TestResultParser.TEST_TOKEN_STABLE_FACTOR_RESULT)) {
                stableValue = Float.parseFloat(result.get(TestResultParser.TEST_TOKEN_STABLE_FACTOR_RESULT).toString());
            }
            if (checkStableFactorTestResult(0, stableValue)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkStableFactorTestResult(int errCode, float stableValue) {
        return ((errCode == 0) && (stableValue < mThresHold.stableValue));
    }

    public boolean checkTwillBadpointResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            int maxValue = 0;
            int localValue = 0;
            int numLocalValue = 0;
            int lineValue = 0;
            if (result.containsKey(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_TOTAL_RESULT)) {
                maxValue = (Integer) result.get(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_TOTAL_RESULT);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_LOCAL_RESULT)) {
                localValue = (Integer) result.get(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_LOCAL_RESULT);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_NUMLOCAL_RESULT)) {
                numLocalValue = (Integer) result.get(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_NUMLOCAL_RESULT);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_LINE_RESULT)) {
                lineValue = (Integer) result.get(TestResultParser.TEST_TOKEN_TWILL_BADPOINT_LINE_RESULT);
            }
            Log.d(TAG, "maxValue: " + maxValue + ", " + "localValue " + localValue
                + ", " + "numLocalValue: " + numLocalValue + ", " + "lineValue " + lineValue);
            if (checkTwillBadpointResult(0, maxValue, localValue)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkTwillBadpointResult(int errCode, int maxValue, int localValue) {
        return ((errCode == 0) && (maxValue < mThresHold.twillBadpointMaxValue)
                && (localValue < mThresHold.twillBadpointLocalValue));
    }

    public boolean checkSnrTestResult(final HashMap<Integer, Object> result) {
        if (checkErrcode(result)) {
            float snrValue= 0.0f;
            int package_type = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_DATA_NOISE_RESULT)) {
                snrValue = Float.parseFloat(result.get(TestResultParser.TEST_TOKEN_DATA_NOISE_RESULT).toString());
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_PRODUCT_ID)
                && result.containsKey(TestResultParser.TEST_TOKEN_CHIP_TYPE))
            {
                mChipType = (Integer) result.get(TestResultParser.TEST_TOKEN_CHIP_TYPE);
                mProductId = (Integer) result.get(TestResultParser.TEST_TOKEN_PRODUCT_ID);
                package_type = (mProductId & 0xE000) >> 13;
                Log.d(TAG, "mProductId:" + mProductId);
                Log.d(TAG, "package_type:" + package_type);
                if (Constants.GF_CHIP_3956 == mChipType 
                    ||Constants.GF_CHIP_3988 == mChipType
                    ||Constants.GF_CHIP_3976ZS1 == mChipType)
                {
                    if (package_type == 0)
                    {
                        if (0x0623 == mProductId || 0x0624 == mProductId) { 
                            mThresHold.snrMinValue = mThresHold.snrMinValue2;
                            Log.d(TAG," HDK set snr min value2");
                        }
                        else{

                            Log.d(TAG," NDK set snr min value");
                        }

                    }
                    else if (package_type == 1)
                    {

                        mThresHold.snrMinValue = mThresHold.snrMinValue2;
                        Log.d(TAG," HDK set snr min value2");
                    }
                    else
                    {
                        Log.d(TAG, "other type ,to be determined"); 
                    }
                }
           }

            if (checkSnrTestResult(0, snrValue)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkSnrTestResult(int errCode, float snrValue) {

        return ((errCode == 0) && (snrValue >= mThresHold.snrMinValue)  && (snrValue <= mThresHold.snrMaxValue));
    }

    public boolean checkBadPointTestResult(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkBadPointTestResult(CheckPoint checkPoint) {
        return (checkPoint.mErrorCode == 0);
    }

    public boolean checkBioTestResultWithoutTouched(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkBioTestResultWithoutTouched(int errCode, int base, int avg) {
        return (errCode == 0);
    }

    public boolean checkBioTestResultWithTouched(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkBioTestResultWithTouched(int errCode, int base, int avg) {
        return (errCode == 0);
    }

    public boolean checkHBDTestResultWithTouched(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkHBDTestResultWithTouched(int errCode, int avg, int electricity) {
        return (errCode == 0);
    }

    public boolean checkRawdataSaturatedTestResult(final HashMap<Integer, Object> result) {
        return checkErrcode(result);
    }

    public boolean checkRawdataSaturatedTestResult(int errCode, int undersaturatedPixels,
            int oversaturatedPixels, int threshold) {
        if (0 == errCode && undersaturatedPixels < threshold
                && oversaturatedPixels < threshold) {
            return true;
        } else {
            return false;
        }
    }
}

