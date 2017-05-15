package com.example.santana.sirene;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class MFCC {
    private static int NUMBER_OF_CLASSES = 2;
    private static int NUMBER_OF_FEATURES = 12;
    private static int FFT_LENGTH = 512;
    private static int FFT_UNIQUE = 257;
    private static int nFilterBank = 26;
    private static int nCepstralCoeff = 12;
    private static int sineLifterParameter = 22;
    private static double frequencyRange[] = {300, 3700};

    public DoubleFFT_1D fft;
    public int nSamplesInFrame;
    //public double[] buffer;
    double[][] filterBank;
    double[][] dctMatrix;
    double[] lifterCoeff;
    double[][] mags;
    double samplingRate;

    public MFCC(int nSamplesInFrame, double samplingRate) throws Exception {
        this.fft = new DoubleFFT_1D(FFT_LENGTH);
        this.nSamplesInFrame = nSamplesInFrame;
        //this.buffer = new double[2*FFT_LENGTH];
        this.samplingRate = samplingRate;
        this.filterBank = generateTriangularFilterBank();

        this.dctMatrix = generateDCTMatrix();

        this.lifterCoeff = generateLifterCoeff();

        this.mags = new double[FFT_UNIQUE][1];

    }

    private double[][] computeMagnitude(double[] frame) {
        double[] buffer = new double[2 * FFT_LENGTH];
        double[][] frequencySpectrum = new double[FFT_UNIQUE][1];
        for(int j = 0; j < nSamplesInFrame; j++) {
            buffer[2 * j] = frame[j];
            buffer[2 * j + 1] = 0;
        }

        fft.complexForward(buffer);

        for(int n = 0; n < FFT_UNIQUE; n++) {
            double a = buffer[2 * n];
            double b = buffer[2 * n + 1];
            frequencySpectrum[n][0] = Math.sqrt(a * a + b * b);
        }

        return frequencySpectrum;
    }

    private double[][] generateTriangularFilterBank() {
        double fMin = 0;
        double fLow = frequencyRange[0];
        double fHigh = frequencyRange[1];
        double fMax = 0.5 * samplingRate;
        double[] fArray = new double[FFT_UNIQUE];
        double[][] filterBank = new double[nFilterBank][FFT_UNIQUE];

        //System.out.println("Frequency Array");
        for(int j = 0; j < FFT_UNIQUE; j++) {
            fArray[j] = fMin + j * (fMax - fMin) / (FFT_UNIQUE - 1);
            //System.out.println(fArray[j]);
        }

        double aCoeff = hz2mel(fLow);
        double bCoeff = (hz2mel(fHigh) - hz2mel(fLow)) / (nFilterBank + 1);
        double[] cutoffArray = new double[nFilterBank + 2];

        //System.out.println("Cutoff Frequency");
        for(int n = 0; n < nFilterBank + 2; n++) {
            cutoffArray[n] = mel2hz(aCoeff + n * bCoeff);
            //  System.out.println(cutoffArray[n]);
        }

        for(int m = 0; m < nFilterBank; m++) {
            for(int k = 0; k < FFT_UNIQUE; k++) {
                if((fArray[k] >= cutoffArray[m]) && (fArray[k] <= cutoffArray[m + 1]))
                    filterBank[m][k] = (fArray[k] - cutoffArray[m]) / (cutoffArray[m + 1] - cutoffArray[m]);
                if((fArray[k] >= cutoffArray[m + 1]) && (fArray[k] <= cutoffArray[m + 2]))
                    filterBank[m][k] = (cutoffArray[m + 2] - fArray[k]) / (cutoffArray[m + 2] - cutoffArray[m + 1]);
            }
        }
        return filterBank;
    }

    private double[][] generateDCTMatrix() {
        double[][] DCTMatrix = new double[nCepstralCoeff][nFilterBank];

        double a = Math.sqrt(2.0 / nFilterBank);
        System.out.println(a);
        double b = Math.PI / nFilterBank;
        System.out.println(b);
        for(int n = 0; n < nCepstralCoeff; n++)
            for(int m = 0; m < nFilterBank; m++)
                DCTMatrix[n][m] = a * Math.cos(n * b * (m + 0.5));

        return DCTMatrix;
    }

    private double[] generateLifterCoeff() {
        double[] lifterCoeff = new double[nCepstralCoeff];
        double a = Math.PI / sineLifterParameter;
        for(int n = 0; n < nCepstralCoeff; n++)
            lifterCoeff[n] = 1 + 0.5 * sineLifterParameter * Math.sin(a * n);
        return lifterCoeff;
    }

    private double hz2mel(double fHz) {
        double fMelArray;
        fMelArray = 1127 * Math.log(1 + fHz / 700);

        return fMelArray;
    }

    private double mel2hz(double fMel) {
        double fHz = 700 * Math.exp(fMel / 1127) - 700;
        return fHz;
    }

    public double[] extractFeature(double[] frame) {
        double[][] spectrum = computeMagnitude(frame);
        double[][] FBE = Utils.mult(filterBank, spectrum);

        for(int n = 0; n < FBE.length; n++) {
            for(int m = 0; m < FBE[0].length; m++) {
                FBE[n][m] = Math.log(FBE[n][m]);
            }
        }

        double[][] cepstralCoefficients = Utils.mult(dctMatrix, FBE);

        double[] MFCCs = new double[nCepstralCoeff];
        for(int n = 0; n < nCepstralCoeff; n++) {
            MFCCs[n] = lifterCoeff[n] * cepstralCoefficients[n][0];
        }

        return MFCCs;
    }
    //FBE = H*MAG;
    //CC = L*DCT*log(FBE)
}



