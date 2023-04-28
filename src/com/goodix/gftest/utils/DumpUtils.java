/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils;

import android.os.Environment;
import android.util.Log;

public class DumpUtils {
    private static final String TAG = "DumpUtils";

    static
    {
        System.loadLibrary("dumpJni");
    }

    public static void handleDumpData(final byte[] data) {
        Log.d(TAG, "handleDumpData");
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        StringBuilder rootPath = new StringBuilder();
        rootPath.append(Environment.getExternalStorageDirectory().getPath());
        int ret = decodeAndWriteFile(data, data.length, rootPath.toString());

        Log.d(TAG, "handleDumpData, decoder return:" + ret);
        return;
    }

    private static native int decodeAndWriteFile(final byte[] data, int len, String rootDir);
}
