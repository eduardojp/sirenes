package com.example.santana.sirene;

/**
 * Created by Henrique on 24/04/2017.
 */

import java.io.File;
import java.util.Locale;
import java.util.Scanner;

public class Utils {
    public static double[][] readMatrixFromXML(String[] List, int nRows, int nCols) throws Exception {
        double[][] hMatrix = new double[nRows][nCols];
        for(int i = 0; i < nCols; i++) {
            for(int j = 0; j < nRows; j++)
                hMatrix[j][i] = Double.parseDouble(List[i * nRows + j]);
        }
        return hMatrix;
    }

    public static double[][] readMatrix(String fileName, int width, int height) throws Exception {
        double[][] hMatrix = new double[width][height];
        Scanner scanner = new Scanner(new File(fileName));
        scanner.useLocale(Locale.ENGLISH);

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                hMatrix[i][j] = scanner.nextDouble();
                //System.out.print(hMatrix[i][j] + " ");
            }
            //System.out.println();
        }

        return hMatrix;
    }

    public static double[][] mult(double[][] A, double[][] B) {
        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;

        if(aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        double[][] C = new double[aRows][bColumns];
        for(int i = 0; i < aRows; i++) {
            for(int j = 0; j < bColumns; j++) {
                C[i][j] = 0.00000;
            }
        }

        for(int i = 0; i < aRows; i++) { // aRow
            for(int j = 0; j < bColumns; j++) { // bColumn
                for(int k = 0; k < aColumns; k++) { // aColumn
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }
}