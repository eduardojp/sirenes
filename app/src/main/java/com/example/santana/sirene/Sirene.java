package com.example.santana.sirene;

/**
 * Created by henrique on 22/05/16.
 */
public class Sirene {
    /*bandas de frequência*/
    public float f1;
    public float f2;
    public float deltaf;
    public String nome;
    public float[] limites;

    public Sirene(float f1, float f2, float deltaf, String nome){
        this.f1 = f1;
        this.f2 = f2;
        this.deltaf = deltaf;
        this.nome = nome;
        this.limites = new float[] {f1 - deltaf, f1 + deltaf, f2 - deltaf, f2 + deltaf};
    }
}
