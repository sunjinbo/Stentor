package com.audio.stentor;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.File;
import java.io.FileInputStream;

/**
 * PcmAudioTask class.
 * This class can play a PCM file directly.
 */
public class PcmAudioTask extends AudioTask {

    private AudioTrack mAudioTrack;

    public PcmAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void run() {
        notifyTaskStarted();

        final int minBufferSize = AudioTrack.getMinBufferSize(48000,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, // 采样率
                AudioFormat.CHANNEL_IN_STEREO, // 声道
                AudioFormat.ENCODING_PCM_16BIT, // 采样精度
                minBufferSize,
                AudioTrack.MODE_STREAM);

        try {
            final File pcm = new File(mContext.getExternalCacheDir(), "video.pcm");
            if (pcm.exists()) {
                FileInputStream fileInputStream = new FileInputStream(pcm);
                mAudioTrack.play();
                int len;
                int progress = 0;
                byte[] arr = new byte[minBufferSize];
                while ((len = fileInputStream.read(arr))!=-1 ) {
                    mAudioTrack.write(arr, 0, len);
                    progress += len;
                    notifyTaskProgress((int)pcm.length(), progress);
                }

                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;

                fileInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            notifyTaskError(-1);
        }

        notifyTaskCompleted();
    }
}
