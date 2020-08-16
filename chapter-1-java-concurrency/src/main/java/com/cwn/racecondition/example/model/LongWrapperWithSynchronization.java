package com.cwn.racecondition.example.model;

public class LongWrapperWithSynchronization {
    private Long value = new Long(0L);
    private Object key = new Object();

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public void incrementValue() {
        synchronized (key) {
            value = value + 1;
        }
    }
}
