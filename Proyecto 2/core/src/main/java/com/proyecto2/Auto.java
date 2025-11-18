package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Auto extends Vehiculo {
    private float roadMinX, roadMaxX;
    private float speed = 480f;
    private final Rectangle hb = new Rectangle();

    public Auto(Texture texture, float x, float y, float width, float height, float roadMinX, float roadMaxX) {
        super(texture, x, y, width, height);
        this.roadMinX = roadMinX;
        this.roadMaxX = roadMaxX;
    }

    public Auto(Texture texture, float laneCenterX, float y, float targetWidth, float roadMinX, float roadMaxX) {
        super(texture, 0f, y, 1f, 1f);
        setSizeByWidth(targetWidth);
        setCenterX(laneCenterX);
        this.roadMinX = roadMinX;
        this.roadMaxX = roadMaxX;
    }

    @Override
    protected void actualizarMovimiento(float dt) {
        // Soportar flechas + WASD
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        // Invertir controles si el malus está activo
        if (invertControls) {
            boolean tmp = left;
            left = right;
            right = tmp;
        }

        float dx = 0f;
        if (left)  dx -= speed * dt;
        if (right) dx += speed * dt;

        moveBy(dx, 0f);
    }


    @Override
    protected void aplicarLimites() {
        if (bounds.x < roadMinX) bounds.x = roadMinX;
        if (bounds.x + bounds.width > roadMaxX) {
            bounds.x = roadMaxX - bounds.width;
        }
    }

    public Rectangle getHitbox(float sx, float sy) {
        float nx = bounds.x + (1f - sx) * 0.5f * bounds.width;
        float ny = bounds.y + (1f - sy) * 0.5f * bounds.height;
        hb.set(nx, ny, bounds.width * sx, bounds.height * sy);
        return hb;
    }
}
