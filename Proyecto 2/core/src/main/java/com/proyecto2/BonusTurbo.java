package com.proyecto2;

public class BonusTurbo implements Bonus {
    @Override
    public void apply(Lluvia ctx, Vehiculo jugador) {
        ctx.activarTurbo();
    }
}
