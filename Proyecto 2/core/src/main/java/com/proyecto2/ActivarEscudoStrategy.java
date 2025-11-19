package com.proyecto2;

public class ActivarEscudoStrategy implements EfectoJugadorStrategy {
    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarEscudo();
    }
}
