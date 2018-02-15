package org.noise_planet.yin;

import org.junit.Test;
import org.renjin.gcc.runtime.MixedPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.ShortPtr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example of using a compiled C function from Java
 */
public class YinACF_Test {

    public static short[] convertBytesToShort(byte[] buffer, int length, ByteOrder byteOrder) {
        ShortBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, length).order(byteOrder).asShortBuffer();
        short[] samplesShort = new short[byteBuffer.capacity()];
        byteBuffer.order();
        byteBuffer.get(samplesShort);
        return samplesShort;
    }

    public static short[] loadShortStream(InputStream inputStream, ByteOrder byteOrder) throws IOException {
        short[] fullArray = new short[0];
        byte[] buffer = new byte[4096];
        int read;
        // Read input signal up to buffer.length
        while ((read = inputStream.read(buffer)) != -1) {
            // Convert bytes into double values. Samples array size is 8 times inferior than buffer size
            if (read < buffer.length) {
                buffer = Arrays.copyOfRange(buffer, 0, read);
            }
            short[] signal = convertBytesToShort(buffer, buffer.length, byteOrder);
            short[] nextFullArray = new short[fullArray.length + signal.length];
            if(fullArray.length > 0) {
                System.arraycopy(fullArray, 0, nextFullArray, 0, fullArray.length);
            }
            System.arraycopy(signal, 0, nextFullArray, fullArray.length, signal.length);
            fullArray = nextFullArray;
        }
        return fullArray;
    }

    @Test
    public void core1khzTest() {
        Ptr yin = Yin.Yin_create();


        // Make 1000 Hz signal
        final int sampleRate = 44100;
        double windowTime = 0.0872;
        final int window = (int) Math.ceil(sampleRate * windowTime);
        final int signalFrequency = 1000;
        double powerRMS = 2500; // 90 dBspl
        double powerPeak = powerRMS * Math.sqrt(2);
        short[] signal = new short[window];
        for (int s = 0; s < signal.length; s++) {
            double t = s * (1 / (double) sampleRate);
            signal[s] = (short) (Math.sin(2 * Math.PI * signalFrequency * t) * (powerPeak));
        }

        long begin = System.currentTimeMillis();

        Yin.Yin_init(yin, (short)window, 0.1f);

        ShortPtr array = new ShortPtr(signal);

        float freq = Yin.Yin_getPitch(yin, array, sampleRate);

        for(int i=0; i<100;i++) {
            freq = Yin.Yin_getPitch(yin, array, sampleRate);
        }

        System.out.println("\ncore1khzTest:\nWindow: "+window+" samples ( "+Math.round(windowTime * 1000)+" ms)\n" +
                "Frequency: "+freq+"Hz\n" +
                "Probability: "+Math.round(Yin.Yin_getProbability(yin) * 100)+" %\n" +
                "Average evaluate time " + (System.currentTimeMillis() - begin) / 100+ " ms\n");

        assertEquals(signalFrequency, freq, 1.);

    }

    // Test parsing a recording of chirp sounds
    @Test
    public void coreRecording1Test() throws IOException {
        InputStream inputStream = YinACF_Test.class.getResourceAsStream("raw1_44100_16bitPCM.raw");
        short[] signal = loadShortStream(inputStream, ByteOrder.LITTLE_ENDIAN);

        double windowTime = 0.07;
        final int sampleRate = 44100;
        final int window = (int) Math.ceil(sampleRate * windowTime);

        Ptr yin = Yin.Yin_create();
        Yin.Yin_init(yin, (short)window, 0.1f);

        double[] pitchesTestTime = new double[]{0.237, 0.328, 0.411, 0.505, 0.584, 0.673, 0.761, 0.855, 0.940, 1.023,
                1.109, 1.201, 1.286, 1.461, 1.551, 1.629, 1.725, 1.825, 1.9};
        double[] pitchesTestFrequencies = new double[]{4698.6362866638, 5274.0409105875, 1760, 2349.3181433371, 1760,
                1760, 2093.0045224036, 2637.0204552996, 1760,1760, 1864.655046072, 2093.0045224036, 2959.9553816882,
                3322.4375806328, 4698.6362866638, 1760, 7458.6201842551, 6644.875161251, 2489.0158697739};

        assertEquals(pitchesTestFrequencies.length, pitchesTestTime.length);

        int testId = 0;
        for(double startTime : pitchesTestTime) {
            final int startSample = (int)(startTime * sampleRate);
            ShortPtr array = new ShortPtr(Arrays.copyOfRange(signal, startSample,startSample + window));

            float freq = Yin.Yin_getPitch(yin, array, sampleRate);
            String message =  "Failed "+(testId + 1)+"/"+pitchesTestTime.length+" with probability "
                    +Math.round(Yin.Yin_getProbability(yin) * 100)+" %";
            // Accept error of a third of semitone
            double delta = (pitchesTestFrequencies[testId]*(256./243.) - freq) / 3;
            assertEquals(message,pitchesTestFrequencies[testId], freq, delta);
            testId++;
        }


    }

}
