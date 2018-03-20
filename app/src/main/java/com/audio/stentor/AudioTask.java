package com.audio.stentor;

/**
 * AudioTask class.
 */
public abstract class AudioTask {

    protected TaskCallback mCallback;

    protected AudioTask(TaskCallback callback) {
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
