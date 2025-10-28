package com.proyecto2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Road {
    private final Texture tex;
    private final int W, H;
    private float y1 = 0, y2;
    private float speed;

    public Road(Texture tex, int W, int H, float speed) {
        this.tex = tex; this.W = W; this.H = H; this.speed = speed;
        this.y2 = H;
    }

    public void update(float dt) {
        y1 -= speed * dt;
        y2 -= speed * dt;
        if (y1 <= -H) y1 = y2 + H;
        if (y2 <= -H) y2 = y1 + H;
    }

    public void draw(SpriteBatch batch) {
        // Dibuja el PNG de la carretera ocupando todo el ancho y alto visibles
        batch.draw(tex, 0, y1, W, H);
        batch.draw(tex, 0, y2, W, H);
    }
}
