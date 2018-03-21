package com.audio.stentor;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.SystemClock;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * MicphoneAudioTask class.
 */
public class MicphoneAudioTask extends AudioTask {

    private AudioRecord mAudioRecord;

    public MicphoneAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void run() {
        notifyTaskStarted();

        final String dataSource = (new File(mContext.getExternalCacheDir(), "sample.mp4").getAbsolutePath());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dataSource);

        // 取得视频的长度(单位为毫秒)
        final String videoTimeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        float totalTime = Float.valueOf(videoTimeString);

        final int bufferSizeInBytes = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT); // need to be larger than size of a frame

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes);

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            File pcm = new File(mContext.getExternalCacheDir(), "sample.pcm");
            if (pcm.exists()) {
                pcm.delete();
            }

            fos = new FileOutputStream(pcm.getAbsoluteFile());
            bos = new BufferedOutputStream(fos,bufferSizeInBytes);

            mAudioRecord.startRecording();

            byte[] buffer = new byte[bufferSizeInBytes];

            int bufferReadResult = 0;

            long startTime = SystemClock.elapsedRealtime();

            while (true) {
                int internal = (int)((SystemClock.elapsedRealtime() - startTime));
                if (internal > totalTime) {
                    break;
                }

                notifyTaskProgress((int)totalTime, internal);

                bufferReadResult = mAudioRecord.read(buffer, 0,
                        bufferSizeInBytes);

                if (bufferReadResult > 0){
                    bos.write(buffer, 0, bufferReadResult);
                }
            }

            try {
                if (bos != null) {
                    bos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        bos = null;
                    }
                }
            }

            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            notifyTaskError(-1);
        }

        notifyTaskCompleted();
    }
}
