package com.proyecto2;

public class FabricaNormal implements FabricaObjetosJuego {

    @Override
    public Bonus crearBonusEscudo() {
        return new BonusEscudo();
    }

    @Override
    public Bonus crearBonusTurbo() {
        return new BonusTurbo();
    }

    @Override
    public Bonus crearBonusVida() {
        return new BonusVida();
    }

    @Override
    public Malus crearMalusInvert(long duracionMs) {
        return new MalusInvertControls(duracionMs);
    }
}
