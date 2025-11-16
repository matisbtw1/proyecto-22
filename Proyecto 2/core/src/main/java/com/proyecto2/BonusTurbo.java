package com.proyecto2;

public class BonusTurbo implements Bonus {
    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarTurbo();
    }
}
