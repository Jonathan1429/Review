package com.jonathanev.review.Clases;

public class ColoresPregunta {
    private int inicioColor;
    private int finColor;
    private int color;

    public ColoresPregunta(int inicioColor, int finColor, int color) {
        this.inicioColor = inicioColor;
        this.finColor = finColor;
        this.color = color;
    }

    public int getInicioColor() {
        return inicioColor;
    }

    public void setInicioColor(int inicioColor) {
        this.inicioColor = inicioColor;
    }

    public int getFinColor() {
        return finColor;
    }

    public void setFinColor(int finColor) {
        this.finColor = finColor;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
