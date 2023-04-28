/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */
package com.goodix.gftest.memmanager;

public class MemoryNodeInfoSearchConfig {

    public static final int SORT_TYPE_BY_ADDR = 0;
    public static final int SORT_TYPE_BY_SIZE = 1;
    public static final int SORT_TYPE_BY_TIME = 2;
    public static final int SORT_TYPE_BY_CALLSTACK = 3;


    public static final int CALLSTACK_BY_ALL = 0;
    public static final int CALLSTACK_BY_ALGORITHM = 1;
    public static final int CALLSTACK_BY_TA = 2;

    public static final int MEMORY_NODE_USED_AND_FREE = 0;
    public static final int MEMORY_NODE_USED = 1;
    public static final int MEMORY_NODE_FREE = 2;

    int mMinSize;
    int mMaxSize;
    int mNodeType;
    int mSortType;
    int mCallStacKType;

    private MemoryNodeInfoSearchConfig(int nodeType, int sortType, int callStacKType, int minSize, int maxSize) {
        mNodeType = nodeType;
        mSortType = sortType;
        mCallStacKType = callStacKType;
        mMinSize = minSize;
        mMaxSize = maxSize;
    }

    public int getCallStacKType() {
        return mCallStacKType;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

    public int getMinSize() {
        return mMinSize;
    }

    public int getSortType() {
        return mSortType;
    }

    public int getStatus() {
        return mNodeType;
    }

    public static class Builder {
        int minSize = 0;
        int maxSize = Integer.MAX_VALUE;
        int nodeType = MEMORY_NODE_USED_AND_FREE;
        int sortType = SORT_TYPE_BY_ADDR;
        int callStacKType = CALLSTACK_BY_ALL;

        public Builder setCallStacKType(int callStacKType) {
            this.callStacKType = callStacKType;
            return this;
        }

        public Builder setMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder setMinSize(int minSize) {
            this.minSize = minSize;
            return this;
        }

        public Builder setSortType(int sortType) {
            this.sortType = sortType;
            return this;
        }

        public Builder setNodeType(int nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public MemoryNodeInfoSearchConfig build() {
            return new MemoryNodeInfoSearchConfig(nodeType, sortType, callStacKType, minSize, maxSize);
        }
    }
}
