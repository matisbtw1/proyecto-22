package com.proyecto2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Vehiculo {
    protected Texture texture;
    protected Rectangle bounds;
    protected boolean visible = true;

    public Vehiculo(Texture texture, float x, float y, float width, float height) {
        this.texture = texture;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public abstract void update(float dt);

    public void render(SpriteBatch batch) {
        if (!visible) return;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void dispose() {}

    public Rectangle getBounds() { return bounds; }
}
