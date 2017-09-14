package com.diagrams.lib.lazy;

import android.support.annotation.NonNull;
import android.util.SparseArray;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 延迟更新工具，调用{@link #updatePendingObj(Object, Object)}来监听要延迟执行的数据，
 * 调用{@link #disconnect()}来开启延迟数据传递，调用{@link #connect()}关闭延迟数据传递，
 * 并且会把disconnect()期间获取的所有延迟数据传递出去，( 通过构造函数传递的{@link
 * OnPendingListener#optPendingObj(Object, Object)} 传递)。<br/>
 * 注意，默认是connect()状态。
 * <p/>
 * Created by lizhaofei on 2017/9/8 14:37
 */
public class PendingObjHandler<T> {
    private Map<Object, T> pendingObjMap = new HashMap<>();
    private SparseArray<Object> pendingTagMap = new SparseArray<>();

    private Object nullTag = new Object();//tag为null的内部替代值

    private boolean disconnect = false;
    private boolean hasPendingObj = false;
    private OnPendingListener<T> onPendingListener;
    private OnConnectChangeListener onConnectChangeListener;
    private OnPendingFlushStrategy tagFlushListener = new OnPendingFlushStrategy() {
        @Override
        public int tagToInteger(Object tag) {
            if (null == tag) {
                return 0;
            } else {
                return tag.hashCode();
            }
        }
    };

    /** 延迟更新结果监听接口 */
    public interface OnPendingListener<T> {
        void optPendingObj(Object tag, T pendingObj);
    }

    /** 连接状态改变监听接口 */
    public interface OnConnectChangeListener {
        /** @param connect true链接，false断开链接 */
        void onConnectChange(boolean connect);
    }

    /**
     * {@link #updatePendingObj(Object, Object)}，判断第一个参数tag是否为重复tag的处理策略，
     * 如果为重复tag会直接缓存掉已经存在的缓存结果。
     */
    public interface OnPendingFlushStrategy {
        int tagToInteger(Object tag);
    }

    public PendingObjHandler(@NonNull OnPendingListener<T> listener) {
        this.onPendingListener = listener;
    }

    public void setPendingFlushSty(OnPendingFlushStrategy tagFlushListener) {
        if (null != tagFlushListener) {
            this.tagFlushListener = tagFlushListener;
        }
    }

    /** 设置 链接/断开链接 监听接口，会在{@link OnPendingListener#optPendingObj(Object, Object)} 之后执行 */
    public void setOnConnectChangeListener(OnConnectChangeListener onConnectChangeListener) {
        this.onConnectChangeListener = onConnectChangeListener;
    }

    //链接
    public void connect() {
        boolean connectChanged = false;
        if (disconnect) {
            connectChanged = true;
            disconnect = false;
        }
        if (hasPendingObj) {
            Iterator<Map.Entry<Object, T>> iterator = pendingObjMap.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<Object, T> entry = iterator.next();
                final Object tag = entry.getKey();
                final T pendingObj = entry.getValue();

                final int tagSty = tagFlushListener.tagToInteger(tag);
                pendingTagMap.remove(tagSty);
                iterator.remove();

                if (tag == nullTag) {
                    onPendingListener.optPendingObj(null, pendingObj);
                } else {
                    onPendingListener.optPendingObj(tag, pendingObj);
                }
            }
            hasPendingObj = false;
        }
        if (connectChanged && null != onConnectChangeListener) {
            onConnectChangeListener.onConnectChange(true);
        }
    }

    //断开连接
    public void disconnect() {
        if (!disconnect) {
            disconnect = true;
            if (null != onConnectChangeListener) {
                onConnectChangeListener.onConnectChange(false);
            }
        }
    }

    //接管结果传递
    public void updatePendingObj(Object tag, T pendingObj) {
        if (disconnect) {//此时暂存数据
            hasPendingObj = true;
            final Object realTag;
            if (null == tag) {  //对tag为null的情况做特殊处理
                realTag = nullTag;
            } else {
                realTag = tag;
            }

            final int tagSty = tagFlushListener.tagToInteger(realTag);
            final Object savedTag = pendingTagMap.get(tagSty);
            if (null != savedTag) {//证明已经存在此tag
                pendingObjMap.remove(savedTag);
            }
            pendingTagMap.put(tagSty, realTag);
            pendingObjMap.put(realTag, pendingObj);
        } else {//直接回调接口来让外部更新数据
            onPendingListener.optPendingObj(tag, pendingObj);
        }
    }

    //清除所有延迟数据
    public void clear() {
        pendingTagMap.clear();
        pendingObjMap.clear();
    }

    public static <T> SyncPendingObjHandler<T> sync(PendingObjHandler<T> pendingObjHandler) {
        return new SyncPendingObjHandler<>(pendingObjHandler);
    }

    public static class SyncPendingObjHandler<T> {
        private PendingObjHandler<T> real;

        public SyncPendingObjHandler(PendingObjHandler<T> org) {
            real = org;
        }

        public synchronized void connect() {
            real.connect();
        }

        public synchronized void disconnect() {
            real.disconnect();
        }

        public synchronized void updatePendingObj(Object tag, T pendingObj) {
            real.updatePendingObj(tag, pendingObj);
        }

        public synchronized void setPendingFlushSty(OnPendingFlushStrategy tagFlushListener) {
            real.setPendingFlushSty(tagFlushListener);
        }

        public synchronized void setOnConnectChangeListener(OnConnectChangeListener onConnectChangeListener) {
            real.setOnConnectChangeListener(onConnectChangeListener);
        }

        public synchronized void clear() {
            real.clear();
        }
    }//SyncPendingObjHandler end
}
