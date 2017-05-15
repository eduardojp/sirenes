package com.example.santana.sirene;

/**
 * Created by henrique on 22/05/16.
 */
public class Conversor {
    public int sampleRate;
    public float time;

    public Conversor(int sampleRate) {
        this.sampleRate = sampleRate;
        this.time = 0;
    }

    public void updateTime(int samples) {
        time += (float) samples / (float) sampleRate;
    }

    public int getMDFIndex(float f) {
        return Math.round(sampleRate / f);
    }

    public int timeToSamples(float t) {
        return Math.round(t * sampleRate);
    }

    void timeReset() {
        time = 0;
    }
}
