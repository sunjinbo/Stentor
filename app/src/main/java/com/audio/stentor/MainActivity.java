package com.audio.stentor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener, TaskCallback {

    private Button mDecodeButton;
    private Button mPlayoutButton;
    private Button mMixingButton;
    private Button mMicrophoneButton;
    private Button mDenoiseButton;
    private Button mToneButton;
    private Button mEncodeButton;
    private Button mMuxerButton;

    private ProgressBar mAudioTaskProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initData();

        initView();
    }

    private void initData() {
        try {
            AssetsUtils.copyAssetsToSDCard(
                    this,
                    "BGM.wav",
                    (new File(this.getExternalCacheDir(), "BGM.wav")).getAbsolutePath());

            AssetsUtils.copyAssetsToSDCard(
                    this,
                    "BGM.mp3",
                    (new File(this.getExternalCacheDir(), "BGM.mp3")).getAbsolutePath());

            AssetsUtils.copyAssetsToSDCard(
                    this,
                    "BGM.pcm",
                    (new File(this.getExternalCacheDir(), "BGM.pcm")).getAbsolutePath());

            AssetsUtils.copyAssetsToSDCard(
                    this,
                    "sample.mp4",
                    (new File(this.getExternalCacheDir(), "sample.mp4")).getAbsolutePath());

            AssetsUtils.copyAssetsToSDCard(
                    this,
                    "sample.pcm",
                    (new File(this.getExternalCacheDir(), "sample.pcm")).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mDecodeButton = findViewById(R.id.btn_decode);
        mPlayoutButton = findViewById(R.id.btn_playout);
        mMixingButton = findViewById(R.id.btn_mixing);
        mMicrophoneButton = findViewById(R.id.btn_microphone);
        mDenoiseButton = findViewById(R.id.btn_denoise);
        mToneButton = findViewById(R.id.btn_tone);
        mEncodeButton = findViewById(R.id.btn_encode);
        mMuxerButton = findViewById(R.id.btn_muxer);

        mDecodeButton.setOnClickListener(this);
        mPlayoutButton.setOnClickListener(this);
        mMixingButton.setOnClickListener(this);
        mMicrophoneButton.setOnClickListener(this);
        mDenoiseButton.setOnClickListener(this);
        mToneButton.setOnClickListener(this);
        mEncodeButton.setOnClickListener(this);
        mMuxerButton.setOnClickListener(this);

        mAudioTaskProgressBar = findViewById(R.id.progress_audio_task);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_playout:
                (new PlayoutAudioTask(this, this)).start();
                break;

            case R.id.btn_decode:
                (new DecodeAudioTask(this, this)).start();
                break;

                case R.id.btn_microphone:
                (new MicphoneAudioTask(this, this)).start();
                break;

            case R.id.btn_mixing:
                (new MixingAudioTask(this, this)).start();
                break;

            case R.id.btn_denoise:
                (new DenoiseAudioTask(this, this)).start();
                break;

            case R.id.btn_tone:
                break;

            case R.id.btn_encode:
                (new EncodeAudioTask(this, this)).start();
                break;

            case R.id.btn_muxer:
                (new MuxerAudioTask(this, this)).start();
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
                mPlayoutButton.setEnabled(false);
                mMicrophoneButton.setEnabled(false);
                mMixingButton.setEnabled(false);
                mDenoiseButton.setEnabled(false);
                mToneButton.setEnabled(false);
                mEncodeButton.setEnabled(false);
                mMuxerButton.setEnabled(false);

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
                mPlayoutButton.setEnabled(true);
                mMicrophoneButton.setEnabled(true);
                mMixingButton.setEnabled(true);
                mDenoiseButton.setEnabled(true);
                mToneButton.setEnabled(true);
                mEncodeButton.setEnabled(true);
                mMuxerButton.setEnabled(true);

                mAudioTaskProgressBar.setVisibility(View.INVISIBLE);

                if (task instanceof DecodeAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to decode a PCM file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof PlayoutAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to playing a PCM file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof MicphoneAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to record an audio file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof MixingAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to mixing an audio file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof DenoiseAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to denoise an audio file!", Toast.LENGTH_SHORT).show();
                } else if (task instanceof MuxerAudioTask) {
                    Toast.makeText(MainActivity.this, "Successfully to muxing a media file!", Toast.LENGTH_SHORT).show();
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
                mPlayoutButton.setEnabled(true);
                mMicrophoneButton.setEnabled(true);
                mMixingButton.setEnabled(true);
                mDenoiseButton.setEnabled(true);
                mToneButton.setEnabled(true);
                mEncodeButton.setEnabled(true);
                mMuxerButton.setEnabled(true);

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
