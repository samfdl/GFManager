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

public class MilanASeriesChecker extends Checker {
    private static final String TAG = "MilanASeriesChecker";
    private final int[] TEST_ITEM_MILANA = { //
            TestResultChecker.TEST_SPI, /**/
            TestResultChecker.TEST_RESET_PIN, /**/
            TestResultChecker.TEST_INTERRUPT_PIN, /**/
            TestResultChecker.TEST_PIXEL, /**/
//            TestResultChecker.TEST_FW_VERSION, /**/
            TestResultChecker.TEST_BIO_CALIBRATION, /**/
            TestResultChecker.TEST_HBD_CALIBRATION, /**/
            // TestResultChecker.TEST_BAD_POINT, /**/
            TestResultChecker.TEST_PERFORMANCE, /**/
            TestResultChecker.TEST_CAPTURE, /**/
            TestResultChecker.TEST_ALGO, /**/
            TestResultChecker.TEST_UNTRUSTED_ENROLL, /**/
            TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, /**/
    };

    private final int[] TEST_ITEM_MILANA_1 = {
            TestResultChecker.TEST_SPI, /**/
            TestResultChecker.TEST_RESET_PIN, /**/
            TestResultChecker.TEST_INTERRUPT_PIN, /**/
            TestResultChecker.TEST_PIXEL, /**/
//            TestResultChecker.TEST_FW_VERSION, /**/
            // TestResultChecker.TEST_BAD_POINT, /**/
            TestResultChecker.TEST_PERFORMANCE, /**/
            TestResultChecker.TEST_CAPTURE, /**/
            TestResultChecker.TEST_ALGO, /**/
            TestResultChecker.TEST_UNTRUSTED_ENROLL, /**/
            TestResultChecker.TEST_UNTRUSTED_AUTHENTICATE, /**/
            TestResultChecker.TEST_BIO_CALIBRATION, /**/
            TestResultChecker.TEST_HBD_CALIBRATION, /**/
    };

    private final int[] TEST_ITEM_MILANB = { //
            TestResultChecker.TEST_SPI, /**/
            TestResultChecker.TEST_PIXEL, /**/
            TestResultChecker.TEST_RESET_PIN, /**/
            TestResultChecker.TEST_INTERRUPT_PIN, /**/
//            TestResultChecker.TEST_FW_VERSION, /**/
            // TestResultChecker.TEST_BAD_POINT, /**/
            TestResultChecker.TEST_PERFORMANCE, /**/
            TestResultChecker.TEST_CAPTURE, /**/
            TestResultChecker.TEST_ALGO, /**/
    };

    public int[] getTestItems(int chipType) {
        if (Constants.GF_CHIP_5216 == chipType) {
            return TEST_ITEM_MILANB;
        } else {
            return TEST_ITEM_MILANA;
        }
    }

    public int[] getTestItemsByStatus(int index) {
        switch (index) {
            case 0:
                return TEST_ITEM_MILANA;
            case 1:
                return TEST_ITEM_MILANA_1;
            case 2:
                return TEST_ITEM_MILANB;
            default:
                return TEST_ITEM_MILANA;
        }
    }

    public int[] getDefaultTestItems(){
        return getTestItems(Constants.GF_CHIP_UNKNOWN);
    }

