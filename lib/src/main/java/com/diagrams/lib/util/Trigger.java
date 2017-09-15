package com.diagrams.lib.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class Trigger {
    public interface Listener {
        void trigger();
    }

    public Trigger(final int targetCount, final Listener l) {
        if(null == l){
            throw new IllegalArgumentException("listener can not be null!");
        }
        this.targetCount = targetCount;
        listener = l;
    }

    public void touch() {
        int num = touchCount.addAndGet(1);
        if (num == targetCount) {
            immediately();
        }
    }

    public synchronized void immediately() {
        if (listener != null) {
            Listener local = listener;
            listener = null;
            local.trigger();
        }
    }

    private int targetCount;
    private AtomicInteger touchCount = new AtomicInteger();
    private Listener listener;
}
