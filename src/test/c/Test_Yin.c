/* Use ./TestAudio/audioExtract.py to create audioData.h - this converts a wave file to an array of doubles in C.
 * Copy audioData.h into same folder as this test then build and run - the program should give F0 of the signal in
 * audioData.h */
#define _USE_MATH_DEFINES

#include "Yin.h"
#include <stdio.h>
#include <stdint.h>
#include <math.h>
#include <assert.h>

#define SAMPLES 22050

#define MIN(a,b) (((a)<(b))?(a):(b))
#define MAX(a,b) (((a)>(b))?(a):(b))

int main(int argc, char** argv) {
	const int sampleRate = 44100;
	float minimalFrequency = 500;
	const int window = 512;
	double powerRMS = 2500; // 90 dBspl
	float signalFrequency = 1000;
	double powerPeak = powerRMS * sqrt(2);
	
	short audio[SAMPLES];

	for (int s = 0; s < SAMPLES; s++) {
		double t = s * (1 / (double)sampleRate);
		audio[s] = (short)(sin(2 * M_PI * signalFrequency * t) * (powerPeak));
	}
	Yin yin;
	float pitch = -1.0;


	Yin_init(&yin, window, 0.1f);
	pitch = Yin_getPitch(&yin, audio, sampleRate);
	Yin_free(&yin);

	printf("Pitch is found to be %f with buffer length %i and probabiity %f\n", pitch, window, Yin_getProbability(&yin));

	assert(abs(signalFrequency - pitch) < 1.f);


	return 0;
}
