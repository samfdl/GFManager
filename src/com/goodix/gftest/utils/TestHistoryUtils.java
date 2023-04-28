/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils;

import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.utils.checker.TestResultChecker;

import android.os.Environment;
import android.util.Log;

public class TestHistoryUtils {
    private static final String TAG = "TestHistoryUtils";
    private static final String TEST_HISTORY_FILE_NAME_FORMAT = "yyyyMMddHHmmss";
    private static String TEST_HISTORY_DIR_PATH = null;
    private static String TEST_HISTORY_DETAIL_FILE_PATH = null;
    private static String TEST_HISTORY_RESULT_FILE_PATH = null;

    public static void init(String rootPath, String resultFileName, String detailFileName) {
        if (rootPath == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                rootPath = Environment.getExternalStorageDirectory().getPath();
            }
        }

        StringBuilder dirPath = new StringBuilder();
        dirPath.append(rootPath);
        dirPath.append(File.separatorChar);
        dirPath.append(Constants.TEST_HISTORY_PATH);
        TEST_HISTORY_DIR_PATH = dirPath.toString();

        File dirFile = new File(TEST_HISTORY_DIR_PATH);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            if (!dirFile.mkdirs()) {
                Log.e(TAG, "create dir TEST_HISTORY_DIR_PATH fail");
            }
        }

        if (detailFileName == null) {
            SimpleDateFormat df = new SimpleDateFormat(TEST_HISTORY_FILE_NAME_FORMAT);
            detailFileName = "detail" + df.format(new Date()) + ".dat";
        }
        if (resultFileName == null) {
            SimpleDateFormat df = new SimpleDateFormat(TEST_HISTORY_FILE_NAME_FORMAT);
            resultFileName = "result" + df.format(new Date()) + ".dat";
        }

        StringBuilder detailFilePath = new StringBuilder();
        detailFilePath.append(TEST_HISTORY_DIR_PATH).append(File.separatorChar)
        .append(detailFileName);
        TEST_HISTORY_DETAIL_FILE_PATH = detailFilePath.toString();
        StringBuilder resultFilePath = new StringBuilder();
        resultFilePath.append(TEST_HISTORY_DIR_PATH).append(File.separatorChar)
        .append(resultFileName);
        TEST_HISTORY_RESULT_FILE_PATH = resultFilePath.toString();
    }

    public static void clearHistory() {
        File file = new File(TEST_HISTORY_DETAIL_FILE_PATH);
        if (file != null && file.exists()) {
            if (!file.delete()) {
                Log.e(TAG, "delete file TEST_HISTORY_DETAIL_FILE_PATH error");
            }
        }

        file = new File(TEST_HISTORY_RESULT_FILE_PATH);
        if (file != null && file.exists()) {
            if (!file.delete()) {
                  Log.e(TAG, "delete file TEST_HISTORY_RESULT_FILE_PATH error");
            }
        }
    }

    public static void addDetail(int testId, byte[] result) {
        if (TEST_HISTORY_DETAIL_FILE_PATH == null) {
            init(null, null, null);
        }

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(TEST_HISTORY_DETAIL_FILE_PATH, "rw");
            file.seek(file.length());

            if (testId > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("test_id=");
                sb.append(testId);
                sb.append("\n");
                file.writeBytes(sb.toString());
            }

            file.write(result);
            file.writeBytes("\n");
        } catch (IOException e) {
        }

        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
            }
        }
    }

    // dedicated patch function for TEST_BIO_ASSAY
    public static void addDetail(int testId, HashMap<Integer, Object> result1,
            HashMap<Integer, Object> result2) {
        StringBuilder sb = new StringBuilder();

        if (testId == TestResultChecker.TEST_BIO_CALIBRATION) {
            if (result1 != null) {
                sb.append("base0=").append(result1.get(TestResultParser.TEST_TOKEN_HBD_BASE))
                .append(",");
                sb.append("r0=").append(result1.get(TestResultParser.TEST_TOKEN_HBD_AVG))
                .append(",");
                sb.append("error_code=")
                .append(result1.get(TestResultParser.TEST_TOKEN_ERROR_CODE));
            }
            if (result2 != null) {
                sb.append("\n");
                sb.append("base1=").append(result2.get(TestResultParser.TEST_TOKEN_HBD_BASE))
                .append(",");
                sb.append("r1=").append(result2.get(TestResultParser.TEST_TOKEN_HBD_AVG))
                .append(",");
                sb.append("error_code=")
                .append(result2.get(TestResultParser.TEST_TOKEN_ERROR_CODE));
            }
        } else if (testId == TestResultChecker.TEST_HBD_CALIBRATION) {
            if (result1 != null) {
                sb.append("base2=").append(result1.get(TestResultParser.TEST_TOKEN_HBD_BASE))
                .append(",");
                sb.append("r2=").append(result1.get(TestResultParser.TEST_TOKEN_HBD_AVG))
                .append(",");
                sb.append("error_code=")
                .append(result1.get(TestResultParser.TEST_TOKEN_ERROR_CODE));
            }
            if (result2 != null) {
                sb.append("\n");
                sb.append("r3=").append(result2.get(TestResultParser.TEST_TOKEN_HBD_AVG))
                .append(",");
                sb.append("electricity=")
                .append(result2.get(TestResultParser.TEST_TOKEN_ELECTRICITY_VALUE))
                .append(",");
                sb.append("error_code=")
                .append(result2.get(TestResultParser.TEST_TOKEN_ERROR_CODE));
            }
        }
        addDetail(testId, sb.toString());
    }

    public static void addDetail(int testId, HashMap<Integer, Object> result) {
        StringBuilder sb = new StringBuilder();
        if (result != null) {
            for (Entry<Integer, Object> entry: result.entrySet()) {
                switch (entry.getKey()) {
                    case TestResultParser.TEST_TOKEN_ERROR_CODE:
                        sb.append("error_code=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_ALGO_VERSION:
                        sb.append("algo_version=");
                        sb.append(((String) entry.getValue()).trim());
                        break;

                    case TestResultParser.TEST_TOKEN_PREPROCESS_VERSION:
                        sb.append("preprocess_version=");
                        sb.append(((String) entry.getValue()).trim());
                        break;

                    case TestResultParser.TEST_TOKEN_FW_VERSION:
                        sb.append("fw_version=");
                        sb.append(((String) entry.getValue()).trim());
                        break;
                    case TestResultParser.TEST_TOKEN_SENSOR_OTP_TYPE:
                        sb.append("sensor_otp_type=");
                        sb.append(entry.getValue().toString().trim());
                        break;

                    case TestResultParser.TEST_TOKEN_CODE_FW_VERSION:
                        sb.append("code_fw_version=");
                        sb.append(((String) entry.getValue()).trim());
                        break;

                    case TestResultParser.TEST_TOKEN_TEE_VERSION:
                        sb.append("tee_version=");
                        sb.append(((String) entry.getValue()).trim());
                        break;

                    case TestResultParser.TEST_TOKEN_TA_VERSION:
                        sb.append("ta_version=");
                        sb.append(((String) entry.getValue()).trim());
                        break;

                    case TestResultParser.TEST_TOKEN_CHIP_ID:
                        sb.append("chip_id=");
                        byte[] chip_id = (byte[]) entry.getValue();
                        sb.append(TestResultParser.decodeInt32(chip_id, 0));
                        break;

                    case TestResultParser.TEST_TOKEN_VENDOR_ID:
                        sb.append("vendor_id=");
                        byte[] vendor_id = (byte[]) entry.getValue();
                        sb.append(Integer.toHexString(vendor_id[0]));
                        break;

                    case TestResultParser.TEST_TOKEN_SENSOR_ID:
                        sb.append("sensor_id=");
                        byte[] sensor_id = (byte[]) entry.getValue();
                        sb.append(sensor_id);
                        break;

                    case TestResultParser.TEST_TOKEN_PRODUCTION_DATE:
                        sb.append("production_date=");
                        byte[] production_date = (byte[]) entry.getValue();
                        sb.append(('0' + production_date[0]));
                        sb.append(('0' + production_date[1]));
                        sb.append(('0' + production_date[2]));
                        sb.append(('0' + production_date[3]));
                        sb.append(('0' + production_date[4]));
                        sb.append(('0' + production_date[5]));
                        break;

                    case TestResultParser.TEST_TOKEN_CHIP_TYPE:
                        sb.append("chip_type=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_PRODUCT_ID:
                        sb.append("product_id=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_CHIP_SERIES:
                        sb.append("chip_series=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AVG_DIFF_VAL:
                        sb.append("avg_diff_val=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_NOISE:
                        sb.append("noise=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_BAD_PIXEL_SHORT_STREAK_NUM:
                        sb.append("bad_pixel_short_streak_num=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM:
                        sb.append("bad_pixel_num=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM:
                        sb.append("local_bad_pixel_num=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_LOCAL_SMALL_BAD_PIXEL_NUM:
                        sb.append("local_small_bad_pixel=");
                        sb.append(entry.getValue());
                    break;

                    case TestResultParser.TEST_TOKEN_LOCAL_BIG_BAD_PIXEL_NUM:
                        sb.append("local_big_bad_pixel=");
                        sb.append(entry.getValue());
                    break;

                    case TestResultParser.TEST_TOKEN_ALL_TILT_ANGLE:
                        sb.append("all_tilt_angle=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_BLOCK_TILT_ANGLE_MAX:
                        sb.append("block_tilt_angle_max=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_LOCAL_WORST:
                        sb.append("local_worst=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_SINGULAR:
                        sb.append("singular=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AVG_BASE_RAWDATA:
                        sb.append("base_raw_data=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AVG_TOUCH_RAWDATA:
                        sb.append("touch_raw_data=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_MIN_TOUCH_RAWDATA:
                        sb.append("min_raw_data=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_CALIBRATION_PARA_RETEST_RESULT:
                        sb.append("result=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_IN_CIRCLE:
                        sb.append("in_circle=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_BIG_BUBBLE:
                        sb.append("big_bubble=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_LINE:
                        sb.append("line=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_GET_DR_TIMESTAMP_TIME:
                        sb.append("get_timestamp_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_GET_MODE_TIME:
                        sb.append("get_mode_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_GET_FW_VERSION_TIME:
                        sb.append("get_fw_version_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_GET_IMAGE_TIME:
                        sb.append("get_image_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_RAW_DATA_LEN:
                        sb.append("raw_data_len=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_IMAGE_QUALITY:
                        sb.append("image_quality=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_VALID_AREA:
                        sb.append("valid_area=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_KEY_POINT_NUM:
                        sb.append("key_point_num=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_INCREATE_RATE:
                        sb.append("increate_rate=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_OVERLAY:
                        sb.append("overlay=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_GET_RAW_DATA_TIME:
                        sb.append("get_raw_data_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_PREPROCESS_TIME:
                        sb.append("preprocess_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_GET_FEATURE_TIME:
                        sb.append("get_feature_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_ENROLL_TIME:
                        sb.append("enroll_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AUTHENTICATE_TIME:
                        sb.append("authenticate_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AUTHENTICATE_UPDATE_FLAG:
                        sb.append("authenticate_update_flag=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AUTHENTICATE_FINGER_COUNT:
                        sb.append("authenticate_finger_count=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_AUTHENTICATE_FINGER_ITME:
                        sb.append("authenticate_finger_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_TOTAL_TIME:
                        sb.append("total_time=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_RESET_FLAG:
                        sb.append("reset_flag=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_RAW_DATA:
                        sb.append("raw_data=data");
                        break;

                    case TestResultParser.TEST_TOKEN_AVERAGE_PIXEL_DIFF:
                        sb.append("average_pixel_diff=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT:
                        sb.append("under_saturated_pixels=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT:
                        sb.append("over_saturated_pixels=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_SATURATED_PIXEL_THRESHOLD:
                        sb.append("saturated_pixel_threshold=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_FPC_KEY_EN_FLAG:
                        sb.append("fpc_en=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_FPC_KEY_EVENT:
                        sb.append("fpc_key=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_FPC_KEY_STATUS:
                        sb.append("fpc_status=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_FPC_KEY_RAWDATA:
                        byte[] fpcKeyRawData = (byte[]) entry.getValue();
                        short rawdata = (short) (fpcKeyRawData[0] & 0x00FF | fpcKeyRawData[1] << 8);
                        sb.append("fpc_rawdata=");
                        sb.append(rawdata);
                        break;

                    case TestResultParser.TEST_TOKEN_FPC_KEY_CANCELDATA:
                        byte[] fpcKeyCancel = (byte[]) entry.getValue();
                        short canceldata = (short) (fpcKeyCancel[0] & 0x00FF | fpcKeyCancel[1] << 8);
                        sb.append("fpc_canceldata=");
                        sb.append(canceldata);
                        break;

                    case TestResultParser.TEST_TOKEN_STABLE_FACTOR_RESULT:
                        sb.append("stable_factor_value=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_TWILL_BADPOINT_TOTAL_RESULT:
                        sb.append("twill_badpoint_max=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_TWILL_BADPOINT_LOCAL_RESULT:
                        sb.append("twill_badpoint_local=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_TWILL_BADPOINT_NUMLOCAL_RESULT:
                        sb.append("twill_badpoint_numlocal=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_TWILL_BADPOINT_LINE_RESULT:
                        sb.append("twill_badpoint_line=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_DATA_NOISE_RESULT:
                        sb.append("snr_result_snr_value=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_DATA_NOISE_SIGNAL:
                        sb.append("snr_result_signal=");
                        sb.append(entry.getValue());
                        break;

                    case TestResultParser.TEST_TOKEN_DATA_NOISE_NOISE:
                        sb.append("snr_result_noise=");
                        sb.append(entry.getValue());
                        break;

                    default:
                        break;

                }
                sb.append(",");
            }
        }
        addDetail(testId, sb.toString());
    }

    public static void addDetail(int testId, String result) {
        if (result == null) {
            return;
        }
        try{
            addDetail(testId, result.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void addDetail(String result) {
        if (result == null) {
            return;
        }
        addDetail(0, result.getBytes());
    }

    public static void addResult(String result) {
        if (result == null) {
            return;
        }

        addResult(0, result);

    }

    public static void addResult(int testId, String result) {
        if (result == null) {
            return;
        }
        try{
            addResult(testId, result.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void addResult(int testId, byte[] result) {
        if (TEST_HISTORY_RESULT_FILE_PATH == null) {
            init(null, null, null);
        }

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(TEST_HISTORY_RESULT_FILE_PATH, "rw");
            file.seek(file.length());

            if (testId > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("test_id=");
                sb.append(testId);
                sb.append("\n");
                file.writeBytes(sb.toString());
            }

            file.write(result);
            file.writeBytes("\n");
        } catch (IOException e) {
        }

        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
            }
        }
    }

    private static void parseKVString(HashMap<String, Object> item, String kvStr) {
        String[] pair = kvStr.split("=");
        if (pair != null && pair.length > 1) {
            try {
                item.put(pair[0], pair[1]);
            } catch (NumberFormatException e) {
            }
        }
    }

    public static ArrayList<HashMap<String, Object>> load() {
        if (TEST_HISTORY_DETAIL_FILE_PATH == null) {
            return null;
        }

        ArrayList<HashMap<String, Object>> testResult = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> item = null;
        RandomAccessFile detailFile = null;
        RandomAccessFile resultFile = null;
        String line = null;
        try {
            resultFile = new RandomAccessFile(TEST_HISTORY_RESULT_FILE_PATH, "r");
            detailFile = new RandomAccessFile(TEST_HISTORY_DETAIL_FILE_PATH, "r");

            while (true) {
                // read the result from result file
                line = resultFile.readLine();
                if (line == null) {
                    break;
                }
                Log.d(TAG, line);
                if (line.startsWith("test_id")) {
                    item = new HashMap<String, Object>();
                    parseKVString(item, line);
                    continue; // read the next 'result' line
                } else if (line.startsWith("result")) {
                    if (null == item) {
                        Log.e(TAG, "result without test_id");
                        continue;
                    }

                    parseKVString(item, line);

                    // now turn to read the detail file
                    while (true) {
                        line = detailFile.readLine();
                        if (line == null) {
                            break;
                        }

                        Log.d(TAG, line);

                        if (line.contains("error_code")) {
                            Log.d(TAG, "error_code");
                            String[] array = line.split(",");
                            for (String result : array) {
                                parseKVString(item, result);
                            }
                        } else if (line.startsWith("time:")) {
                            Log.d(TAG, "time");
                            parseKVString(item, line);

                            break; // return to read the result file...pfff
                        }
                    }
                    testResult.add(item);
                }
            }

        } catch (IOException e) {
        }

        if (detailFile != null) {
            try {
                detailFile.close();
            } catch (IOException e) {
            }
        }

        if (resultFile != null) {
            try {
                resultFile.close();
            } catch (IOException e) {
            }
        }

        return testResult;
    }
}

