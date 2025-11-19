package com.proyecto2;

public class MalusHoyo implements Malus {
    private final long  durationMs;
    private final float factor;

    public MalusHoyo(long durationMs, float factor) {
        this.durationMs = durationMs;
        this.factor     = factor;
    }

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarHoyo(durationMs, factor);
    }
}
