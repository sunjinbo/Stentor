package com.audio.stentor;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener, TaskCallback {

    private Button mDecodeButton;
    private Button mPcmButton;
    private Button mMixingButton;
    private Button mMicrophoneButton;
    private Button mDenoiseButton;
    private Button mPackagingButton;

    private ProgressBar mAudioTaskProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_main);

        initData();

        initView();
    }

    private void initData() {

    }

    private void initView() {
        mDecodeButton = findViewById(R.id.btn_decode);
        mPcmButton = findViewById(R.id.btn_play_pcm);
        mMixingButton = findViewById(R.id.btn_mixing);
        mMicrophoneButton = findViewById(R.id.btn_microphone);
        mDenoiseButton = findViewById(R.id.btn_denoise);
        mPackagingButton = findViewById(R.id.btn_packaging);

        mDecodeButton.setOnClickListener(this);
        mPcmButton.setOnClickListener(this);
        mMixingButton.setOnClickListener(this);
        mMicrophoneButton.setOnClickListener(this);
        mDenoiseButton.setOnClickListener(this);
        mPackagingButton.setOnClickListener(this);

        mAudioTaskProgressBar = findViewById(R.id.progress_audio_task);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_decode:
                (new DecodeAudioTask(this, this)).start();
                break;

            case R.id.btn_play_pcm:
                (new PcmAudioTask(this, this)).start();
                break;

            case R.id.btn_mixing:
                break;

            case R.id.btn_microphone:
                (new MicphoneAudioTask(this, this)).start();
                break;

            case R.id.btn_denoise:
                break;

            case R.id.btn_packaging:
                break;

            default:
                break;
        }
    }

    @Override
    public void onTaskStarted(AudioTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDecodeButton.setEnabled(false);
                mPcmButton.setEnabled(false);
                mMixingButton.setEnabled(false);
                mMicrophoneButton.setEnabled(false);
                mDenoiseButton.setEnabled(false);
                mPackagingButton.setEnabled(false);

                mAudioTaskProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onTaskCompleted(final AudioTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDecodeButton.setEnabled(true);
                mPcmButton.setEnabled(true);
                mMixingButton.setEnabled(true);
                mMicrophoneButton.setEnabled(true);
                mDenoiseButton.setEnabled(true);
                mPackagingButton.setEnabled(true);

                mAudioTaskProgressBar.setVisibility(View.INVISIBLE);

                if (task instanceof DecodeAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to decode a PCM file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof PcmAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to playing a PCM file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof MicphoneAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to record an audio file!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onTaskError(AudioTask task, int error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDecodeButton.setEnabled(true);
                mPcmButton.setEnabled(true);
                mMixingButton.setEnabled(true);
                mMicrophoneButton.setEnabled(true);
                mDenoiseButton.setEnabled(true);
                mPackagingButton.setEnabled(true);

                mAudioTaskProgressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(MainActivity.this, "This task occurs error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTaskProgress(AudioTask task, final int total, final int current) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int progress = 0;
                if (total > 0) {
                    progress = (int)((double)current / (double)total * 100);
                }

                mAudioTaskProgressBar.setProgress(progress);
            }
        });
    }
}