    public MilanASeriesChecker() {
        Log.i(TAG, "MilanASeriesChecker Constructor");
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
    public boolean checkSpiTestResult(int errCode, String fwVersion, int ChipId, int sensorOtpType) {

        return (errCode == 0) && (fwVersion != null)
                && fwVersion.startsWith(mThresHold.spiFwVersion);
    }

    @Override
    public boolean checkFwVersionTestResult(HashMap<Integer, Object> result) {

        if (super.checkFwVersionTestResult(result)) {
            String fwVersion = "";
            String codeFwVersion = "";
            int sensorOtpType = 0;
            if (result.containsKey(TestResultParser.TEST_TOKEN_FW_VERSION)) {
                fwVersion = (String) result.get(TestResultParser.TEST_TOKEN_FW_VERSION);
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_SENSOR_OTP_TYPE)) {
                sensorOtpType = Integer.valueOf(result.get(TestResultParser.TEST_TOKEN_SENSOR_OTP_TYPE).toString());
            }
            if (result.containsKey(TestResultParser.TEST_TOKEN_CODE_FW_VERSION)) {
                codeFwVersion = (String) result
                        .get(TestResultParser.TEST_TOKEN_CODE_FW_VERSION);
            }
            Log.d(TAG, "sensorOtpType= " + sensorOtpType);
            if (checkFwVersionTestResult(0, fwVersion, codeFwVersion)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkFwVersionTestResult(int errCode, String fwVersion, int sensorOtpType) {

        return (errCode == 0) && (fwVersion != null);
    }

    @Override
    public boolean checkFwVersionTestResult(int errCode, String fwVersion, String codeFwVersion) {
        if (null == fwVersion || null == codeFwVersion) {
            return false;
        }
        fwVersion = fwVersion.trim();
        codeFwVersion = codeFwVersion.trim();
        Log.d(TAG, "errCode= " + errCode + ", fwVersion.trim()=" + fwVersion + ", codeFwVersion.trim()=" + codeFwVersion);
        return (errCode == 0) && (fwVersion.equals(codeFwVersion));
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

    @Override
    public boolean checkBioTestResultWithTouched(HashMap<Integer, Object> result) {

        if (super.checkBioTestResultWithTouched(result)) {
            int baseValue = 0;
            int avgValue = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_HBD_BASE)) {
                baseValue = (Short) result.get(TestResultParser.TEST_TOKEN_HBD_BASE);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_HBD_AVG)) {
                avgValue = (Short) result.get(TestResultParser.TEST_TOKEN_HBD_AVG);
            }

            if (checkBioTestResultWithTouched(0, baseValue, avgValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkBioTestResultWithTouched(int errCode, int base, int avg) {
        return (errCode == 0) && (Math.abs(base - avg) >= mThresHold.osdTouchedMin
                && (Math.abs(base - avg) <= mThresHold.osdTouchedMax));
    }

    @Override
    public boolean checkBioTestResultWithoutTouched(HashMap<Integer, Object> result) {

        if (super.checkBioTestResultWithTouched(result)) {
            int baseValue = 0;
            int avgValue = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_HBD_BASE)) {
                baseValue = (Short) result.get(TestResultParser.TEST_TOKEN_HBD_BASE);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_HBD_AVG)) {
                avgValue = (Short) result.get(TestResultParser.TEST_TOKEN_HBD_AVG);
            }

            if (checkBioTestResultWithoutTouched(0, baseValue, avgValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkBioTestResultWithoutTouched(int errCode, int base, int avg) {
        return (errCode == 0) && (Math.abs(base - avg) <= mThresHold.osdUntouched);
    }

    @Override
    public boolean checkHBDTestResultWithTouched(HashMap<Integer, Object> result) {
        if (super.checkHBDTestResultWithTouched(result)) {
            int avgValue = 0;
            int electricity = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_HBD_AVG)) {
                avgValue = (Short) result.get(TestResultParser.TEST_TOKEN_HBD_AVG);
            }

            if (result.containsKey(TestResultParser.TEST_TOKEN_ELECTRICITY_VALUE)) {
                electricity = (Integer) result
                        .get(TestResultParser.TEST_TOKEN_ELECTRICITY_VALUE);
            }

            if (checkHBDTestResultWithTouched(0, avgValue, electricity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkHBDTestResultWithTouched(int errCode, int avg, int electricity) {
        return (errCode == 0) && (avg <= mThresHold.hbdAvgMax && avg >= mThresHold.hbdAvgMin)
                && (electricity >= mThresHold.hbdElectricityMin
                        && electricity <= mThresHold.hbdElectricityMax);
    }

}

