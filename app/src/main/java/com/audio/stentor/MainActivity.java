package com.audio.stentor;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener, TaskCallback {

    private Button mDecodeButton;
    private Button mMixingButton;
    private Button mMicrophoneButton;
    private Button mDenoiseButton;
    private Button mPackagingButton;

    private Button mPreviewButton;

    private TextView mCurrentPositionTextView;
    private TextView mTotalTextView;

    private ProgressBar mPreviewProgressBar;
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
        mMixingButton = findViewById(R.id.btn_mixing);
        mMicrophoneButton = findViewById(R.id.btn_microphone);
        mDenoiseButton = findViewById(R.id.btn_denoise);
        mPackagingButton = findViewById(R.id.btn_packaging);
        mPreviewButton = findViewById(R.id.btn_preview);

        mDecodeButton.setOnClickListener(this);
        mMixingButton.setOnClickListener(this);
        mMicrophoneButton.setOnClickListener(this);
        mDenoiseButton.setOnClickListener(this);
        mPackagingButton.setOnClickListener(this);
        mPreviewButton.setOnClickListener(this);

        mCurrentPositionTextView = findViewById(R.id.tv_current_position);
        mTotalTextView = findViewById(R.id.tv_total);

        mPreviewProgressBar = findViewById(R.id.progress_preview);
        mAudioTaskProgressBar = findViewById(R.id.progress_audio_task);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_decode:
                (new DecodeAudioTask(this, this)).start();
                break;

            case R.id.btn_mixing:
                break;

            case R.id.btn_microphone:
                break;

            case R.id.btn_denoise:
                break;

            case R.id.btn_packaging:
                break;

            case R.id.btn_preview:
                if (TextUtils.equals(mPreviewButton.getText(), "pause")) {
                    mPreviewButton.setText("start");
                } else {
                    mPreviewButton.setText("pause");
                }
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
                mMixingButton.setEnabled(false);
                mMicrophoneButton.setEnabled(false);
                mDenoiseButton.setEnabled(false);
                mPackagingButton.setEnabled(false);
                mPreviewButton.setEnabled(false);
                mPreviewButton.setText("start");
                mAudioTaskProgressBar.setVisibility(View.VISIBLE);
                mTotalTextView.setText(TimeUtils.formatNumberToMinuteSecond(0.0));
                mCurrentPositionTextView.setText(TimeUtils.formatNumberToMinuteSecond(0.0));
            }
        });
    }

    @Override
    public void onTaskCompleted(AudioTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDecodeButton.setEnabled(true);
                mMixingButton.setEnabled(true);
                mMicrophoneButton.setEnabled(true);
                mDenoiseButton.setEnabled(true);
                mPackagingButton.setEnabled(true);
                mPreviewButton.setEnabled(true);
                mPreviewButton.setText("start");
                mAudioTaskProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onTaskError(AudioTask task, int error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDecodeButton.setEnabled(true);
                mMixingButton.setEnabled(true);
                mMicrophoneButton.setEnabled(true);
                mDenoiseButton.setEnabled(true);
                mPackagingButton.setEnabled(true);
                mPreviewButton.setEnabled(true);
                mPreviewButton.setText("start");
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
