package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Moto extends Vehiculo {
	private float velocidad = 260f;      // si quieres, un poco más baja que antes
    private float inerciaX = 0f;         // solo inercia horizontal
    private float factorDerrapeX = 1.2f; // 0.6 = derrapa pero se controla

    private final float roadMinX = GestorObjetos.ROAD_LEFT;
    private final float roadMaxX = GestorObjetos.ROAD_RIGHT;

    private final Rectangle hb = new Rectangle();

    public Moto(Texture textura, float x, float y, float w, float h) {
        super(textura, x, y, w, h);
    }

    @Override
    protected void actualizarMovimiento(float dt) {
        // Controles: WASD + flechas
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A)     || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D)     || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean up    = Gdx.input.isKeyPressed(Input.Keys.W)     || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down  = Gdx.input.isKeyPressed(Input.Keys.S)     || Gdx.input.isKeyPressed(Input.Keys.DOWN);

        // Invertir controles si el malus está activo
        if (invertControls) {
            boolean tmpL = left;
            left = right;
            right = tmpL;

            boolean tmpU = up;
            up = down;
            down = tmpU;
        }

        // Movimiento "puro" por input (sin inercia aún)
        float dxInput = 0f;
        float dy      = 0f;

        if (left)  dxInput -= velocidad * dt;
        if (right) dxInput += velocidad * dt;
        if (up)    dy      += velocidad * dt;
        if (down)  dy      -= velocidad * dt;

        // Aplicar inercia SOLO en X
        float dx = dxInput + inerciaX;

        if (GestorObjetos.isAceiteActivo()) {
            int dir = GestorObjetos.getAceiteDir();
            dx += dir * velocidad * 0.7f * dt; // igual idea que en Auto
        }
        
        bounds.x += dx;
        bounds.y += dy;

	     // Actualizar inercia horizontal:
	     // Si NO estoy presionando en X, que la inercia se vaya pero MUY de a poco
	     if (!left && !right) {
	         inerciaX *= 0.92f;           // antes 0.6 → ahora resbala mucho más
	         if (Math.abs(inerciaX) < 2f) {
	             inerciaX = 0f;
	         }
	     } else {
	         // Si estoy girando, acumulo un poco de inercia extra
	         // (se siente que la cola "sigue" el giro)
	         inerciaX += dxInput * factorDerrapeX;   // antes era solo asignar
	         // Limitar la inercia máxima para que no se descontrole
	         float maxInercia = velocidad * dt * 1.5f;
	         if (inerciaX >  maxInercia) inerciaX =  maxInercia;
	         if (inerciaX < -maxInercia) inerciaX = -maxInercia;
	     }

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
