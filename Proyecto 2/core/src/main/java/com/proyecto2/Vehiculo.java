package com.proyecto2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Vehiculo {
    protected Texture texture;
    protected Rectangle bounds;
    protected boolean visible = true;

    // Rectángulo temporal para hitbox 
    private final Rectangle tmpHitbox = new Rectangle();

    public Vehiculo(Texture texture, float x, float y, float width, float height) {
        this.texture = texture;
        this.bounds = new Rectangle(x, y, width, height);
    }

    // ================== TEMPLATE METHOD ==================
    /**
     * Template Method del patrón Template Method.
     * Define la secuencia fija de actualización de cualquier vehículo:
     * 1) Actualizar movimiento según su tipo.
     * 2) Aplicar límites (carretera/pantalla).
     */
    public final void update(float dt) {
        if (!visible) return;
        actualizarMovimiento(dt); // definido por las subclases
        aplicarLimites();         // hook, se puede sobrescribir
    }

    /**
     * Paso obligatorio que cada tipo de vehículo debe implementar.
     * Aquí se leen inputs, se calcula dx/dy y se actualiza bounds.
     */
    protected abstract void actualizarMovimiento(float dt);

    /**
     * Hook opcional: por defecto no hace nada.
     * Las subclases pueden sobrescribir para limitar la posición.
     */
    protected void aplicarLimites() {
        // Por defecto sin límites; las subclases pueden redefinir
    }
    // ======================================================

    /** Movimiento genérico */
    protected void moveBy(float dx, float dy) {
        bounds.x += dx;
        bounds.y += dy;
    }

    /** Dibujado genérico */
    public void render(SpriteBatch batch) {
        if (!visible) return;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Hitbox genérica: rectángulo centrado dentro de bounds
     * escalado por factores sx, sy.
     */
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
    public void setSizeByWidth(float targetWidth) {
        float aspectRatio = texture.getHeight() / (float) texture.getWidth();
        bounds.width = targetWidth;
        bounds.height = targetWidth * aspectRatio;
    }
    public void setCenterX(float centerX) {
        bounds.x = centerX - bounds.width / 2f;
    }


    public void dispose() {
        // Si algún día quieres liberar la textura aquí, hazlo con cuidado
        // (ahora las texturas las maneja el Singleton AssetsJuego)
    }
}
