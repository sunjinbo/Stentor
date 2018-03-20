package com.audio.stentor;

import android.content.Context;
import android.os.SystemClock;

import java.util.Random;

/**
 * TestAudioTask.
 */
public class TestAudioTask extends AudioTask implements Runnable {

    private final static int MAX_TOTAL = 100;

    private enum  State {
        Unit, Running, Suspend, Completed
    }

    private State mState = State.Unit;

    private int mCurrentProgress = 0;
    private Random mRandom = new Random();

    public TestAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void start() {
        if (mState == State.Unit) {
            mCurrentProgress = 0;
            new Thread(this).start();
        }
    }

    @Override
    public void pause() {
        if (mState == State.Running) {
            mState = State.Suspend;
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public void run() {

        notifyTaskStarted();

        while (mCurrentProgress <= 100) {
            notifyTaskProgress(MAX_TOTAL, mCurrentProgress);

            SystemClock.sleep(111);

            int rand = mRandom.nextInt(100);
            if (rand >= 99 || mState == State.Completed) {
                break;
            }

            while (mState == State.Suspend) {
                SystemClock.sleep(111);
            }

            mCurrentProgress += 1;
        }

        if (mCurrentProgress >= 100) {
            notifyTaskCompleted();
        } else {
            notifyTaskError(mCurrentProgress);
        }
    }
}
