/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils.checker;

import java.lang.Override;
import java.util.HashMap;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.checker.OswegoMChecker;
import com.goodix.gftest.utils.checker.MilanASeriesChecker;
import com.goodix.gftest.utils.checker.MilanFSeriesChecker;
import com.goodix.gftest.utils.checker.MilanHVSeriesChecker;
import com.goodix.gftest.utils.checker.MilanANSeriesChecker;

import android.util.Log;

public class TestResultChecker {

    private static final String TAG = "TestResultChecker";

    public static final int TEST_NONE = 0;
    public static final int TEST_SPI = 1;
    public static final int TEST_RESET_PIN = 2;
    public static final int TEST_INTERRUPT_PIN = 3;
    public static final int TEST_PIXEL = 4;
    public static final int TEST_BAD_POINT = 5;
    public static final int TEST_SENSOR_FINE = 6;
    public static final int TEST_PERFORMANCE = 7;
    public static final int TEST_CAPTURE = 8;
    public static final int TEST_ALGO = 9;
    public static final int TEST_FW_VERSION = 10;
    public static final int TEST_SENSOR_CHECK = 11;
    public static final int TEST_BIO_CALIBRATION = 12;
    public static final int TEST_HBD_CALIBRATION = 13;
    public static final int TEST_CODE_FW_VERSION = 14;
    public static final int TEST_RAWDATA_SATURATED = 15;
    public static final int TEST_UNTRUSTED_ENROLL = 16;
    public static final int TEST_UNTRUSTED_AUTHENTICATE = 17;
    public static final int TEST_FPC_MENU_KEY = 18;
    public static final int TEST_FPC_BACK_KEY = 19;
    public static final int TEST_FPC_RING_KEY = 20;
    public static final int TEST_STABLE_FACTOR = 21;
    public static final int TEST_TWILL_BADPOINT = 22;
    public static final int TEST_SNR = 23;
    public static final int TEST_PIXEL_SHORT_STREAK = 24;
    public static final int TEST_MAX = 25;

    private static TestResultChecker mInstance = null;
    private ITestResultCheckerFactory mTestResultCheckerFactory = null;
    private HashMap<Integer, Threshold> mThresMap = null;

    private TestResultChecker() {
        mTestResultCheckerFactory = new TestResultCheckerFactory();
    }

    public static TestResultChecker getInstance() {
        if (mInstance == null) {
            mInstance = new TestResultChecker();
        }
        return mInstance;
    }

    public ITestResultCheckerFactory getTestResultCheckerFactory() {
        Log.i(TAG, "getTestResultCheckerFactory");
        if (mTestResultCheckerFactory == null) {
            mTestResultCheckerFactory = new TestResultCheckerFactory();
        }
        return mTestResultCheckerFactory;
    }

    public Threshold getThreshold(int chipType) {
        if (mThresMap == null) {
            mThresMap = new HashMap<Integer, Threshold>();
        }
        Threshold thres = mThresMap.get(chipType);
        if (thres == null) {
            thres = new Threshold(chipType);
            mThresMap.put(chipType, thres);
        }

        return thres;
    }

    public class Threshold {
        public String spiFwVersion;
        public int chipId;
        public int chipId1 = 0;
        public int chipId2 = 0;
        public int badBointNum = 0;
        public int badBointDummyNum = 0;
        public int localBadBointNum = 0;
        public long totalTime = 0;
        public int imageQuality = 0;
        public int validArea = 0;
        public short avgDiffVal;
        public int badPixelNum = 0;
        public int badPixelShortStreakNum = 0;
        public int localBadPixelNum = 0;
        public float allTiltAngle = 0;
        public float blockTiltAngleMax = 0;
        public float stableValue = 0;
        public int twillBadpointMaxValue = 0;
        public int twillBadpointLocalValue = 0;
        public float snrMinValue = 0;
        public float snrMinValue2 = 0;
        public float snrMaxValue = 0;
        public short localWorst = 0;
        public int singular = 0;
        public int inCircle = 0;
        public int osdUntouched = 0;
        public int osdTouchedMin = 0;
        public int osdTouchedMax = 0;
        public int hbdAvgMax = 0;
        public int hbdAvgMin = 0;
        public int hbdElectricityMin = 0;
        public int hbdElectricityMax = 0;
        public int localSmallBadPixel = 0;
        public int localBigBadPixel = 0;
        public int averagePixelDiff = 0;
        public int fpcMenuRawdataMinVal = 0;
        public int fpcMenuRawdataMaxVal = 0;
        public int fpcBackRawdataMinVal = 0;
        public int fpcBackRawdataMaxVal = 0;
        public int fpcRingRawdataMinVal = 0;
        public int fpcRingRawdataMaxVal = 0;
        public int fpcMenuCancelationMinVal = 0;
        public int fpcMenuCancelationMaxVal = 0;
        public int fpcBackCancelationMinVal = 0;
        public int fpcBackCancelationMaxVal = 0;
        public int fpcRingCancelationMinVal = 0;
        public int fpcRingCancelationMaxVal = 0;

