package com.example.santana.sirene;
//package tcc;

import android.util.Log;

/**
 * Created by henrique on 31/10/16.
 */
public class Neural {
    private double[][] inputWeights;
    private double[][] outputWeights;
    private double[][] layerWeights;
    private double[][] bias1;
    private double[][] bias2;
    private double[][] xmin;
    private double[][] xmax;
    private static int ymin = -1;
    private static int ymax = 1;
    private static int nMFCC = 12;
    private static int nFrames = Config.FRAMESPERDECISION;
    private static int hiddenLayerSize = Config.HIDDENLAYERSIZE;

    public Neural(double[][] inputWeights, double[][] layerWeights, double[][] bias1,
                  double[][] bias2, double[][] xmin, double[][] xmax) throws Exception {
        this.inputWeights = inputWeights;
        //this.inputWeights = new double[16][12];
        //this.outputWeights = new double[16][12];
        this.layerWeights = layerWeights;
        this.bias1 = bias1;
        this.bias2 = bias2;
        this.xmin = xmin;
        this.xmax = xmax;

        Log.d("Neural", "IW 1,1 " + inputWeights[0][0]);
        Log.d("Neural", "IW 1,2 " + inputWeights[0][1]);
        Log.d("Neural", "IW 2,1 " + inputWeights[1][0]);
        Log.d("Neural", "IW 7,6 " + inputWeights[6][5]);
        Log.d("Neural", "IW 16,12 " + inputWeights[15][11]);
        Log.d("Neural", "bias2 0 " + bias2[0][0]);
        Log.d("Neural", "bias2 1 " + bias2[1][0]);

    }

    private double[][] processInput(double[][] input) {
        double[][] processedInput = new double[nMFCC][nFrames];

        for(int jCol = 0; jCol < nFrames; jCol++) {
            for(int iRow = 0; iRow < nMFCC; iRow++) {
                processedInput[iRow][jCol] = (ymax - ymin) * (input[jCol][iRow] - xmin[iRow][0]) / (xmax[iRow][0] - xmin[iRow][0]) + ymin;
            }
        }
        return processedInput;
    }

    private double[][] evalInputLayer(double[][] processedInput) {
        double[][] transferFunctionInput = Utils.mult(inputWeights, processedInput);
        double[][] hiddenLayerInput = new double[hiddenLayerSize][nFrames];
        /*System.out.println("Hidden Layer Input Before tanh");
        for(int k = 0; k < 16; k++)
            System.out.println(transferFunctionInput[k][0] + bias1[k][0]);*/

        //System.out.println("tanh test:" + Math.tanh(-1.0676));

        for(int k = 0; k < nFrames; k++) {
            for(int j = 0; j < hiddenLayerSize; j++) {
                hiddenLayerInput[j][k] = Math.tanh(transferFunctionInput[j][k] + bias1[j][0]);
            }
        }

        //s2 = tansig(IW*s1 + b1);
        return hiddenLayerInput;
    }

    private double[][] evalOutputLayer(double[][] hiddenLayerInput) {
        double[][] transferFunctionInput = Utils.mult(layerWeights, hiddenLayerInput);
        double[][] output = new double[2][nFrames];

        /*System.out.println("Neural Output Before tanh");
        for(int k = 0; k < 2; k++)
            System.out.println(transferFunctionInput[k][0]);*/

        /*for(int k = 0; k < nFrames; k++)
            for(int j = 0; j < 2; j++)
                output[j][k] = Math.tanh(transferFunctionInput[j][k] + bias2[j][0]);*/
        for(int k = 0; k < nFrames; k++) {
            double a = Math.exp(transferFunctionInput[0][k] + bias2[0][0]);
            double b = Math.exp(transferFunctionInput[1][k] + bias2[1][0]);
            output[0][k] = a / (a + b);
            output[1][k] = b / (a + b);
        }


        //Utils.softMax(output);

        //s3 = softmax(LW*s2 + b2);

        return output;
    }


    public double[][] neuralDecision(double[][] MFCCs) {
        double[][] processedInput = processInput(MFCCs);

        /*System.out.println("Processed Input Frame 0");
        for(int k = 0; k < 12; k++)
            System.out.println(processedInput[k][0]);*/

        /*System.out.println("Processed Input Frame 50");
        for(int k = 0; k < 12; k++)
            System.out.println(processedInput[k][49]);*/
        //System.out.println("Processed Input");

        double[][] hiddenLayerInput = evalInputLayer(processedInput);
        /*System.out.println("Hidden Layer Input");
        for(int k = 0; k < 16; k++)
            System.out.println(hiddenLayerInput[k][0]);*/

        double[][] neuralDecision = evalOutputLayer(hiddenLayerInput);

        return neuralDecision;
    }

}