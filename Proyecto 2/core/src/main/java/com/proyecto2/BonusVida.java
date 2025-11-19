package com.proyecto2;

public class BonusVida implements Bonus {

    private final EfectoJugadorStrategy estrategia =
            new RepararVidaStrategy();

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        estrategia.apply(ctx, jugador);
    }
}
