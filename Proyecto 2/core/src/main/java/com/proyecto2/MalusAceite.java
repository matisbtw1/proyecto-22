package com.proyecto2;

public class MalusAceite implements Malus {
    private final long durationMs;

    public MalusAceite(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarAceite(durationMs);
    }
}
