package com.proyecto2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Vehiculo {
    protected Texture texture;
    protected Rectangle bounds;
    protected boolean visible = true;

    // Rectángulo temporal para hitbox (evita crear garbage cada frame)
    private final Rectangle tmpHitbox = new Rectangle();

    public Vehiculo(Texture texture, float x, float y, float width, float height) {
        this.texture = texture;
        // Suavizado al escalar (no rompe si la textura se comparte)
        this.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.bounds = new Rectangle(x, y, width, height);
    }

    /** Lógica por frame */
    public abstract void update(float dt);

    /** Dibujo básico usando el tamaño actual de bounds */
    public void render(SpriteBatch batch) {
        if (!visible) return;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /** Ajusta el tamaño exacto */
    public void setSize(float w, float h) { bounds.setSize(w, h); }

    /** Escala multiplicando el tamaño actual */
    public void setScale(float s) { bounds.setSize(bounds.width * s, bounds.height * s); }

    /** Define el ancho objetivo y calcula el alto manteniendo proporción del sprite */
    public void setSizeByWidth(float targetWidth) {
        float ratio = texture.getHeight() / (float) texture.getWidth();
        bounds.setSize(targetWidth, targetWidth * ratio);
    }

    /** Posicionamiento */
    public void setPosition(float x, float y) { bounds.setPosition(x, y); }
    public void moveBy(float dx, float dy) { bounds.setPosition(bounds.x + dx, bounds.y + dy); }

    /** Centro en X/Y (útil para alinear a la mitad del carril) */
    public void setCenterX(float cx) { bounds.x = cx - bounds.width * 0.5f; }
    public float getCenterX() { return bounds.x + bounds.width * 0.5f; }
    public float getCenterY() { return bounds.y + bounds.height * 0.5f; }

    /** Visibilidad */
    public void setVisible(boolean v) { visible = v; }
    public boolean isVisible() { return visible; }

    /** Relación de aspecto del sprite (alto/ancho) */
    public float getAspect() { return texture.getHeight() / (float) texture.getWidth(); }

    /** Hitbox reducida (sx/sy en [0..1], p.ej. 0.8f = 80% del sprite) */
    public Rectangle getHitbox(float sx, float sy) {
        float w = bounds.width * sx;
        float h = bounds.height * sy;
        float x = bounds.x + (bounds.width - w) * 0.5f;
        float y = bounds.y + (bounds.height - h) * 0.5f;
        tmpHitbox.set(x, y, w, h);
        return tmpHitbox;
    }

    /** Accesores básicos */
    public Rectangle getBounds() { return bounds; }
    public Texture getTexture() { return texture; }

    public void dispose() {
        // Si gestionas texturas con AssetManager, NO las disposes aquí.
        // Déjalo vacío o controla con un flag si esta instancia es dueña de la textura.
    }
}
