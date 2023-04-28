/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils.checker;

import java.util.HashMap;
import android.util.Log;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.checker.TestResultChecker.CheckPoint;

public class MilanANSeriesChecker extends Checker {
    private static final String TAG = "MilanANSeriesChecker";

    public static final int[] TEST_ITEM_MILAN_BN = {
        TestResultChecker.TEST_SPI, /**/
        TestResultChecker.TEST_RESET_PIN, /**/
        TestResultChecker.TEST_INTERRUPT_PIN, /**/
        TestResultChecker.TEST_PIXEL, /**/
        //TestResultChecker.TEST_FPC_RING_KEY,
        TestResultChecker.TEST_PERFORMANCE, /**/
        TestResultChecker.TEST_CAPTURE, /**/
        TestResultChecker.TEST_ALGO, /**/
        //TestResultChecker.TEST_UNTRUSTED_ENROLL, /**/
        //TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, /**/
        TestResultChecker.TEST_BAD_POINT, /**/
        //TestResultChecker.TEST_FPC_MENU_KEY,
        //TestResultChecker.TEST_FPC_BACK_KEY,
    };

    @Override
    public int[] getTestItems(int chipType) {
        return TEST_ITEM_MILAN_BN;
    }

    @Override
    public int[] getTestItemsByStatus(int index) {
        return TEST_ITEM_MILAN_BN;
    }

    @Override
    public int[] getDefaultTestItems() {
        return TEST_ITEM_MILAN_BN;
    }

    @Override
    public boolean checkSpiTestResult(HashMap<Integer, Object> result) {
        if (super.checkSpiTestResult(result)) {
            String fwVersion = null;
            int sensorOtpType = 0;
            if (result.containsKey(TestResultParser.TEST_TOKEN_FW_VERSION)) {
                fwVersion = (String) result.get(TestResultParser.TEST_TOKEN_FW_VERSION);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_SENSOR_OTP_TYPE)) {
                sensorOtpType = Integer.valueOf(result.get(TestResultParser.TEST_TOKEN_SENSOR_OTP_TYPE).toString());
            }
            if (checkSpiTestResult(0, fwVersion, 0, sensorOtpType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkSpiTestResult(int errCode, String fwVersion,
            int ChipId, int sensorOtpType) {
        return (errCode == 0) && (fwVersion != null) && fwVersion.startsWith(mThresHold.spiFwVersion);
    }

    @Override
    public boolean checkFwVersionTestResult(HashMap<Integer, Object> result) {
        if (super.checkFwVersionTestResult(result)) {
            String fwVersion = null;
            String codeFwVersion = null;
            if (result.containsKey(TestResultParser.TEST_TOKEN_FW_VERSION)) {
                fwVersion = (String) result.get(TestResultParser.TEST_TOKEN_FW_VERSION);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_CODE_FW_VERSION)) {
                codeFwVersion = (String) result
                        .get(TestResultParser.TEST_TOKEN_CODE_FW_VERSION);
            }

            if (checkFwVersionTestResult(0, fwVersion, codeFwVersion)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkFwVersionTestResult(int errCode, String fwVersion,
            String codeFwVersion) {
        return (errCode == 0) && (fwVersion != null);
    }

    @Override
    public boolean checkFwVersionTestResult(int errCode, String fwVersion,
            int sensorOtpType) {
        return (errCode == 0) && (fwVersion != null);
    }

    @Override
    public boolean checkFwVersionTestResult(int errCode, String fwVersion,
            String codeFwVersion, int sensorOtpType) {
        if (null != fwVersion) {
            Log.d(TAG, "fwVersion.trim()=" + fwVersion.trim() + ", codeFwVersion="+codeFwVersion);
        } else {
            Log.d(TAG, "fwVersion=" + fwVersion + ", codeFwVersion="+codeFwVersion);
        }
        return (errCode == 0) && (fwVersion != null)
                && fwVersion.trim().equals(codeFwVersion.trim());
    }

    @Override
    public boolean checkResetPinTestReuslt(HashMap<Integer, Object> result) {
        return super.checkResetPinTestReuslt(result);
    }

    @Override
    public boolean checkResetPinTestReuslt(int errCode, int resetFlag) {
        return super.checkResetPinTestReuslt(errCode, resetFlag);
    }

    @Override
    public boolean checkInterruptPinTestReuslt(HashMap<Integer, Object> result) {
        return super.checkInterruptPinTestReuslt(result);
    }

    @Override
    public boolean checkInterruptPinTestReuslt(int errCode, int resetFlag) {
        return super.checkInterruptPinTestReuslt(errCode, resetFlag);
    }

    @Override
    public boolean checkPixelTestResult(HashMap<Integer, Object> result) {
        return super.checkPixelTestResult(result);
    }

    @Override
    public boolean checkPixelTestResult(int errCode, int badPixelNum) {
        return super.checkPixelTestResult(errCode, badPixelNum);
    }

    @Override
    public boolean checkPerformanceTestResult(HashMap<Integer, Object> result) {
        return super.checkPerformanceTestResult(result);
    }

    @Override
    public boolean checkPerformanceTestResult(int errCode, int totalTime) {
        return super.checkPerformanceTestResult(errCode, totalTime);
    }

    @Override
    public boolean checkCaptureTestResult(HashMap<Integer, Object> result) {
        return super.checkCaptureTestResult(result);
    }

    @Override
    public boolean checkCaptureTestResult(int errCode, int imageQuality,
            int validArea) {
        return super.checkCaptureTestResult(errCode, imageQuality, validArea);
    }

    @Override
    public boolean checkAlgoTestResult(HashMap<Integer, Object> result) {
        return super.checkAlgoTestResult(result);
    }

    @Override
    public boolean checkAlgoTestResult(int errCode) {
        return super.checkAlgoTestResult(errCode);
    }

    @Override
    public boolean checkBadPointTestResult(HashMap<Integer, Object> result) {
        if (super.checkBadPointTestResult(result)) {

            CheckPoint checkPoint = TestResultChecker.getInstance().new CheckPoint();
            checkPoint.mBadPixelNum = 0;
            checkPoint.mLocalBadPixelNum = 0;
            checkPoint.mLocalWorst = 0;
            checkPoint.mInCircle = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM)) {
                checkPoint.mBadPixelNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM)) {
                checkPoint.mLocalBadPixelNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_LOCAL_WORST)) {
                checkPoint.mLocalWorst = (Short) result
                        .get(TestResultParser.TEST_TOKEN_LOCAL_WORST);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_IN_CIRCLE)) {
                checkPoint.mInCircle = (Short) result.get(TestResultParser.TEST_TOKEN_IN_CIRCLE);
            }

            if (checkBadPointTestResult(checkPoint)) {
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean checkBadPointTestResult(CheckPoint checkPoint) {
        return (null != checkPoint) && (checkPoint.mErrorCode == 0) && (checkPoint.mBadPixelNum < mThresHold.badPixelNum
                && checkPoint.mLocalBadPixelNum < mThresHold.localBadPixelNum
                && checkPoint.mLocalWorst < mThresHold.localWorst && checkPoint.mInCircle < mThresHold.inCircle);
    }



}
