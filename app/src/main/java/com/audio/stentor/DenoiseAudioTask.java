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
 * DenoiseAudioTask class.
 */
public class DenoiseAudioTask extends AudioTask {

    public DenoiseAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void run() {
        notifyTaskStarted();

        try {
            final File sample = new File(mContext.getExternalCacheDir(), "sample.pcm");
            if (sample.exists()) {
                final int minBufferSize = AudioTrack.getMinBufferSize(48000,
                        AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

                FileInputStream sampleInputStream = new FileInputStream(sample);

                FileOutputStream fos = null;
                BufferedOutputStream bos = null;

                File tmp = new File(mContext.getExternalCacheDir(), "sample.tmp");
                if (tmp.exists()) {
                    tmp.delete();
                }

                fos = new FileOutputStream(tmp.getAbsoluteFile());
                bos = new BufferedOutputStream(fos, minBufferSize);

                int sample_len, bgm_len;
                int progress = 0;

                byte[] sample_arr = new byte[minBufferSize];

                while (((sample_len = sampleInputStream.read(sample_arr))!=-1)) {
                    short[] short_arr = toShortArray(sample_arr);
                    short[] denoise_arr = denoise(short_arr, 0, short_arr.length);
                    byte[] byte_arr = toByteArray(denoise_arr);

                    bos.write(byte_arr, 0, byte_arr.length);
                    progress += sample_len;
                    notifyTaskProgress((int)sample.length(), progress);
                }

                try {
                    sampleInputStream.close();
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

    private static short[] toShortArray(byte[] src) {
        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

    private static byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] >> 8);
            dest[i * 2 + 1] = (byte) (src[i] >> 0);
        }

        return dest;
    }

    private static short[] denoise(short[] data, int offset, int length) {
        int i,j;
        for (i = 0; i < length; i++) {
            j = data[i+offset];
            data[i+offset] = (short)(j>>2);
        }
        return data;
    }
}
