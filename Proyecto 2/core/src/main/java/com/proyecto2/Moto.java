package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Moto extends Vehiculo {
    private float velocidad = 160f;
    private float inerciaX = 0f, inerciaY = 0f;
    private float factorDerrape = 0.96f;

    private final float roadMinX = GestorObjetos.ROAD_LEFT;
    private final float roadMaxX = GestorObjetos.ROAD_RIGHT;

    private final Rectangle hb = new Rectangle();

    public Moto(Texture textura, float x, float y, float w, float h) {
        super(textura, x, y, w, h);
    }

    @Override
  
    protected void actualizarMovimiento(float dt) {
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up    = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down  = Gdx.input.isKeyPressed(Input.Keys.S);

        float dx = 0f, dy = 0f;
        if (left)  dx -= velocidad * dt;
        if (right) dx += velocidad * dt;
        if (up)    dy += velocidad * dt;
        if (down)  dy -= velocidad * dt;

        dx += inerciaX; 
        dy += inerciaY;

        bounds.x += dx;
        bounds.y += dy;

        inerciaX = dx * factorDerrape;
        inerciaY = dy * factorDerrape;
    }

    @Override
    protected void aplicarLimites() {
        if (bounds.x < roadMinX) bounds.x = roadMinX;
        if (bounds.x + bounds.width > roadMaxX)
            bounds.x = roadMaxX - bounds.width;
        if (bounds.y < 0) bounds.y = 0;
        if (bounds.y + bounds.height > 480)
            bounds.y = 480 - bounds.height;
    }

}
