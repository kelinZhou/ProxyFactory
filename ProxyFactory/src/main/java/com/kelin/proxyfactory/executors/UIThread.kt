package com.kelin.proxyfactory.executors;


import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public final class UIThread implements PostExecutionThread {
    public UIThread() {
    }

    @Override
    public Scheduler getScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