        private Threshold() {
            super();
            imageQuality = Constants.TEST_CAPTURE_VALID_IMAGE_QUALITY_THRESHOLD;
            validArea = Constants.TEST_CAPTURE_VALID_IMAGE_AREA_THRESHOLD;
            totalTime = Constants.TEST_PERFORMANCE_TOTAL_TIME;
        }

        private Threshold(int chipType) {
            this();
            switch (chipType) {
                case Constants.GF_CHIP_318M:
                case Constants.GF_CHIP_3118M:
                case Constants.GF_CHIP_518M:
                case Constants.GF_CHIP_5118M:
                    spiFwVersion = Constants.Oswego.TEST_SPI_GFX18;
                    badBointNum = Constants.Oswego.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.Oswego.TEST_BAD_POINT_BAD_PIXEL_NUM;
                    // localBadPixelNum = Constants.Oswego.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    avgDiffVal = Constants.Oswego.TEST_BAD_POINT_AVG_DIFF_VAL;
                    allTiltAngle = Constants.Oswego.TEST_BAD_POINT_ALL_TILT_ANGLE;
                    blockTiltAngleMax = Constants.Oswego.TEST_BAD_POINT_BLOCK_TILT_ANGLE_MAX;
                    localSmallBadPixel = Constants.Oswego.TEST_BAD_POINT_LOCAL_SMALL_BAD_POINT;
                    localBigBadPixel = Constants.Oswego.TEST_BAD_POINT_LOCAL_BIG_BAD_POINT;
                    averagePixelDiff = Constants.Oswego.AVERAGE_PIXEL_DIFF_THRESHOLD;
                    break;

                case Constants.GF_CHIP_316M:
                case Constants.GF_CHIP_516M:
                case Constants.GF_CHIP_816M:
                    spiFwVersion = Constants.Oswego.TEST_SPI_GFX16;
                    badBointNum = Constants.Oswego.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.Oswego.TEST_BAD_POINT_BAD_PIXEL_NUM;
                    // localBadPixelNum = Constants.Oswego.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    avgDiffVal = Constants.Oswego.TEST_BAD_POINT_AVG_DIFF_VAL;
                    allTiltAngle = Constants.Oswego.TEST_BAD_POINT_ALL_TILT_ANGLE;
                    blockTiltAngleMax = Constants.Oswego.TEST_BAD_POINT_BLOCK_TILT_ANGLE_MAX;
                    localSmallBadPixel = Constants.Oswego.TEST_BAD_POINT_LOCAL_SMALL_BAD_POINT;
                    localBigBadPixel = Constants.Oswego.TEST_BAD_POINT_LOCAL_BIG_BAD_POINT;
                    averagePixelDiff = Constants.Oswego.AVERAGE_PIXEL_DIFF_THRESHOLD;
                    break;

                case Constants.GF_CHIP_3208:
                    chipId = Constants.MilanF.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanF.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanF.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanF.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanF.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanF.TEST_BAD_POINT_LOCAL_WORST;

                    break;
                case Constants.GF_CHIP_3268:
                    chipId = Constants.MilanFN.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanFN.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanFN.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanFN.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanFN.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanFN.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_5288:
                case Constants.GF_CHIP_5288_CER:
                    chipId = Constants.MilanFN_HV.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanFN_HV.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanFN_HV.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanFN_HV.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanFN_HV.TEST_BAD_POINT_LOCAL_WORST;

                    break;
                case Constants.GF_CHIP_3206:
                    chipId = Constants.MilanG.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanG.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanG.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanG.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanG.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanG.TEST_BAD_POINT_LOCAL_WORST;
                    break;

                case Constants.GF_CHIP_3266:
                    chipId = Constants.MilanE.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanE.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanE.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanE.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanE.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanE.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_8206:
                case Constants.GF_CHIP_5296:
                case Constants.GF_CHIP_5296_CER:
                    chipId = Constants.MilanE_HV.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanE_HV.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanE_HV.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanE_HV.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanE_HV.TEST_BAD_POINT_LOCAL_WORST;
                    break;

                case Constants.GF_CHIP_3288:
                    chipId = Constants.MilanL.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanL.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanL.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanL.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanL.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3228:
                    chipId = Constants.MilanK.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanK.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanK.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanK.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanK.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanK.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3226:
                    chipId = Constants.MilanJ.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanJ.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanJ.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanJ.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanJ.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanJ.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_6226:
                    chipId = Constants.MilanJ_HV.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanJ_HV.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanJ_HV.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanJ_HV.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanJ_HV.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3258:
                    chipId = Constants.MilanH.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanH.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanH.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanH.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanH.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanH.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3258DN2:
                    chipId = Constants.MilanHU.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanHU.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanHU.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.MilanHU.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanHU.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanHU.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3216:
                    chipId = Constants.MilanN.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanN.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.MilanN.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.MilanN.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanN.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanN.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3658DN1:
                    chipId = Constants.ChicagoHU.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoHU.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoHU.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoHU.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoHU.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoHU.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3658DN2:
                    chipId = Constants.ChicagoH.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoH.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoH.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoH.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoH.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoH.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_5658ZS3:
                    chipId = Constants.ChicagoH.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoH.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoH.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoH.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoH.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoH.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3658DN3:
                    chipId = Constants.ChicagoHS.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoHS.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoHS.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoHS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoHS.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoHS.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3626ZS1:
                    chipId = Constants.ChicagoT.TEST_SPI_CHIP_ID;
                    chipId1 = Constants.ChicagoTC.TEST_SPI_CHIP_ID;
                    chipId2 = Constants.ChicagoTR.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoT.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoT.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoT.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoT.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoT.TEST_BAD_POINT_LOCAL_WORST;
                    stableValue = Constants.ChicagoT.TEST_STABLE_FACTOR_THRESHOLD;
                    snrMinValue = Constants.ChicagoT.TEST_SNR_MIN_THRESHOLD;
                    snrMaxValue = Constants.ChicagoT.TEST_SNR_MAX_THRESHOLD;
                    twillBadpointMaxValue = Constants.ChicagoT.TEST_TWILL_BADPOINT_MAX_THRESHOLD;
                    twillBadpointLocalValue = Constants.ChicagoT.TEST_TWILL_BADPOINT_LOCAL_THRESHOLD;
                    break;
                case Constants.GF_CHIP_3636ZS1:
                    chipId = Constants.ChicagoS.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoS.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoS.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoS.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoS.TEST_BAD_POINT_LOCAL_WORST;
                    stableValue = Constants.ChicagoS.TEST_STABLE_FACTOR_THRESHOLD;
                    snrMinValue = Constants.ChicagoS.TEST_SNR_MIN_THRESHOLD;
                    snrMaxValue = Constants.ChicagoS.TEST_SNR_MAX_THRESHOLD;
                    twillBadpointMaxValue = Constants.ChicagoS.TEST_TWILL_BADPOINT_MAX_THRESHOLD;
                    twillBadpointLocalValue = Constants.ChicagoS.TEST_TWILL_BADPOINT_LOCAL_THRESHOLD;
                    badPixelShortStreakNum = Constants.ChicagoS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_SHORT_STREAK_NUM;
                    break;
                case Constants.GF_CHIP_3988:
                    chipId = Constants.DubaiA.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.DubaiA.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.DubaiA.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.DubaiA.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.DubaiA.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.DubaiA.TEST_BAD_POINT_LOCAL_WORST;
                    stableValue = Constants.DubaiA.TEST_STABLE_FACTOR_THRESHOLD;
                    snrMinValue = Constants.DubaiA.TEST_SNR_MIN_THRESHOLD;
                    snrMinValue2 = Constants.DubaiA.TEST_SNR_MIN_THRESHOLD2;
                    snrMaxValue = Constants.DubaiA.TEST_SNR_MAX_THRESHOLD;
                    twillBadpointMaxValue = Constants.DubaiA.TEST_TWILL_BADPOINT_MAX_THRESHOLD;
                    twillBadpointLocalValue = Constants.DubaiA.TEST_TWILL_BADPOINT_LOCAL_THRESHOLD;
                    badPixelShortStreakNum = Constants.DubaiA.TEST_BAD_POINT_TOTAL_BAD_PIXEL_SHORT_STREAK_NUM;
                    break;
                case Constants.GF_CHIP_3956:
                    chipId = Constants.DubaiB.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.DubaiB.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.DubaiB.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.DubaiB.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.DubaiB.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.DubaiB.TEST_BAD_POINT_LOCAL_WORST;
                    stableValue = Constants.DubaiB.TEST_STABLE_FACTOR_THRESHOLD;
                    snrMinValue = Constants.DubaiB.TEST_SNR_MIN_THRESHOLD;
                    snrMinValue2 = Constants.DubaiB.TEST_SNR_MIN_THRESHOLD2;
                    snrMaxValue = Constants.DubaiB.TEST_SNR_MAX_THRESHOLD;
                    twillBadpointMaxValue = Constants.DubaiB.TEST_TWILL_BADPOINT_MAX_THRESHOLD;
                    twillBadpointLocalValue = Constants.DubaiB.TEST_TWILL_BADPOINT_LOCAL_THRESHOLD;
                    badPixelShortStreakNum = Constants.DubaiB.TEST_BAD_POINT_TOTAL_BAD_PIXEL_SHORT_STREAK_NUM;
                    break;
                case Constants.GF_CHIP_3976ZS1:
                    chipId = Constants.DubaiS.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.DubaiS.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.DubaiS.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.DubaiS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.DubaiS.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.DubaiS.TEST_BAD_POINT_LOCAL_WORST;
                    stableValue = Constants.DubaiS.TEST_STABLE_FACTOR_THRESHOLD;
                    snrMinValue = Constants.DubaiS.TEST_SNR_MIN_THRESHOLD;
                    snrMinValue2 = Constants.DubaiS.TEST_SNR_MIN_THRESHOLD2;
                    snrMaxValue = Constants.DubaiS.TEST_SNR_MAX_THRESHOLD;
                    twillBadpointMaxValue = Constants.DubaiS.TEST_TWILL_BADPOINT_MAX_THRESHOLD;
                    twillBadpointLocalValue = Constants.DubaiS.TEST_TWILL_BADPOINT_LOCAL_THRESHOLD;
                    badPixelShortStreakNum = Constants.DubaiS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_SHORT_STREAK_NUM;
                    break;
                case Constants.GF_CHIP_5658ZN1:
                    chipId = Constants.ChicagoHS.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoHS.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoHS.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoHS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoHS.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoHS.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_5658ZN2:
                    chipId = Constants.ChicagoHS.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoHS.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoHS.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoHS.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoHS.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoHS.TEST_BAD_POINT_LOCAL_WORST;
                    break;
                case Constants.GF_CHIP_3668DN1:
                    chipId = Constants.ChicagoCU.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoCU.TEST_SENSOR_BAD_POINT_COUNT;
                    localBadBointNum = Constants.ChicagoCU.TEST_SENSOR_LOCAL_BAD_POINT_COUNT;
                    badPixelNum = Constants.ChicagoCU.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoCU.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoCU.TEST_BAD_POINT_LOCAL_WORST;
                    break;

                case Constants.GF_CHIP_5228:
                case Constants.GF_CHIP_5298:
                    chipId = Constants.MilanHU.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.MilanHU.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.MilanHU.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanHU.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanHU.TEST_BAD_POINT_LOCAL_WORST;
                    break;

                case Constants.GF_CHIP_5206:
                case Constants.GF_CHIP_5216:
                    if (chipType == Constants.GF_CHIP_5206) {
                        spiFwVersion = Constants.MilanA.TEST_SPI_FW_VERSION;
                    } else {
                        spiFwVersion = Constants.MilanB.TEST_SPI_FW_VERSION;
                    }
                    badBointNum = Constants.MilanA.TEST_SENSOR_BAD_POINT_COUNT;
                    totalTime = Constants.MilanA.TEST_PERFORMANCE_TOTAL_TIME;

                    badPixelNum = Constants.MilanA.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanA.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanA.TEST_BAD_POINT_LOCAL_WORST;
                    inCircle = Constants.MilanA.TEST_BAD_POINT_INCIRCLE;

                    if (chipType == Constants.GF_CHIP_5206) {
                        osdUntouched = Constants.MilanA.TEST_BIO_THRESHOLD_UNTOUCHED;
                        osdTouchedMin = Constants.MilanA.TEST_BIO_THRESHOLD_TOUCHED_MIN;
                        osdTouchedMax = Constants.MilanA.TEST_BIO_THRESHOLD_TOUCHED_MAX;

                        hbdAvgMin = Constants.MilanA.TEST_HBD_THRESHOLD_AVG_MIN;
                        hbdAvgMax = Constants.MilanA.TEST_HBD_THRESHOLD_AVG_MAX;
                        hbdElectricityMin = Constants.MilanA.TEST_HBD_THRESHOLD_ELECTRICITY_MIN;
                        hbdElectricityMax = Constants.MilanA.TEST_HBD_THRESHOLD_ELECTRICITY_MAX;
                    }
                    break;

                case Constants.GF_CHIP_5208:
                case Constants.GF_CHIP_5218:

                    spiFwVersion = Constants.MilanC.TEST_SPI_FW_VERSION;
                    badBointNum = Constants.MilanC.TEST_SENSOR_BAD_POINT_COUNT;
                    totalTime = Constants.MilanC.TEST_PERFORMANCE_TOTAL_TIME;

                    badPixelNum = Constants.MilanC.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanC.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanC.TEST_BAD_POINT_LOCAL_WORST;
                    inCircle = Constants.MilanC.TEST_BAD_POINT_INCIRCLE;

                    if (chipType == Constants.GF_CHIP_5208) {
                        osdUntouched = Constants.MilanC.TEST_BIO_THRESHOLD_UNTOUCHED;
                        osdTouchedMin = Constants.MilanC.TEST_BIO_THRESHOLD_TOUCHED_MIN;
                        osdTouchedMax = Constants.MilanC.TEST_BIO_THRESHOLD_TOUCHED_MAX;

                        hbdAvgMin = Constants.MilanC.TEST_HBD_THRESHOLD_AVG_MIN;
                        hbdAvgMax = Constants.MilanC.TEST_HBD_THRESHOLD_AVG_MAX;
                        hbdElectricityMin = Constants.MilanC.TEST_HBD_THRESHOLD_ELECTRICITY_MIN;
                        hbdElectricityMax = Constants.MilanC.TEST_HBD_THRESHOLD_ELECTRICITY_MAX;
                    }
                    break;

                case Constants.GF_CHIP_5236:
                    spiFwVersion = Constants.MilanAn.TEST_SPI_FW_VERSION;
                    badBointNum = Constants.MilanAn.TEST_SENSOR_BAD_POINT_COUNT;
                    badBointDummyNum = Constants.MilanAn.TEST_SENSOR_BAD_POINT_DUMMY_COUNT;
                    totalTime = Constants.MilanAn.TEST_PERFORMANCE_TOTAL_TIME;

                    badPixelNum = Constants.MilanAn.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.MilanAn.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.MilanAn.TEST_BAD_POINT_LOCAL_WORST;
                    inCircle = Constants.MilanAn.TEST_BAD_POINT_INCIRCLE;

                    fpcMenuRawdataMinVal = Constants.MilanAn.TEST_FPC_MENU_RAWDATA_MIN;
                    fpcMenuRawdataMaxVal = Constants.MilanAn.TEST_FPC_MENU_RAWDATA_MAX;
                    fpcBackRawdataMinVal = Constants.MilanAn.TEST_FPC_BACK_RAWDATA_MIN;
                    fpcBackRawdataMaxVal = Constants.MilanAn.TEST_FPC_BACK_RAWDATA_MAX;
                    fpcRingRawdataMinVal = Constants.MilanAn.TEST_FPC_RING_RAWDATA_MIN;
                    fpcRingRawdataMaxVal = Constants.MilanAn.TEST_FPC_RING_RAWDATA_MAX;
                    fpcMenuCancelationMinVal = Constants.MilanAn.TEST_FPC_MENU_CANCEL_MIN;
                    fpcMenuCancelationMaxVal = Constants.MilanAn.TEST_FPC_MENU_CANCEL_MAX;
                    fpcBackCancelationMinVal = Constants.MilanAn.TEST_FPC_BACK_CANCEL_MIN;
                    fpcBackCancelationMaxVal = Constants.MilanAn.TEST_FPC_BACK_CANCEL_MAX;
                    fpcRingCancelationMinVal = Constants.MilanAn.TEST_FPC_RING_CANCEL_MIN;
                    fpcRingCancelationMaxVal = Constants.MilanAn.TEST_FPC_RING_CANCEL_MAX;

                break;

                case Constants.GF_CHIP_5628DN3:
                    chipId = Constants.ChicagoH_HV.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoH_HV.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.ChicagoH_HV.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoH_HV.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoH_HV.TEST_BAD_POINT_LOCAL_WORST;
                    break;

                case Constants.GF_CHIP_5628DN2:
                    chipId = Constants.ChicagoHS_HV.TEST_SPI_CHIP_ID;
                    badBointNum = Constants.ChicagoH_HV.TEST_SENSOR_BAD_POINT_COUNT;

                    badPixelNum = Constants.ChicagoH_HV.TEST_BAD_POINT_TOTAL_BAD_PIXEL_NUM;
                    localBadPixelNum = Constants.ChicagoH_HV.TEST_BAD_POINT_LOCAL_BAD_PIXEL_NUM;
                    localWorst = Constants.ChicagoH_HV.TEST_BAD_POINT_LOCAL_WORST;
                    break;

                default:
                    break;
            }

        }
    }

