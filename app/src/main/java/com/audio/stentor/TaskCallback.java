package com.audio.stentor;

/**
 * TaskCallback interface.
 */
public interface TaskCallback {
    void onTaskStarted(AudioTask task);
    void onTaskCompleted(AudioTask task);
    void onTaskError(AudioTask task, int error);
    void onTaskProgress(AudioTask task, int total, int current);
}
