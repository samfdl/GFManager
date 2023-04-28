/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils.checker;

import java.util.HashMap;

import android.util.Log;

import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.checker.TestResultChecker.CheckPoint;

public class OswegoMChecker extends Checker {
    private static final String TAG = "OswegoMChecker";
    private final int[] TEST_ITEM_OSWEGO = { //
            TestResultChecker.TEST_SPI, /**/
            TestResultChecker.TEST_RESET_PIN, /**/
            TestResultChecker.TEST_INTERRUPT_PIN, /**/
            TestResultChecker.TEST_PIXEL, /**/
            TestResultChecker.TEST_BAD_POINT, /**/
            //TestResultChecker.TEST_SENSOR_FINE, /**/
            TestResultChecker.TEST_RAWDATA_SATURATED, /**/
            TestResultChecker.TEST_PERFORMANCE, /**/
            TestResultChecker.TEST_CAPTURE, /**/
            TestResultChecker.TEST_ALGO, /**/
            TestResultChecker.TEST_UNTRUSTED_ENROLL, /**/
            TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, /**/
            TestResultChecker.TEST_FW_VERSION
    };

    public int[] getTestItems(int chipType) {
        return TEST_ITEM_OSWEGO;
    }

    public int[] getTestItemsByStatus(int index) {
        return TEST_ITEM_OSWEGO;
    }

    public int[] getDefaultTestItems(){
        return TEST_ITEM_OSWEGO;
    }

    public OswegoMChecker() {
        Log.i(TAG, "OswegoMChecker Constructor");
    }

    @Override
    public boolean checkSpiTestResult(HashMap<Integer, Object> result) {
        if (super.checkSpiTestResult(result)) {
            String fwVersion = null;
            if (result.containsKey(TestResultParser.TEST_TOKEN_FW_VERSION)) {
                fwVersion = (String) result.get(TestResultParser.TEST_TOKEN_FW_VERSION);
            }

            if (checkSpiTestResult(0, fwVersion, 0, 0)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkSpiTestResult(int errCode, String fwVersion, int ChipId, int sensorOtpType) {

        return (errCode == 0)
                && (fwVersion != null && fwVersion.startsWith(mThresHold.spiFwVersion));
    }

    @Override
    public boolean checkFwVersionTestResult(HashMap<Integer, Object> result) {

        if (super.checkFwVersionTestResult(result)) {
            String fwVersion = "";
            String codeFwVersion = "";
            if (result.containsKey(TestResultParser.TEST_TOKEN_FW_VERSION)) {
                fwVersion = (String) result.get(TestResultParser.TEST_TOKEN_FW_VERSION);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_CODE_FW_VERSION)) {
                codeFwVersion = (String) result
                        .get(TestResultParser.TEST_TOKEN_CODE_FW_VERSION);
            }

            Log.d(TAG, "codeFwVersion: " + codeFwVersion + ", ");

            if (checkFwVersionTestResult(0, fwVersion, codeFwVersion)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkFwVersionTestResult(int errCode, String fwVersion, String codeFwVersion) {
        return (errCode == 0) && (fwVersion != null)
                && (fwVersion.startsWith(codeFwVersion));
    }

    @Override
    public boolean checkBadPointTestResult(HashMap<Integer, Object> result) {
        if (super.checkBadPointTestResult(result)) {
            CheckPoint checkPoint = TestResultChecker.getInstance().new CheckPoint();
            checkPoint.mAvgDiffVal = 0;
            checkPoint.mBadPixelNum = 0;
            checkPoint.mLocalBadPixelNum = 0;
            checkPoint.mAllTiltAngle = 0;
            checkPoint.mBlockTiltAngleMax = 0;
            checkPoint.mBigBadPixel = 0;
            checkPoint.mSmallBadPixel = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_AVG_DIFF_VAL)) {
                checkPoint.mAvgDiffVal = (Short) result
                        .get(TestResultParser.TEST_TOKEN_AVG_DIFF_VAL);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM)) {
                checkPoint.mBadPixelNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM)) {
                checkPoint.mLocalBadPixelNum = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_ALL_TILT_ANGLE)) {
                checkPoint.mAllTiltAngle = (Float) result
                        .get(TestResultParser.TEST_TOKEN_ALL_TILT_ANGLE);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_BLOCK_TILT_ANGLE_MAX)) {
                checkPoint.mBlockTiltAngleMax = (Float) result
                        .get(TestResultParser.TEST_TOKEN_BLOCK_TILT_ANGLE_MAX);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_LOCAL_SMALL_BAD_PIXEL_NUM)) {
                checkPoint.mSmallBadPixel = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_LOCAL_SMALL_BAD_PIXEL_NUM);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_LOCAL_BIG_BAD_PIXEL_NUM)) {
                checkPoint.mBigBadPixel = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_LOCAL_BIG_BAD_PIXEL_NUM);
            }

            if (checkBadPointTestResult(checkPoint)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkBadPointTestResult(CheckPoint checkPoint) {
        if(null == checkPoint) {
            return false;
        } else {
            Log.i(TAG, "checkPoint:" + checkPoint.toString());
            return  (checkPoint.mErrorCode == 0) && (checkPoint.mBadPixelNum < mThresHold.badPixelNum
                    && checkPoint.mSmallBadPixel < mThresHold.localSmallBadPixel
                    && checkPoint.mBigBadPixel < mThresHold.localBigBadPixel
                    && checkPoint.mAvgDiffVal > mThresHold.avgDiffVal
                    && checkPoint.mAllTiltAngle < mThresHold.allTiltAngle
                    && checkPoint.mBlockTiltAngleMax < mThresHold.blockTiltAngleMax);
        }
    }

}
