/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils.checker;

import java.util.HashMap;

import android.util.Log;

import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.checker.TestResultChecker.CheckPoint;

public class MilanHVSeriesChecker extends Checker {
    private static final String TAG = "MilanFSeriesChecker";
    private final int[] TEST_ITEM_MILAN_F_SERIES = { //
            TestResultChecker.TEST_SPI, /**/
            TestResultChecker.TEST_RESET_PIN, /**/
            TestResultChecker.TEST_INTERRUPT_PIN, /**/
            TestResultChecker.TEST_PIXEL, /**/
            TestResultChecker.TEST_BAD_POINT, /**/
/*            TestResultChecker.TEST_PERFORMANCE,
            TestResultChecker.TEST_CAPTURE,
            TestResultChecker.TEST_ALGO */
    };

    public int[] getTestItems(int chipType) {
        return TEST_ITEM_MILAN_F_SERIES;
    }

    public int[] getTestItemsByStatus(int index) {
        return TEST_ITEM_MILAN_F_SERIES;
    }

    public int[] getDefaultTestItems(){
        return TEST_ITEM_MILAN_F_SERIES;
    }

    public MilanHVSeriesChecker() {
        Log.i(TAG, "MilanFSeriesChecker Constructor");
    }

    @Override
    public boolean checkSpiTestResult(HashMap<Integer, Object> result) {
        if (super.checkSpiTestResult(result)) {
            int chipID = 0;
            if (result.containsKey(TestResultParser.TEST_TOKEN_CHIP_ID)) {
                byte[] chip = (byte[]) result.get(TestResultParser.TEST_TOKEN_CHIP_ID);
                if (chip != null && chip.length >= 4) {
                    chipID = TestResultParser.decodeInt32(chip, 0) >> 8;
                }
            }
            if (checkSpiTestResult(0, null, chipID, 0)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkSpiTestResult(int errCode, String fwVersion, int chipId, int sensorOtpType) {
        return (errCode == 0) && (chipId == mThresHold.chipId);
    }

    @Override
    public boolean checkBadPointTestResult(HashMap<Integer, Object> result) {
        if (super.checkBadPointTestResult(result)) {

            CheckPoint checkPoint = TestResultChecker.getInstance().new CheckPoint();
            checkPoint.mBadPixelNum = 0;
            checkPoint.mLocalBadPixelNum = 0;
            checkPoint.mLocalWorst = 0;
            checkPoint.mSingular = 0;

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

            if (result.containsKey(TestResultParser.TEST_TOKEN_SINGULAR)) {
                checkPoint.mSingular = (Integer) result.get(TestResultParser.TEST_TOKEN_SINGULAR);
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
                && checkPoint.mLocalWorst < mThresHold.localWorst);
    }

}

