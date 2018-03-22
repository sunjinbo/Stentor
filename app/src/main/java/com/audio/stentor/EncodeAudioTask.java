package com.audio.stentor;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.SystemClock;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * EncodeAudioTask class.
 */
public class EncodeAudioTask extends AudioTask {

    private ArrayList<byte[]> chunkPCMDataContainer = new ArrayList<>();
    private boolean codeOver = false;

    public EncodeAudioTask(Context context, TaskCallback callback) {
        super(context, callback);
    }

    @Override
    public void start() {
        super.start();
        new Thread(new PcmReader()).start();
    }

    @Override
    public void run() {
        notifyTaskStarted();

        try {
            FileOutputStream fos = new FileOutputStream(new File(mContext.getExternalCacheDir(), "sample.aac"));
            BufferedOutputStream bos = new BufferedOutputStream(fos,200*1024);

            MediaFormat encodeFormat = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    44100,
                    2);
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);//比特率
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024);

            MediaCodec mediaEncode = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaEncode.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mediaEncode.start();
            ByteBuffer[] encodeInputBuffers = mediaEncode.getInputBuffers();
            ByteBuffer[] encodeOutputBuffers = mediaEncode.getOutputBuffers();
            MediaCodec.BufferInfo encodeBufferInfo = new MediaCodec.BufferInfo();

            while (!codeOver || !chunkPCMDataContainer.isEmpty()) {
                int inputIndex;
                ByteBuffer inputBuffer;
                int outputIndex;
                ByteBuffer outputBuffer;
                byte[] chunkAudio;
                int outBitSize;
                int outPacketSize;
                byte[] chunkPCM;
                for (int i = 0; i < encodeInputBuffers.length-1; i++) {
                    chunkPCM = getPCMData();//获取解码器所在线程输出的数据 代码后边会贴上
                    if (chunkPCM == null) {
                        break;
                    }
                    inputIndex = mediaEncode.dequeueInputBuffer(-1);//同解码器
                    inputBuffer = encodeInputBuffers[inputIndex];//同解码器
                    inputBuffer.clear();//同解码器
                    inputBuffer.limit(chunkPCM.length);
                    inputBuffer.put(chunkPCM);//PCM数据填充给inputBuffer
                    mediaEncode.queueInputBuffer(inputIndex, 0, chunkPCM.length, 0, 0);//通知编码器 编码
                }

                outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);//同解码器
                while (outputIndex >= 0) {//同解码器

                    outBitSize=encodeBufferInfo.size;
                    outPacketSize=outBitSize+7;//7为ADTS头部的大小
                    outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
                    outputBuffer.position(encodeBufferInfo.offset);
                    outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
                    chunkAudio = new byte[outPacketSize];
                    addADTStoPacket(chunkAudio,outPacketSize);//添加ADTS 代码后面会贴上
                    outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中 偏移量offset=7 你懂得
                    outputBuffer.position(encodeBufferInfo.offset);

                    try {
                        bos.write(chunkAudio,0,chunkAudio.length);//BufferOutputStream 将文件保存到内存卡中 *.aac
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mediaEncode.releaseOutputBuffer(outputIndex,false);
                    outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);
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

            notifyTaskError(Error.ERR_IO_EXCEPTION);
        }

        notifyTaskCompleted();
    }

    private static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private void putPCMData(byte[] pcmChunk) {
        synchronized (EncodeAudioTask.class) {//记得加锁
            chunkPCMDataContainer.add(pcmChunk);
        }
    }

    /**
     * 在Container中{@link #chunkPCMDataContainer}取出PCM数据
     * @return PCM数据块
     */
    private byte[] getPCMData() {
        synchronized (EncodeAudioTask.class) {//记得加锁
            if (chunkPCMDataContainer.isEmpty()) {
                return null;
            }

            byte[] pcmChunk = chunkPCMDataContainer.get(0);//每次取出index 0 的数据
            chunkPCMDataContainer.remove(pcmChunk);//取出后将此数据remove掉 既能保证PCM数据块的取出顺序 又能及时释放内存
            return pcmChunk;
        }
    }

    private class PcmReader implements Runnable {

        @Override
        public void run() {
            final int minBufferSize = AudioTrack.getMinBufferSize(48000,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            try {
                final File sample = new File(mContext.getExternalCacheDir(), "sample.pcm");
                FileInputStream sampleInputStream = new FileInputStream(sample);

                while (true) {
                    byte[] bgm_arr = new byte[minBufferSize];
                    int sample_len = sampleInputStream.read(bgm_arr);
                    if (sample_len != -1) {
                        putPCMData(bgm_arr);
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            codeOver = true;
        }
    }
}
