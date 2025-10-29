package com.proyecto2;

public class BonusEscudo implements Bonus {
    @Override
    public void apply(Lluvia ctx, Vehiculo jugador) {
        ctx.activarEscudo();
    }
}
