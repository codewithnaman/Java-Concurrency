package com.cwn.racecondition.example.model;

public class LongWrapperWithoutSynchronization {
    private Long value = new Long(0L);

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public void incrementValue() {
        value = value + 1;
    }
}
