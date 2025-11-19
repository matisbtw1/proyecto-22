package com.proyecto2;

public class InvertirControlesStrategy implements EfectoJugadorStrategy {

    private final long durationMs;

    public InvertirControlesStrategy(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activateInvertControls(durationMs);
    }
}
