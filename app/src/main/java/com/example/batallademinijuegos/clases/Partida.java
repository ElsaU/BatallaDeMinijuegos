package com.example.batallademinijuegos.clases;

public class Partida {
    public String id;
    public String jugador1;
    public String jugador2;
    public int puntosJ1;
    public int puntosJ2;
    public boolean estadoJ1;
    public boolean estadoJ2;

    public Partida(String id, String j1, String j2, int puntosJ1, int puntosJ2){
        this.id = id;
        this.jugador1 = j1;
        this.jugador2 = j2;
        this.puntosJ1 = puntosJ1;
        this.puntosJ2 = puntosJ2;
    }

    public Partida(String id, String j1, String j2){
        this.id = id;
        this.jugador1 = j1;
        this.jugador2 = j2;
    }
}
