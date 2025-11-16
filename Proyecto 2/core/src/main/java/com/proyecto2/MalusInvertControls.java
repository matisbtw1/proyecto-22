package com.proyecto2;

public class MalusInvertControls implements Malus {
    private final long durationMs;

    public MalusInvertControls(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activateInvertControls(durationMs);
    }
}
