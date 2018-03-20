package com.audio.stentor;

import android.os.SystemClock;

import java.util.Random;

/**
 * TestAudioTask.
 */
public class TestAudioTask extends AudioTask implements Runnable {

    private final static int MAX_TOTAL = 100;

    private int mCurrentProgress = 0;
    private Random mRandom = new Random();

    public TestAudioTask(TaskCallback callback) {
        super(callback);
    }

    public void start() {
        mCurrentProgress = 0;
        new Thread(this).start();
    }

    @Override
    public void run() {

        notifyTaskStarted();

        while (mCurrentProgress <= 100) {
            notifyTaskProgress(MAX_TOTAL, mCurrentProgress);

            SystemClock.sleep(111);

            int rand = mRandom.nextInt(100);
            if (rand >= 99) {
                break;
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
