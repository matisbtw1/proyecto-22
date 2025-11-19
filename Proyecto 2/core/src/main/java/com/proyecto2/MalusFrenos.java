package com.proyecto2;

public class MalusFrenos implements Malus {
    private final long durationMs;

    public MalusFrenos(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarFrenos(durationMs);
    }
}
