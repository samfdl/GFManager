/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.memmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MemoryPoolInfo {

    private static final String TAG = "MemoryPoolInfo";

    private long startAddr = 0;
    private long endAddr = 0;

    private MemoryPoolDebugInfo mDebugInfo;

    private List<MemoryNode> usedMemoryList = new ArrayList<MemoryNode>();
    private List<MemoryNode> freedMemoryList = new ArrayList<MemoryNode>();

    public MemoryPoolInfo(long startAddr, long endAddr) {
        this(startAddr, endAddr, null);
    }

    public MemoryPoolInfo(long startAddr, long endAddr, MemoryPoolDebugInfo debugInfo) {
        this.startAddr = startAddr;
        this.endAddr = endAddr;
        mDebugInfo = debugInfo;
    }

    public MemoryPoolDebugInfo getMemoryPoolDebugInfo() {
        return mDebugInfo;
    }

    public long getStartAddr() {
        return startAddr;
    }

    public long getEndAddr() {
        return endAddr;
    }

    public int getSize() {
        return (int) (endAddr - startAddr);
    }

    public int getUsedNodeCount() {
        return usedMemoryList.size();
    }

    public int getUsedNodeSize() {
        int size = 0;
        for (MemoryNode node : usedMemoryList) {
            size += node.getSize();
        }
        return size;
    }

    public int getFreedNodeCount() {
        return freedMemoryList.size();
    }

    public int getFreeNodeSize() {
        int size = 0;
        for (MemoryNode node : freedMemoryList) {
            size += node.getSize();
        }
        return size;
    }

    public int getInternalFragmentSize() {
        int size = 0;
        for (MemoryNode node : usedMemoryList) {
            size += node.getSize() - node.getUsedSize();
        }
        return size;
    }

    public int getInternalFragmentCount() {
        int count = 0;
        for (MemoryNode node : usedMemoryList) {
            if (node.getUsedSize() < node.getSize()) {
                count++;
            }
        }
        return count;
    }

    public void addMemoryNode(MemoryNode node) {
        if (node.getFlag() == MemoryNode.MemoryNodeStatus.MEMORY_STATUS_USED) {
            this.usedMemoryList.add(node);
        } else if (node.getFlag() == MemoryNode.MemoryNodeStatus.MEMORY_STATUS_FREE) {
            this.freedMemoryList.add(node);
        }
    }

    public byte[] getMemoryUsageMap(int unitSize) {
        byte[] status = null;

        int cnt = getSize() % unitSize == 0 ? getSize() / unitSize : (getSize() / unitSize + 1);
        if (cnt > 0) {
            status = new byte[cnt];

            for (MemoryNode node : usedMemoryList) {
                int nodeStartIndex = (int) (node.addr - startAddr) / unitSize;
                int nodeEndIndex = (int) ((node.addr + node.size - startAddr) % unitSize == 0
                        ? (node.addr + node.size - startAddr) / unitSize - 1
                        : (node.addr + node.size - startAddr) / unitSize);

                for (int i = nodeStartIndex; i <= nodeEndIndex; i++) {
                    status[i] = 1;
                }
            }
        }
        return status;
    }

    public List<MemoryNode> getFreedMemoryNodes(int minSize, int maxSize) {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setMinSize(minSize).setMaxSize(maxSize)
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_FREE)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_SIZE).build());
    }

    public List<MemoryNode> getUsedMemoryNodes(int minSize, int maxSize) {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setMinSize(minSize).setMaxSize(maxSize)
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_USED)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_SIZE).build());
    }

    public List<MemoryNode> getMemoryList() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder().build());
    }

    public List<MemoryNode> getFreedMemoryNodes() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_FREE).build());
    }

    public List<MemoryNode> getUsedMemoryList() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_USED).build());
    }

    public List<MemoryNode> getUsedNodeListByAddr() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_USED)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_ADDR).build());
    }

    public List<MemoryNode> getFreedNodeListByAddr() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_FREE)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_ADDR).build());
    }

    public List<MemoryNode> getUsedNodeListBySize() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_USED)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_SIZE).build());
    }

    public List<MemoryNode> getFreedNodeListBySize() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_FREE)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_SIZE).build());
    }

    public List<MemoryNode> getUsedNodeListByTime() {
        return getMemoryNodeList(new MemoryNodeInfoSearchConfig.Builder()
                .setNodeType(MemoryNodeInfoSearchConfig.MEMORY_NODE_USED)
                .setSortType(MemoryNodeInfoSearchConfig.SORT_TYPE_BY_TIME).build());
    }

    public List<MemoryNode> getMemoryNodeList(MemoryNodeInfoSearchConfig searchConfig) {
        List<MemoryNode> list = new ArrayList<MemoryNode>();

        if (null == searchConfig) {
            return list;
        }

        if (searchConfig.getStatus() != MemoryNodeInfoSearchConfig.MEMORY_NODE_USED) {
            // add free node
            for (MemoryNode node : freedMemoryList) {
                if ((node.getSize() >= searchConfig.getMinSize()) && (node.getSize() <= searchConfig.getMaxSize())) {
                    list.add(node);
                }
            }
        }

        if (searchConfig.getStatus() != MemoryNodeInfoSearchConfig.MEMORY_NODE_FREE) {
            // add used node
            for (MemoryNode node : usedMemoryList) {

                if ((node.getSize() >= searchConfig.getMinSize()) && (node.getSize() <= searchConfig.getMaxSize())) {
                    if (searchConfig.getCallStacKType() == MemoryNodeInfoSearchConfig.CALLSTACK_BY_TA) {
                        // add node whose callstack by TA
                        if (isCalledDirectlyByTA(node.getCallStack())) {
                            list.add(node);
                        }
                    } else if (searchConfig.getCallStacKType() == MemoryNodeInfoSearchConfig.CALLSTACK_BY_ALGORITHM) {
                        // add node whose callstack by Algorithm
                        if (isCalledDirectlyByAlgorithmLib(node.getCallStack())) {
                            list.add(node);
                        }
                    } else if (searchConfig.getCallStacKType() == MemoryNodeInfoSearchConfig.CALLSTACK_BY_ALL) {
                        list.add(node);
                    }
                }
            }
        }

        if (list.size() > 0) {
            if (searchConfig.getSortType() == MemoryNodeInfoSearchConfig.SORT_TYPE_BY_TIME) {
                Collections.sort(list, mTimeComparator);
            } else if (searchConfig.getSortType() == MemoryNodeInfoSearchConfig.SORT_TYPE_BY_SIZE) {
                Collections.sort(list, mSizeComparator);
            } else if (searchConfig.getSortType() == MemoryNodeInfoSearchConfig.SORT_TYPE_BY_CALLSTACK) {
                Collections.sort(list, mCallStackComparator);
            } else {
                Collections.sort(list, mStartAddrComparator);
            }
        }

        return list;
    }

    private static Comparator<MemoryNode> mSizeComparator = new Comparator<MemoryNode>() {
        @Override
        public int compare(MemoryNode lhs, MemoryNode rhs) {
            if (lhs == rhs) return 0;

            if (lhs.getUsedSize() > 0 && rhs.getUsedSize() > 0) {
                if (lhs.getUsedSize() == rhs.getUsedSize()) return 0;

                return lhs.getUsedSize() < rhs.getUsedSize() ? -1 : 1;
            } else {
                if (lhs.getSize() == rhs.getSize()) return 0;
                return lhs.getSize() < rhs.getSize() ? -1 : 1;
            }
        }
    };

    private static Comparator<MemoryNode> mTimeComparator = new Comparator<MemoryNode>() {
        @Override
        public int compare(MemoryNode lhs, MemoryNode rhs) {
            if (lhs == rhs) return 0;
            if (lhs.getTimestamp() > 0 && rhs.getTimestamp() > 0) {
                if (lhs.getTimestamp() == rhs.getTimestamp())
                    return 0;
                return lhs.getTimestamp() < rhs.getTimestamp() ? -1 : 1;
            } else {
                if (lhs.getSize() == rhs.getSize())
                    return 0;
                return lhs.getSize() < rhs.getSize() ? -1 : 1;
            }
        }
    };

    private static Comparator<MemoryNode> mStartAddrComparator = new Comparator<MemoryNode>() {
        @Override
        public int compare(MemoryNode lhs, MemoryNode rhs) {
            if (lhs == rhs) return 0;
            if (lhs.getAddr() == rhs.getAddr()) return 0;
            return lhs.getAddr() < rhs.getAddr() ? -1 : 1;
        }
    };

    public static boolean isCalledDirectlyByTA(String callStack) {
        return callStack.startsWith("[Locals");
    }

    public static boolean isCalledDirectlyByAlgorithmLib(String callStack) {
        return callStack.startsWith("[./packages");
    }

    private static Comparator<MemoryNode> mCallStackComparator = new Comparator<MemoryNode>() {
        @Override
        public int compare(MemoryNode lhs, MemoryNode rhs) {
            if (lhs == rhs) return 0;

            if (lhs.getCallStack() == rhs.getCallStack()) return 0;
            if (lhs.getCallStack() == null) return 1;
            if (rhs.getCallStack() == null) return -1;

            if (lhs.getCallStack().equals(rhs.getCallStack())) return 0;
            if (lhs.getCallStack().equals("")) return 1;
            if (rhs.getCallStack().equals("")) return -1;

            if (isCalledDirectlyByTA(lhs.getCallStack()) && isCalledDirectlyByAlgorithmLib(rhs.getCallStack()))
                return -1;
            if (isCalledDirectlyByAlgorithmLib(lhs.getCallStack()) && isCalledDirectlyByTA(rhs.getCallStack()))
                return 1;

            return lhs.getCallStack().compareTo(rhs.getCallStack());
        }
    };

    public static class MemoryPoolDebugInfo {
        private int usedNodeCount = 0;
        private int usedMemorySize = 0;
        private int maxUsedNodeCount = 0;
        private int maxUsedMemorySize = 0;

        public MemoryPoolDebugInfo(int usedCount, int usedSize, int maxUsedCount, int maxUsedSize) {
            this.usedNodeCount = usedCount;
            this.usedMemorySize = usedSize;
            this.maxUsedNodeCount = maxUsedCount;
            this.maxUsedMemorySize = maxUsedSize;
        }

        public int getMaxUsedMemorySize() {
            return maxUsedMemorySize;
        }

        public int getMaxUsedNodeCount() {
            return maxUsedNodeCount;
        }

        public int getUsedMemorySize() {
            return usedMemorySize;
        }

        public int getUsedNodeCount() {
            return usedNodeCount;
        }

    }

    public static class MemoryNode {

        public enum MemoryNodeStatus {
            MEMORY_STATUS_FREE,
            MEMORY_STATUS_USED,
        }

        private long addr;
        private int usedSize;
        private int size;
        private MemoryNodeStatus flag;
        private long timestamp;
        private String callStack;

        public MemoryNode(long addr, MemoryNodeStatus flag, int size, long timestamp, int usedSize) {
            this(addr, flag, size, timestamp, usedSize, null);
        }

        public MemoryNode(long addr, MemoryNodeStatus flag, int size, long timestamp, int usedSize,
                String callStack) {
            this.addr = addr;
            this.flag = flag;
            this.size = size;
            this.timestamp = timestamp;
            this.usedSize = usedSize;
            this.callStack = callStack;
        }

        public long getAddr() {
            return addr;
        }

        public MemoryNodeStatus getFlag() {
            return flag;
        }

        public int getSize() {
            return size;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getUsedSize() {
            return usedSize;
        }

        public String getCallStack() {
            return callStack;
        }
    }
}
