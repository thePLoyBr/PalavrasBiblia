package br.com.quanticoapps.charadabiblica.entities;

import android.text.Editable;

import java.io.Serializable;

/**
 * Created by Felipe on 30/10/2017.
 */

public class Jogador implements Serializable{
    private int name;
    private String color;
    private int points;

    public Jogador(int name) {
        this.name = name;
        this.color = color;
    }

    public Jogador(int name, String color, int points) {
        this.name = name;
        this.color = color;
        this.points = points;
    }

    public String toString(){
        return "Nome : " + name + " Cor : " + color + " Pontos " + points;
    }

    //GetSet
}
