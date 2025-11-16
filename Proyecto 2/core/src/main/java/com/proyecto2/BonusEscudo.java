package com.proyecto2;

public class BonusEscudo implements Bonus {
    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.activarEscudo();
    }
}
