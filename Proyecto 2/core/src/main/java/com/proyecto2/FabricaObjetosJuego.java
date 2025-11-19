package com.proyecto2;

public interface FabricaObjetosJuego {

    Bonus crearBonusEscudo();

    Bonus crearBonusTurbo();

    Bonus crearBonusVida();

    Malus crearMalusInvert(long duracionMs);
}
