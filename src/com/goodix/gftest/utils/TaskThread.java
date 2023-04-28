/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.utils;

import android.os.HandlerThread;
import android.os.Looper;

public class TaskThread {

    private static TaskThread sTaskThread;

    private HandlerThread mThread;

    static private Object sLock = new Object();

    private TaskThread() {
        mThread = new HandlerThread("callback_handler_thread");
        mThread.start();
    }

    public static TaskThread getInstance() {
        TaskThread tmpThread;
        synchronized (sLock) {
            if (sTaskThread == null) {
                sTaskThread = new TaskThread();
            }
            tmpThread = sTaskThread;
        }
        return tmpThread;
    }

    public Looper getLooper() {
        return mThread.getLooper();
    }

    public boolean quit() {
        return mThread.quit();
    }
}

