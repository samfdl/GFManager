/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */
package com.goodix.gftest.memmanager;

import com.goodix.fingerprint.Constants;

public class MemoryManagerConfig {
    private boolean mDebug;
    private boolean mBestMatch;
    private boolean mEraseWhenFree;
    private boolean mRecordTime;
    private int mPollSize;

    private MemoryManagerConfig(boolean debug, boolean bestMatch, boolean eraseWhenFree, boolean recordTime, int pollSize) {
        mDebug = debug;
        mBestMatch = bestMatch;
        mEraseWhenFree = eraseWhenFree;
        mRecordTime = recordTime;
        mPollSize = pollSize;
    }

    public boolean isBestMatch() {
        return mBestMatch;
    }

    public boolean isDebug() {
        return mDebug;
    }

    public boolean isEraseWhenFree() {
        return mEraseWhenFree;
    }

    public boolean isRecordTime() {
        return mRecordTime;
    }

    public int getPollSize() {
        return mPollSize;
    }

    public static class Builder {
        private boolean debug = true;
        private boolean bestMatch = true;
        private boolean eraseWhenFree = true;
        private boolean recordTime = true;
        private int pollSize = Constants.TEST_MEM_MANAGER_MAX_HEAP_SIZE;

        public Builder() {
        }

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder setBestMatch(boolean bestMatch) {
            this.bestMatch = bestMatch;
            return this;
        }

        public Builder setEraseWhenFree(boolean eraseWhenFree) {
            this.eraseWhenFree = eraseWhenFree;
            return this;
        }

        public Builder setRecordTime(boolean record) {
            this.recordTime = record;
            return this;
        }

        public Builder setPollSize(int pollSize) {
            this.pollSize = pollSize;
            return this;
        }

        public MemoryManagerConfig build() {
            return new MemoryManagerConfig(debug, bestMatch, eraseWhenFree, recordTime, pollSize);
        }
    }
}
