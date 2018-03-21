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
import java.util.ArrayList;

/**
 * DecodeAudioTask class.
 */
public class DecodeAudioTask extends AudioTask implements Runnable {

    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaDecode;

    private ByteBuffer[] mDecodeInputBuffers;
    private ByteBuffer[] mDecodeOutputBuffers;
    private MediaCodec.BufferInfo mDecodeBufferInfo;

    private ArrayList<byte[]> mChunkPCMDataContainer = new ArrayList<>();
    private boolean mIsDecodeCompleted = false;
    private long mDecodeSize = 0;
    private float mVideoTotalTime;

    public DecodeAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
        try {
            AssetsUtils.copyAssetsToSDCard(context, "video.mp4",
                    (new File(context.getExternalCacheDir(), "video.mp4")).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void pause() {

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

        try {
            final String dataSource = (new File(mContext.getExternalCacheDir(), "video.mp4").getAbsolutePath());
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
            mDecodeOutputBuffers = mMediaDecode.getOutputBuffers();
            mDecodeBufferInfo = new MediaCodec.BufferInfo();

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {
                fos = new FileOutputStream(new File(mContext.getExternalCacheDir(), "video.pcm"));
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
                        mDecodeSize += sampleSize;
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
