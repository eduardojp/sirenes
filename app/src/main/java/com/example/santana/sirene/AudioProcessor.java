package com.example.santana.sirene;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by henrique on 07/05/16.
 */

/**
 * Created by henrique on 07/05/16.
 */
public class AudioProcessor {
    private int frameSize;
    private int frameStep;
    private static double preemphasisCoefficient = 0.97d;
    private double[] window;

    private int NMFCC = 12;
    private int frameNumber;
    private float mainInterval;
    //private double[] filteredSignal;
    private int threshold;
    private double[] bufferSamples;

    private Conversor conversor;
    private Neural neuralNetwork;
    private MFCC mfcc;

    private LinkedList<Sirene> sirenes;
    public int NFrames;

    private LinkedList<Double> mainList;
    private LinkedList<Double> sampleList;
    //private short[] sampleList;
    private static final String TAG = AudioProcessor.class.getSimpleName();

    public AudioProcessor(float frameSpan, int NFrames,
                          int sampleRate, Neural neuralNetwork) throws Exception {
            /*inicialização do conversor*/
        this.conversor = new Conversor(sampleRate);
        this.neuralNetwork = neuralNetwork;

            /*tamanho do frame em número de amostras*/
        this.NFrames = NFrames;
        this.mainInterval = NFrames * frameSpan;
        //this.frameSize = conversor.timeToSamples(frameSpan);
        this.frameSize = Config.SAMPLESPERFRAME;

        this.mfcc = new MFCC(frameSize, sampleRate);

        //this.filteredSignal = new double[frameSize*NFrames];

            /*tamanho de novas amostradas coletadas*/
        this.sampleList = new LinkedList<>();
        this.bufferSamples = new double[frameSize * NFrames];

        this.window = hammingWindow();
        //this.filteredSignal = new double[frameSize*NFrames];


        //Log.d("Constants", "FrameSize: " + frameSize);
        //Log.d("Constants", "LagSize: " + lagSize);
    }

    Boolean processBuffer(short[] bufferSamples, int nSamples) throws Exception {
        Log.d("BufferSamples", "BufferSamples Length: " + bufferSamples.length);
        /*for(int i = 0; i < nSamples; i++){
            sampleList.add(buffer[i]);
        }*/

        MFCC mfcc = new MFCC(frameSize, 44100);

        //conversor.updateTime(nSamples);

            /*acumula amostras até mainListSize*/
        /*if(conversor.time >= mainInterval){
            int i = 0;*/

        /*while(i < NFrames*frameSize){
                bufferSamples[i] = sampleList.remove();
                i++;
            }*/


        //Processamento
        //Pré-ênfase
        double[] filteredSignal = preemphasisFilter(bufferSamples);

        //Framing
        double[][] signalInFrames = frameSignal(filteredSignal);

        //Extração da característica
        double[][] MFCCArray = new double[NFrames][NMFCC];

        Boolean isSiren = null;
        for(int j = 0; j < NFrames; j++) {
            MFCCArray[j] = mfcc.extractFeature(signalInFrames[j]);
        }
        Log.d("MFCCs", "MFCCs Frame 0 ");
        for(int k = 0; k < 12; k++)
            Log.d("MFCCs", "" + MFCCArray[0][k]);

        Log.d("MFCCs", "MFCCs Frame 50 ");
        for(int k = 0; k < 12; k++)
            Log.d("MFCCs", "" + MFCCArray[49][k]);
        //Julgamento da rede neural
        double[][] neuralOutput = neuralNetwork.neuralDecision(MFCCArray);

        Log.d("NeuralOutput", "Frame 0 " + neuralOutput[0][0] + " " + neuralOutput[1][0]);
        Log.d("NeuralOutput", "Frame 50 " + neuralOutput[0][49] + " " + neuralOutput[1][49]);

        //Filtro de saída


        //conversor.timeReset();
        isSiren = outputFilter(neuralOutput);


        return isSiren;
        //return null;
    }
    ///return null;
    //}

    //Filtro de pré-emphase com alpha = 0.97;
    private double[] preemphasisFilter(short[] inputSamples) {
        double[] filteredSignal = new double[frameSize * NFrames];
        filteredSignal[0] = inputSamples[0];
        for(int n = 1; n < frameSize * NFrames; n++) {
            filteredSignal[n] = inputSamples[n] - preemphasisCoefficient * inputSamples[n - 1];
        }
        return filteredSignal;
    }

    private double[][] frameSignal(double[] filteredSignal) {
        double[][] signalInFrames = new double[NFrames][frameSize];
        for(int n = 0; n < NFrames; n++)
            for(int m = 0; m < frameSize; m++) {
                signalInFrames[n][m] = filteredSignal[n * frameSize + m] * window[m];
            }
        return signalInFrames;
    }

    private double[] hammingWindow() {
        double[] hamming = new double[frameSize];
        for(int j = 0; j < frameSize; j++)
            hamming[j] = 0.54d - 0.46d * Math.cos(2 * Math.PI * j / (frameSize - 1));
        return hamming;
    }


    private boolean outputFilter(double[][] neuralOutput) {

        double isSiren = 0.0d;
        double isNotSiren = 0.0d;
        for(int k = 0; k < NFrames; k++) {
            isSiren += neuralOutput[0][k];
            isNotSiren += neuralOutput[1][k];
        }

        isSiren = isSiren / NFrames;
        isNotSiren = isNotSiren / NFrames;

        return isSiren > isNotSiren;
    }

}
