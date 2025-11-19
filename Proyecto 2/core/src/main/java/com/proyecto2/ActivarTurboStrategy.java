package com.proyecto2;

public class ActivarTurboStrategy implements EfectoJugadorStrategy {
    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarTurbo();
    }
}
