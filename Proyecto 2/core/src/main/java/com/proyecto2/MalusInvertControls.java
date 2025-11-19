package com.proyecto2;

public class MalusInvertControls implements Malus {

    private final EfectoJugadorStrategy estrategia;

    public MalusInvertControls(long durationMs) {
        this.estrategia = new InvertirControlesStrategy(durationMs);
    }

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        estrategia.apply(ctx, jugador);
    }
}
