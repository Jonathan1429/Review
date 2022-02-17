package com.example.repasar.Clases;

import java.util.Objects;

public class Guias {
    String nombreGuia;
    int imgGuia;

    public Guias() {
    }

    public Guias(String nombreGuia, int imgGuia) {
        this.nombreGuia = nombreGuia;
        this.imgGuia = imgGuia;
    }

    public String getNombreGuia() {
        return nombreGuia;
    }

    public void setNombreGuia(String nombreGuia) {
        this.nombreGuia = nombreGuia;
    }

    public int getImgGuia() {
        return imgGuia;
    }

    public void setImgGuia(int imgGuia) {
        this.imgGuia = imgGuia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guias guias = (Guias) o;
        return Objects.equals(nombreGuia, guias.nombreGuia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreGuia);
    }
}
