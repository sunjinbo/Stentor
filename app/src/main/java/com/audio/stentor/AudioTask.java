package com.audio.stentor;

import android.content.Context;

/**
 * AudioTask class.
 */
public abstract class AudioTask {

    protected Context mContext;
    protected TaskCallback mCallback;

    public abstract void start();
    public abstract void pause();
    public abstract void stop();
    public abstract void release();

    protected AudioTask(Context context, TaskCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    protected void notifyTaskStarted() {
        if (mCallback != null) {
            mCallback.onTaskStarted(this);
        }
    }

    protected void notifyTaskCompleted() {
        if (mCallback != null) {
            mCallback.onTaskCompleted(this);
        }
    }

    protected void notifyTaskError(int error) {
        if (mCallback != null) {
            mCallback.onTaskError(this, error);
        }
    }

    protected void notifyTaskProgress(int total, int current) {
        if (mCallback != null) {
            mCallback.onTaskProgress(this, total, current);
        }
    }
}
