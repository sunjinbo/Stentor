package com.audio.stentor;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * MixingAudioTask.
 */
public class MixingAudioTask extends AudioTask {

    public MixingAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void run() {
        notifyTaskStarted();

        try {
            final File sample = new File(mContext.getExternalCacheDir(), "sample.pcm");
            final File BGM = new File(mContext.getExternalCacheDir(), "BGM.pcm");
            if (sample.exists() && BGM.exists()) {
                final int minBufferSize = AudioTrack.getMinBufferSize(48000,
                        AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

                FileInputStream sampleInputStream = new FileInputStream(sample);
                FileInputStream bgmInputStream = new FileInputStream(BGM);

                FileOutputStream fos = null;
                BufferedOutputStream bos = null;

                File tmp = new File(mContext.getExternalCacheDir(), "sample.tmp");
                if (tmp.exists()) {
                    tmp.delete();
                }

                fos = new FileOutputStream(tmp.getAbsoluteFile());
                bos = new BufferedOutputStream(fos, minBufferSize);

                int sample_len, bgm_len;

                byte[] sample_arr = new byte[minBufferSize];
                byte[] bgm_arr = new byte[minBufferSize];
                while (((sample_len = sampleInputStream.read(sample_arr))!=-1) && (bgm_len = bgmInputStream.read(bgm_arr))!=-1) {
                    byte[] mix = mixAudio(sample_arr, bgm_arr);
                    bos.write(mix, 0, mix.length);
                }

                try {
                    sampleInputStream.close();
                    bgmInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
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
            } else {
                notifyTaskError(Error.ERR_FILE_NOT_FOUND);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            notifyTaskError(-1);
            return;
        }

        File sample = new File(mContext.getExternalCacheDir(), "sample.pcm");
        if (sample.exists()) {
            sample.delete();
        }

        File tmp = new File(mContext.getExternalCacheDir(), "sample.tmp");
        if (tmp.exists()) {
            tmp.renameTo(sample);
        }

        notifyTaskCompleted();
    }

    private byte[] mixAudio(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return null;
        }

        byte[] output = new byte[first.length];
        for (int i = 0; i < output.length; i++) {
            float sample1 = first[i] / 128f;
            float sample2 = second[i] / 128f;

            float mixed = (sample1 + sample2 * 0.5f) * 0.6f;

            if (mixed > 1.0f) mixed = 1.0f;
            if (mixed < -1.0f) mixed = -1.0f;

            output[i] = (byte) (mixed * 128f);
        }
        return output;
    }
}
