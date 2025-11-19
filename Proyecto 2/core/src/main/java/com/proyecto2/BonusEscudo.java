package com.proyecto2;

public class BonusEscudo implements Bonus {

    private final EfectoJugadorStrategy estrategia =
            new ActivarEscudoStrategy();

    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        estrategia.apply(ctx, jugador);
    }
}