    public enum TestResultCheckerEnum {
        OswegoMChecker("com.goodix.gftest.utils.checker.OswegoMChecker"),
        MilanASeriesChecker("com.goodix.gftest.utils.checker.MilanASeriesChecker"),
        MilanFSeriesChecker("com.goodix.gftest.utils.checker.MilanFSeriesChecker"),
        MilanHVSeriesChecker("com.goodix.gftest.utils.checker.MilanHVSeriesChecker"),
        MilanANSeriesChecker("com.goodix.gftest.utils.checker.MilanANSeriesChecker");
        private String value = "";
        private TestResultCheckerEnum(String value) {
            this.value = value;
        }
        public String getValue() {
            return this.value;
        }
    }

    public interface ITestResultCheckerFactory {
        public Checker createCheckerByChip(int chipSeries, int chipType);
        public Checker createOswegoMChecker(int chipType);
        public Checker createMilanASeriesChecker(int chipType);
        public Checker createMilanFSeriesChecker(int chipType);
        public Checker createMilanHVSeriesChecker(int chipType);
        public Checker createMilanANSeriesChecker(int chipType);
    }

    public abstract class AbstractTestResultCheckerFactory implements ITestResultCheckerFactory {
        protected Checker createChecker(TestResultCheckerEnum testResultCheckerEnum) {
            Checker checker = null;
            if (!testResultCheckerEnum.getValue().equals("")) {
                try {
                    if(testResultCheckerEnum.getValue().equals("com.goodix.gftest.utils.checker.OswegoMChecker")){
                        checker=new OswegoMChecker();
                    }
                    else if(testResultCheckerEnum.getValue().equals("com.goodix.gftest.utils.checker.MilanASeriesChecker")){
                        checker=new MilanASeriesChecker();
                    }
                    else if(testResultCheckerEnum.getValue().equals("com.goodix.gftest.utils.checker.MilanFSeriesChecker")){
                        checker=new MilanFSeriesChecker();
                    }
                    else if(testResultCheckerEnum.getValue().equals("com.goodix.gftest.utils.checker.MilanHVSeriesChecker")){
                        checker=new MilanHVSeriesChecker();
                    }
                    else if(testResultCheckerEnum.getValue().equals("com.goodix.gftest.utils.checker.MilanANSeriesChecker")){
                        checker=new MilanANSeriesChecker();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return checker;
        }
    }

    public class TestResultCheckerFactory extends AbstractTestResultCheckerFactory {
        @Override
        public Checker createCheckerByChip(int chipSeries, int chipType) {
            Log.i(TAG, "chipSeries = " + chipSeries);
            Log.i(TAG, "chipType = " + chipType);
            switch (chipSeries) {
                case Constants.GF_OSWEGO_M: {
                    return createOswegoMChecker(chipType);
                }

                case Constants.GF_MILAN_F_SERIES:
                case Constants.GF_DUBAI_A_SERIES: {
                    return createMilanFSeriesChecker(chipType);
                }

                case Constants.GF_MILAN_A_SERIES: {
                    return createMilanASeriesChecker(chipType);
                }

                case Constants.GF_MILAN_HV: {
                    return createMilanHVSeriesChecker(chipType);
                }

                case Constants.GF_MILAN_AN_SERIES: {
                    return createMilanANSeriesChecker(chipType);
                }

                default:
                    return null;
            }
        }
        @Override
        public Checker createOswegoMChecker(int chipType) {
            Log.i(TAG, "createOswegoMChecker");
            Checker checker = super.createChecker(TestResultCheckerEnum.OswegoMChecker);
            if (checker!=null) {
                checker.setChipTypeAndInitThreshold(chipType);
            }
            return checker;
        }
        @Override
        public Checker createMilanASeriesChecker(int chipType) {
            Log.i(TAG, "createMilanASeriesChecker");
            Checker checker = super.createChecker(TestResultCheckerEnum.MilanASeriesChecker);
            if (checker!=null) {
                checker.setChipTypeAndInitThreshold(chipType);
            }
            return checker;
        }
        @Override
        public Checker createMilanFSeriesChecker(int chipType) {
            Log.i(TAG, "createMilanFSeriesChecker");
            Checker checker = super.createChecker(TestResultCheckerEnum.MilanFSeriesChecker);
            if (checker!=null) {
                checker.setChipTypeAndInitThreshold(chipType);
            }
            return checker;
        }
        @Override
        public Checker createMilanHVSeriesChecker(int chipType) {
            Log.i(TAG, "createMilanHVSeriesChecker");
            Checker checker = super.createChecker(TestResultCheckerEnum.MilanHVSeriesChecker);
            if (checker!=null) {
                checker.setChipTypeAndInitThreshold(chipType);
            }
            return checker;
        }
        @Override
        public Checker createMilanANSeriesChecker(int chipType) {
            Log.i(TAG, "createMilanANSeriesChecker");
            Checker checker = super.createChecker(TestResultCheckerEnum.MilanANSeriesChecker);
            if (checker!=null) {
                checker.setChipTypeAndInitThreshold(chipType);
            }
            return checker;
        }
    }

    public class CheckPoint {
        public int mErrorCode = 0;
        public short mAvgDiffVal = 0;
        public int mBadPixelNum = 0;
        public int mLocalBadPixelNum = 0;
        public float mAllTiltAngle = 0;
        public float mBlockTiltAngleMax = 0;
        public short mLocalWorst = 0;
        public int mSingular = 0;
        public short mInCircle = 0;
        public int mSmallBadPixel = 0;
        public int mBigBadPixel = 0;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("error code:").append(mErrorCode).append("\n")
            .append("avgDiffVal:").append(mAvgDiffVal).append("\n")
            .append("badPixelNum:").append(mBadPixelNum).append("\n")
            .append("localBadPixelNum:").append(mLocalBadPixelNum).append("\n")
            .append("allTiltAngle:").append(mAllTiltAngle).append("\n")
            .append("blockTiltAngleMax:").append(mBlockTiltAngleMax).append("\n")
            .append("localWorst:").append(mLocalWorst).append("\n")
            .append("singular:").append(mSingular).append("\n")
            .append("inCircle:").append(mInCircle).append("\n")
            .append("smallBadPixel:").append(mSmallBadPixel).append("\n")
            .append("bigBadPixel:").append(mBigBadPixel).append("\n");

            return builder.toString();
        }
    }
}

