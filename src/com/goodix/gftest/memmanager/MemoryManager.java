/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.memmanager;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.goodix.fingerprint.utils.TestParamEncoder;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.gftest.memmanager.MemoryPoolInfo.MemoryNode;
import com.goodix.gftest.memmanager.MemoryPoolInfo.MemoryNode.MemoryNodeStatus;
import com.goodix.gftest.utils.TaskThread;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class MemoryManager {

    private static final String TAG = "MemoryManager";

    public static final int FLAG_ENABLE = 1 << 31;
    public static final int FLAG_ENABLE_REBOOT = 1 << 30;

    public static final int FLAG_CONFIG_DEBUG = 1 << 0;
    public static final int FLAG_CONFIG_BEST_MATCH = 1 << 1;
    public static final int FLAG_CONFIG_EARASE_WHEN_FREE = 1 << 2;
    public static final int FLAG_CONFIG_RECORD_TIME = 1 << 3;

    private static MemoryManager mInstatnce = null;

    private Context mContext;
    private Handler mTaskHandler;
    private Handler mUIHandler;
    private GoodixFingerprintManager mGoodixFingerprintManager;
    private MemoryManagerDataCallBack mMemoryManagerDataCallBack;
    private MemoryManagerConfig mMemoryConfig;

    private boolean mEnable = false;
    private boolean mInitial = false;
    private boolean mEnableReboot = false;

    public abstract static class MemoryManagerDataCallBack {
        public void onMemoryInfoFetched(MemoryPoolInfo info) {
        }

        public void onMemoryDumpFinished(String fileName) {
        }

        public void onMemoryConfigFetched(MemoryManagerConfig config) {
        }

        public void onMemoryManagerEnabled(boolean enable, boolean enableReboot) {
        }
    }

    public synchronized static MemoryManager getInstance(Context context) {
        if (null == mInstatnce) {
            mInstatnce = new MemoryManager(context.getApplicationContext());
        }

        return mInstatnce;
    }

    private MemoryManager(Context context) {
        mContext = context;
        mTaskHandler = new Handler(TaskThread.getInstance().getLooper());
        mUIHandler = new Handler(context.getApplicationContext().getMainLooper());
        mGoodixFingerprintManager = GoodixFingerprintManager.getFingerprintManager(context);
        mGoodixFingerprintManager.registerTestCmdCallback(mTestCmdCallback);
        mMemoryConfig = new MemoryManagerConfig.Builder().build();
    }

    public void setMemoryManagerDataCallBack(MemoryManagerDataCallBack callBack) {
        mMemoryManagerDataCallBack = callBack;
    }

    public void load() {
        Log.d(TAG, "load");

        if (mInitial) {
            if (mMemoryManagerDataCallBack != null) {
                mMemoryManagerDataCallBack.onMemoryManagerEnabled(mEnable, mEnableReboot);
                if (mEnable) {
                    mMemoryManagerDataCallBack.onMemoryConfigFetched(mMemoryConfig);
                }
            }
            return;
        }
        Log.i(TAG, "load memory manager not init");

        if (mGoodixFingerprintManager == null) {
            Log.e(TAG, "no GoodixFingerprintManager");
            return;
        }

        mGoodixFingerprintManager
                .registerTestCmdCallback(new GoodixFingerprintManager.TestCmdCallback() {
                    @Override
                    public void onTestCmd(int cmdId, HashMap<Integer, Object> result) {
                        if (Constants.CMD_TEST_MEMMGR_GET_CONFIG == cmdId) {
                            mEnable = false;
                            // get Enable;
                            if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_ENABLE)) {
                                mEnable = ((Integer) result
                                        .get(TestResultParser.TEST_TOKEN_MEMMGR_ENABLE)) > 0 ? true
                                        : false;
                            }

                            if (result
                                    .containsKey(TestResultParser.TEST_TOKEN_MEMMGR_NEXT_REBOOT_ENABLE)) {
                                mEnableReboot = (Integer) result
                                        .get(TestResultParser.TEST_TOKEN_MEMMGR_NEXT_REBOOT_ENABLE) > 0 ? true
                                        : false;
                            }

                            Log.d(TAG, "load enable: " + mEnable);
                            Log.d(TAG, "load enableReboot: " + mEnableReboot);
                            mGoodixFingerprintManager.registerTestCmdCallback(mTestCmdCallback);
                            // set initial flag
                            mInitial = true;

                            if (mMemoryManagerDataCallBack != null) {
                                mMemoryManagerDataCallBack.onMemoryManagerEnabled(mEnable,
                                        mEnableReboot);
                                if (mEnable) {
                                    mMemoryConfig = parseConfig(result);
                                    mMemoryManagerDataCallBack.onMemoryConfigFetched(mMemoryConfig);
                                }
                            }
                        }
                    }
                });

        mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_MEMMGR_GET_CONFIG, null);
    }

    public void setMemoryConfig(final MemoryManagerConfig config)
            throws MemoryManagerUnInitializationException, MemoryManagerDisableException {
        Log.d(TAG, "setMemoryConfig");
        setMemoryConfig(config, mMemoryManagerDataCallBack);
    }

    public void setMemoryConfig(final MemoryManagerConfig config, MemoryManagerDataCallBack callBack)
            throws MemoryManagerUnInitializationException, MemoryManagerDisableException {
        Log.d(TAG, "setMemoryConfig");

        if (!mInitial) {
            throw new MemoryManagerUnInitializationException();
        }

        if (!mEnable) {
            throw new MemoryManagerDisableException();
        }

        if (config != null) {

            if (mEnableReboot) {
                sendSetConfgCommand(mEnableReboot, config, callBack);
            }
        }
    }

    public void enableMemoryManager(final boolean enable) {
        Log.d(TAG, "enableMemoryManager");
        sendSetConfgCommand(enable, mMemoryConfig, mMemoryManagerDataCallBack);
    }

    public void enableMemoryManager(final boolean enable, final MemoryManagerConfig config) {
        Log.d(TAG, "enableMemoryManager");
        mEnableReboot = enable;
        sendSetConfgCommand(enable, config, mMemoryManagerDataCallBack);
    }

    private void sendSetConfgCommand(final boolean enable, final MemoryManagerConfig config,
            final MemoryManagerDataCallBack callBack) {

        mMemoryManagerDataCallBack = callBack;
        byte[] params = convertInitParams(enable, config);
        if (mGoodixFingerprintManager != null && params != null) {
            mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_MEMMGR_SET_CONFIG, params);
        }
    }

    public void fetchMemoryInfo() throws MemoryManagerUnInitializationException,
            MemoryManagerDisableException {
        Log.d(TAG, "fetchMemoryInfo");

        if (!mInitial) {
            throw new MemoryManagerUnInitializationException();
        }

        if (!mEnable) {
            throw new MemoryManagerDisableException();
        }

        if (mGoodixFingerprintManager != null) {
            mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_MEMMGR_GET_INFO);
        }
    }

    public void dumpMemory() throws MemoryManagerUnInitializationException,
            MemoryManagerDisableException {
        Log.d(TAG, "dumpMemory");

        if (!mInitial) {
            throw new MemoryManagerUnInitializationException();
        }

        if (!mEnable) {
            throw new MemoryManagerDisableException();
        }

        if (mGoodixFingerprintManager != null) {
            mGoodixFingerprintManager.testCmd(Constants.CMD_TEST_MEMMGR_DUMP_POOL);
        }
    }

    private byte[] convertInitParams(boolean enable, MemoryManagerConfig config) {
        if (config == null) {
            Log.e(TAG, "bad parameters");
            return null;
        }

        Log.d(TAG, "convertInitParams enable: " + enable);
        Log.d(TAG, "convertInitParams debug: " + config.isDebug());
        Log.d(TAG, "convertInitParams bestMatch: " + config.isBestMatch());
        Log.d(TAG, "convertInitParams eraseWhenFree: " + config.isEraseWhenFree());
        Log.d(TAG, "convertInitParams recordTime: " + config.isRecordTime());
        Log.d(TAG, "convertInitParams pollSize: " + config.getPollSize());

        byte[] dataBytes = new byte[TestParamEncoder.TEST_ENCODE_SIZEOF_INT32 * 6];
        int offset = 0;

        offset = TestParamEncoder.encodeInt32(dataBytes, offset,
                TestResultParser.TEST_TOKEN_MEMMGR_ENABLE, enable ? 1 : 0);
        offset = TestParamEncoder.encodeInt32(dataBytes, offset,
                TestResultParser.TEST_TOKEN_MEMMGR_DEBUG_ENABLE, config.isDebug() ? 1 : 0);
        offset = TestParamEncoder.encodeInt32(dataBytes, offset,
                TestResultParser.TEST_TOKEN_MEMMGR_BEST_MATCH_ENABLE, config.isBestMatch() ? 1 : 0);
        offset = TestParamEncoder.encodeInt32(dataBytes, offset,
                TestResultParser.TEST_TOKEN_MEMMGR_FREE_ERASE_ENABLE, config.isEraseWhenFree() ? 1
                        : 0);
        offset = TestParamEncoder.encodeInt32(dataBytes, offset,
                TestResultParser.TEST_TOKEN_MEMMGR_DUMP_TIME_ENABLE, config.isRecordTime() ? 1 : 0);
        offset = TestParamEncoder.encodeInt32(dataBytes, offset,
                TestResultParser.TEST_TOKEN_MEMMGR_POOL_SIZE, config.getPollSize());

        return dataBytes;
    }

    private MemoryManagerConfig parseConfig(final HashMap<Integer, Object> result) {
        boolean debug = true;
        boolean bestMatch = true;
        boolean eraseWhenFree = true;
        boolean recordTime = true;
        int pollSize = 0;

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_POOL_SIZE)) {
            pollSize = (Integer) result.get(TestResultParser.TEST_TOKEN_MEMMGR_POOL_SIZE);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_DEBUG_ENABLE)) {
            debug = (Integer) result.get(TestResultParser.TEST_TOKEN_MEMMGR_DEBUG_ENABLE) > 0 ? true
                    : false;
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_BEST_MATCH_ENABLE)) {
            bestMatch = (Integer) result.get(TestResultParser.TEST_TOKEN_MEMMGR_BEST_MATCH_ENABLE) > 0 ? true
                    : false;
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_FREE_ERASE_ENABLE)) {
            eraseWhenFree = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_MEMMGR_FREE_ERASE_ENABLE) > 0 ? true : false;
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_TIME_ENABLE)) {
            recordTime = (Integer) result.get(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_TIME_ENABLE) > 0 ? true
                    : false;
        }

        Log.d(TAG, "parseConfig debug: " + debug);
        Log.d(TAG, "parseConfig bestMatch: " + bestMatch);
        Log.d(TAG, "parseConfig eraseWhenFree: " + eraseWhenFree);
        Log.d(TAG, "parseConfig recordTime: " + recordTime);
        Log.d(TAG, "parseConfig pollSize: " + pollSize);

        MemoryManagerConfig.Builder builder = new MemoryManagerConfig.Builder();
        MemoryManagerConfig config = builder.setDebug(debug).setBestMatch(bestMatch).
                setEraseWhenFree(eraseWhenFree).setRecordTime(recordTime).setPollSize(pollSize)
                .build();

        return config;
    }

    private MemoryPoolInfo parsePollInfo(final HashMap<Integer, Object> result) {
        MemoryPoolInfo info = null;

        long startAddr = 0;
        long endAddr = 0;
        int usedNodeCount = 0;
        int usedMemorySize = 0;
        int maxNodeCount = 0;
        int maxMemorySize = 0;
        int totalNodeCount = 0;
        byte[] nodeInfo = null;

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_POOL_START_ADDR)) {
            startAddr = (Long) result.get(TestResultParser.TEST_TOKEN_MEMMGR_POOL_START_ADDR);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_POOL_END_ADDR)) {
            endAddr = (Long) result.get(TestResultParser.TEST_TOKEN_MEMMGR_POOL_END_ADDR);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_CUR_USED_BLOCK_COUNT)) {
            usedNodeCount = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_MEMMGR_CUR_USED_BLOCK_COUNT);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_CUR_USED_MEM_SIZE)) {
            usedMemorySize = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_MEMMGR_CUR_USED_MEM_SIZE);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_MAX_USED_BLOCK_COUNT)) {
            maxNodeCount = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_MEMMGR_MAX_USED_BLOCK_COUNT);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_MAX_USED_MEM_SIZE)) {
            maxMemorySize = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_MEMMGR_MAX_USED_MEM_SIZE);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_TOTAL_NODE_COUNT)) {
            totalNodeCount = (Integer) result
                    .get(TestResultParser.TEST_TOKEN_MEMMGR_TOTAL_NODE_COUNT);
        }

        Log.d(TAG, "parsePollInfo startAddr: " + startAddr);
        Log.d(TAG, "parsePollInfo endAddr: " + endAddr);
        Log.d(TAG, "parsePollInfo totalNodeCount: " + totalNodeCount);
        Log.d(TAG, "parsePollInfo usedNodeCount: " + usedNodeCount);
        Log.d(TAG, "parsePollInfo usedMemorySize: " + usedMemorySize);
        Log.d(TAG, "parsePollInfo maxNodeCount: " + maxNodeCount);
        Log.d(TAG, "parsePollInfo maxMemorySize: " + maxMemorySize);

        MemoryPoolInfo.MemoryPoolDebugInfo debugInfo = null;
        if (mMemoryConfig.isDebug()) {
            debugInfo = new MemoryPoolInfo.MemoryPoolDebugInfo(usedNodeCount, usedMemorySize,
                    maxNodeCount, maxMemorySize);
            info = new MemoryPoolInfo(startAddr, endAddr, debugInfo);
        } else {
            info = new MemoryPoolInfo(startAddr, endAddr);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_NODE_INFO)) {
            nodeInfo = (byte[]) result.get(TestResultParser.TEST_TOKEN_MEMMGR_NODE_INFO);
            if (nodeInfo != null) {
                int nodeSize = nodeInfo.length / totalNodeCount;
                for (int i = 0; i < totalNodeCount; i++) {
                    int offset = i * nodeSize;
                    // addr
                    long addr = TestResultParser.decodeInt64(nodeInfo, offset);
                    offset += 8;
                    // size
                    int size = TestResultParser.decodeInt32(nodeInfo, offset);
                    offset += 4;
                    // usedSize
                    int usedSize = TestResultParser.decodeInt32(nodeInfo, offset);
                    offset += 4;
                    // flag
                    MemoryNodeStatus flag = TestResultParser.decodeInt32(nodeInfo, offset) > 0 ? MemoryNodeStatus.MEMORY_STATUS_USED
                            : MemoryNodeStatus.MEMORY_STATUS_FREE;
                    offset += 4;
                    // timestamp
                    long timestamp = TestResultParser.decodeInt64(nodeInfo, offset);
                    offset += 8;
                    int callStackLen = TestResultParser.decodeInt32(nodeInfo, offset);
                    offset += 4;
                    // using 128 byte to transfer callstack
                    String callStack = null;
                    try {
                        callStack = new String(nodeInfo, offset, callStackLen, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    offset += 128;

                    MemoryNode node = null;

                    if (mMemoryConfig.isDebug() && flag.equals(MemoryNodeStatus.MEMORY_STATUS_USED)) {
                        node = new MemoryNode(addr, flag, size, timestamp, usedSize, callStack);
                    } else {
                        node = new MemoryNode(addr, flag, size, timestamp, usedSize);
                    }
                    info.addMemoryNode(node);
                }
            }
        }

        return info;
    }

    public static void createAndAppendFile(String fileName, String content) {

        File file = new File(fileName);

        if (null != file.getParentFile()) {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    Log.e(TAG, "create dirs fail path:" + fileName);
                }
            }
        }

        RandomAccessFile randomFile = null;
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                     Log.e(TAG, "create file fail path:" + fileName);
                }
            }
            // 打开一个随机访问文件流，按读写方式
            randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
            randomFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomFile != null) {
                    randomFile.close();
                }
            } catch (Exception e) {

            }
        }
    }

    private void dumpMemoryToFile(HashMap<Integer, Object> result) {
        Log.d(TAG, "dumpMemoryToFile");

        byte[] memoryBuffer = null;
        int offset = 0;
        String time = null;
        boolean finished = false;
        StringBuilder content = new StringBuilder();

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_POOL)) {
            memoryBuffer = (byte[]) result.get(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_POOL);
            if(memoryBuffer != null) {
                for (int i = 0; i < memoryBuffer.length / 16; i++) {
                    content.append(String.format("%08x", offset + i * 16));
                    for (int j = 0; j < 8; j++) {
                        content.append(String.format(" %02x%02x", memoryBuffer[i * 16 + j * 2],
                        memoryBuffer[i * 16 + j * 2 + 1]));
                    }
                    content.append("\n");
                }
            }
            else {
                Log.e(TAG, "memoryBuffer is null!");
                return;
            }
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_OFFSET)) {
            offset = (Integer) result.get(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_OFFSET);
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_TIME)) {
            try {
                time = new String((byte[])result.get(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_TIME), "utf-8");
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }

        if (result.containsKey(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_FINISHED)) {
            finished = ((Integer) result.get(TestResultParser.TEST_TOKEN_MEMMGR_DUMP_FINISHED)) > 0 ? true
                    : false;
        }

        Log.d(TAG, "dumpMemoryToFile offset: " + offset);
        Log.d(TAG, "dumpMemoryToFile time: " + time);
        Log.d(TAG, "dumpMemoryToFile finished: " + finished);

        String rootFilePath = mContext.getFilesDir() + "/memory/";
        StringBuilder tmpPath = new StringBuilder();
        tmpPath.append(rootFilePath);
        tmpPath.append("memory");
        if (time != null) {
            tmpPath.append("_" + time);
        }
        tmpPath.append(".dat");

        String filePath = tmpPath.toString();
        createAndAppendFile(filePath, content.toString());

        if (mMemoryManagerDataCallBack != null) {
            if (finished) {
                mMemoryManagerDataCallBack.onMemoryDumpFinished(filePath);
            }
        }
    }

    private void sendDumpFinishedMessage(final String fileName) {
        if (mUIHandler != null) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMemoryManagerDataCallBack != null) {
                        mMemoryManagerDataCallBack.onMemoryDumpFinished(fileName);
                    }
                }
            });
        }
    }

    private GoodixFingerprintManager.TestCmdCallback mTestCmdCallback = new GoodixFingerprintManager.TestCmdCallback() {
        @Override
        public void onTestCmd(int cmdId, final HashMap<Integer, Object> result) {
            if (Constants.CMD_TEST_MEMMGR_GET_INFO == cmdId) {
                if (mMemoryManagerDataCallBack != null) {
                    final MemoryPoolInfo info = parsePollInfo(result);
                    mMemoryManagerDataCallBack.onMemoryInfoFetched(info);
                }
            } else if (Constants.CMD_TEST_MEMMGR_GET_CONFIG == cmdId) {
                final MemoryManagerConfig memoryManagerConfig = parseConfig(result);

                if (mMemoryManagerDataCallBack != null) {
                    mMemoryManagerDataCallBack.onMemoryConfigFetched(memoryManagerConfig);
                }
            } else if (Constants.CMD_TEST_MEMMGR_DUMP_POOL == cmdId) {
                if (mTaskHandler != null) {
                    mTaskHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dumpMemoryToFile(result);
                        }
                    });
                }
            }

        }
    };

    public static class MemoryManagerUnInitializationException extends Exception {
        @Override
        public String toString() {
            return "MemManager not Initial!";
        }
    }

    public static class MemoryManagerDisableException extends Exception {
        @Override
        public String toString() {
            return "Memory Manager not enable!";
        }
    }

}
