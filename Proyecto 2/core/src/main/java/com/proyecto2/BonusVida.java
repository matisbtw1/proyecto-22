package com.proyecto2;

public class BonusVida implements Bonus {
    @Override
    public void apply(GestorObjetos ctx, Vehiculo jugador) {
        ctx.repararVida();
    }
}
