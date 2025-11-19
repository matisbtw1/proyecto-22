package com.proyecto2;

public class BonusTurbo implements Bonus {

    private final EfectoJugadorStrategy estrategia =
            new ActivarTurboStrategy();

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        estrategia.apply(ctx, jugador);
    }
}
