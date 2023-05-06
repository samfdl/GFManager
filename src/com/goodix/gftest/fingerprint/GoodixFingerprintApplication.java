/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.fingerprint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Application;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.goodix.fingerprint.service.GoodixFingerprintService;

public class GoodixFingerprintApplication extends Application {
    private static final String TAG = "GoodixFingerprintApplication";
    private GoodixFingerprintManager mGoodixFingerprintManager = null;

    private void getService(Context context) {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Log.d(TAG, "success to get ServiceManager");

            Method addService = serviceManager.getMethod("addService", String.class, IBinder.class);
            Log.d(TAG, "success to get method: addService");

            GoodixFingerprintService service = new GoodixFingerprintService(context);
            addService.invoke(null,
                    new Object[]{
                            Constants.GOODIX_FINGERPRINT_SERVICE_NAME, service
                    });
            Log.d(TAG, "success to addService: " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG, "InvocationTargetException");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}