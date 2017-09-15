package com.diagrams.core.messagemgr;

public enum MessageID {
    OBSERVER_ID_RESERVE {
        public Class<? extends IObserverBase> getObserverClass() {
            return null;
        }
    },
    SIMPLE {
        @Override
        Class<? extends IObserverBase> getObserverClass() {
            return SimpleObserver.class;
        }
    };

    abstract Class<? extends IObserverBase> getObserverClass();

    public static class SimpleObserver implements IObserverBase {

    }
}