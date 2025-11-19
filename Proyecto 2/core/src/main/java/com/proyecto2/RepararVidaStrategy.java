package com.proyecto2;

public class RepararVidaStrategy implements EfectoJugadorStrategy {
    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.repararVida();
    }
}
