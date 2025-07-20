package com.alfred.zombification.access;

public interface ZombifiableEntity {
    boolean isZombified();
    void setZombified(boolean zombified);
    boolean isUnzombifying();
    void setUnzombifying(boolean unzombifying);
    int getConversionTimer();
    void setConversionTimer(int conversionTimer);
    void conversionTimerTick(int i);
}
