package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Auto extends Vehiculo {
    private float roadMinX, roadMaxX;
    private float speed = 520f;
    private final Rectangle hb = new Rectangle(); // hitbox reducido

    /** Constructor original (compatibilidad) */
    public Auto(Texture texture, float x, float y, float width, float height, float roadMinX, float roadMaxX) {
        super(texture, x, y, width, height);
        this.roadMinX = roadMinX;
        this.roadMaxX = roadMaxX;
    }

    /**
     * Constructor recomendado: define el auto por el CENTRO del carril y el ANCHO objetivo.
     * Mantiene proporción automáticamente y centra el sprite en X.
     */
    public Auto(Texture texture, float laneCenterX, float y, float targetWidth, float roadMinX, float roadMaxX) {
        super(texture, 0f, y, 1f, 1f);
        setSizeByWidth(targetWidth);   // calcula alto según textura
        setCenterX(laneCenterX);       // lo centra en el carril
        this.roadMinX = roadMinX;
        this.roadMaxX = roadMaxX;
    }

    @Override
    public void update(float dt) {
        float dx = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) dx -= speed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx += speed * dt;

        moveBy(dx, 0f);

        // Limitar al asfalto
        if (bounds.x < roadMinX) bounds.x = roadMinX;
        if (bounds.x + bounds.width > roadMaxX) bounds.x = roadMaxX - bounds.width;
    }

    /** Hitbox escalada (por defecto úsala con 0.8f, 0.8f) */
    public Rectangle getHitbox(float sx, float sy) {
        float nx = bounds.x + (1f - sx) * 0.5f * bounds.width;
        float ny = bounds.y + (1f - sy) * 0.5f * bounds.height;
        hb.set(nx, ny, bounds.width * sx, bounds.height * sy);
        return hb;
    }

    // --- utilidades opcionales ---
    public void setSpeed(float speed) { this.speed = speed; }
    public float getSpeed() { return speed; }

    public void setRoadBounds(float minX, float maxX) { this.roadMinX = minX; this.roadMaxX = maxX; }
    public float getRoadMinX() { return roadMinX; }
    public float getRoadMaxX() { return roadMaxX; }
}
