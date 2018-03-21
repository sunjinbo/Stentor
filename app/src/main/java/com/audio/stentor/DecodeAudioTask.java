package com.audio.stentor;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * DecodeAudioTask class.
 * This class can extract audio track from video file(.MP4) and save the pcm data to sdcard
 */
public class DecodeAudioTask extends AudioTask {

    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaDecode;

    private ByteBuffer[] mDecodeInputBuffers;
    private MediaCodec.BufferInfo mDecodeBufferInfo;

    private boolean mIsDecodeCompleted = false;
    private float mVideoTotalTime;

    public DecodeAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void run() {

        notifyTaskStarted();

        try {
            final String dataSource = (new File(mContext.getExternalCacheDir(), "sample.mp4").getAbsolutePath());
            mMediaExtractor = new MediaExtractor();
            mMediaExtractor.setDataSource(dataSource);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(dataSource);

            // 取得视频的长度(单位为毫秒)
            final String videoTimeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            mVideoTotalTime = Float.valueOf(videoTimeString);

            for (int i = 0; i < mMediaExtractor.getTrackCount(); ++i) {
                MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                Log.d("decode", "track count = " + i + ", mime = " + mime);
                if (mime.startsWith("audio")) {
                    mMediaExtractor.selectTrack(i);
                    mMediaDecode = MediaCodec.createDecoderByType(mime);
                    mMediaDecode.configure(mediaFormat, null, null, 0);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mMediaExtractor != null && mMediaDecode != null) {
            mMediaDecode.start();
            mDecodeInputBuffers = mMediaDecode.getInputBuffers();
            mDecodeBufferInfo = new MediaCodec.BufferInfo();

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {
                fos = new FileOutputStream(new File(mContext.getExternalCacheDir(), "sample.pcm"));
                bos = new BufferedOutputStream(fos,200*1024);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!mIsDecodeCompleted) {
                for (int i = 0; i < mDecodeInputBuffers.length - 1; i++) {
                    int inputIndex = mMediaDecode.dequeueInputBuffer(-1);
                    if (inputIndex < 0) {
                        mIsDecodeCompleted = true;
                        return;
                    }

                    ByteBuffer inputBuffer = mDecodeInputBuffers[inputIndex];
                    inputBuffer.clear();

                    long time = mMediaExtractor.getSampleTime();
                    notifyTaskProgress((int)mVideoTotalTime, (int)(time / 1000));
                    int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        mIsDecodeCompleted = true;
                    } else {
                        mMediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);
                        mMediaExtractor.advance();
                    }
                }

                int outputIndex = mMediaDecode.dequeueOutputBuffer(mDecodeBufferInfo, 10000);

                ByteBuffer outputBuffer;
                byte[] chunkPCM;
                while (outputIndex >= 0) {
                    outputBuffer = mMediaDecode.getOutputBuffer(outputIndex);
                    chunkPCM = new byte[mDecodeBufferInfo.size];
                    outputBuffer.get(chunkPCM);
                    outputBuffer.clear();

                    try {
                        if (bos != null) {
                            bos.write(chunkPCM, 0, chunkPCM.length);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mMediaDecode.releaseOutputBuffer(outputIndex, false);

                    outputIndex = mMediaDecode.dequeueOutputBuffer(mDecodeBufferInfo, 10000);
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
        }

        notifyTaskCompleted();
    }
}
