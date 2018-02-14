package org.noise_planet.yin;

import org.junit.Test;
import org.renjin.gcc.runtime.MixedPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.ShortPtr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example of using a compiled C function from Java
 */
public class YinACF_Test {

    @Test
    public void core1khzTest() {
        Ptr yin = Yin.Yin_create();


        // Make 1000 Hz signal
        final int sampleRate = 44100;
        final int window = (int) Math.ceil(sampleRate * 0.05);
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

        System.out.println("Window: "+window+"Evaluate time " + (System.currentTimeMillis() - begin)+ " ms");

        assertEquals(signalFrequency, freq, 1.);

    }

}
