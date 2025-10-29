package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Lluvia {
    // Tipos
    private static final int ENEMIGO      = 0;
    private static final int BONUS_ESCUDO = 1;
    private static final int BONUS_TURBO  = 2;
    private static final int BONUS_VIDA   = 3;

    // Objetos y tipos
    private final Array<Rectangle> objetos = new Array<>();
    private final Array<Integer>   tipos   = new Array<>();

    // Texturas / audio
    private Texture texEnemigo, texEscudo, texTurbo, texVida;
    private Sound dropSound;
    private Music rainMusic;

    // Spawner / movimiento
    private long  lastSpawnTime = 0L;
    private long  spawnIntervalMs = 700;
    private float speed = 270f;

    // Puntos y errores
    private int   errores = 0;
    private int   puntos  = 0;
    private float puntosFrac = 0f;
    private float puntosPorSegundo = 10f;

    // Geometría carretera (ajusta si cambias el fondo)
    public static final int   LANES = 4;
    public static final float ROAD_LEFT  = 180f;         // borde izq. asfalto
    public static final float ROAD_RIGHT = 800f - 180f;  // borde der. asfalto

    // Tamaños de lo que cae (render y lógica usan lo mismo)
    private static final float OBJ_W = 110f;
    private static final float OBJ_H = 130f;

    // Afinar colisión (reduce rect por cada lado)
    private static final float COLLISION_SHRINK = 0.12f; // 12% del ancho/alto

    private int lastLane = -1;

    // Estados de bonus
    private boolean escudoActivo = false;
    private long escudoStartMs = 0L;
    private static final long DURACION_ESCUDO_MS = 5000L;

    private boolean turboActivo = false;
    private long turboStartMs = 0L;
    private static final long DURACION_TURBO_MS = 5000L;
    private float bonusVelocidad = 1.15f;

    private boolean justPickedVida = false;
    private long vidaStartMs = 0L;
    private static final long DURACION_VIDA_MS = 1000L;

    // Probabilidades
    private float probEscudo = 0.07f;
    private float probTurbo  = 0.07f;
    private float probVida   = 0.07f;

    // Rects temporales para colisión “shrink”
    private final Rectangle tmpV = new Rectangle();
    private final Rectangle tmpO = new Rectangle();

    // --------- Helpers públicos para Main (y coherencia de carriles)
    public static float laneWidth() {
        return (ROAD_RIGHT - ROAD_LEFT) / (float) LANES;
    }
    public static float laneCenterX(int laneIndex) {
        return ROAD_LEFT + laneWidth() * (laneIndex + 0.5f);
    }
    public static float roadMinX() { return ROAD_LEFT; }
    public static float roadMaxX() { return ROAD_RIGHT; }

    public void crear() {
        texEnemigo = new Texture(Gdx.files.internal("police_explorer.png"));
        texEscudo  = new Texture(Gdx.files.internal("shield.png"));
        texTurbo   = new Texture(Gdx.files.internal("turbo.png"));
        texVida    = new Texture(Gdx.files.internal("vida.png"));

        dropSound  = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic  = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        spawnObjeto();
    }

    private void spawnObjeto() {
        Rectangle r = new Rectangle();
        r.y = 480;
        r.width  = OBJ_W;
        r.height = OBJ_H;

        int lane;
        do { lane = MathUtils.random(0, LANES - 1); } while (LANES > 1 && lane == lastLane);
        lastLane = lane;

        float xCenter = laneCenterX(lane);
        r.x = xCenter - OBJ_W / 2f;

        // decide tipo
        float rand = MathUtils.random();
        int tipo;
        if (rand < probEscudo) tipo = BONUS_ESCUDO;
        else if (rand < probEscudo + probTurbo) tipo = BONUS_TURBO;
        else if (rand < probEscudo + probTurbo + probVida) tipo = BONUS_VIDA;
        else tipo = ENEMIGO;

        objetos.add(r);
        tipos.add(tipo);
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) spawnObjeto();

        float mul = turboActivo ? bonusVelocidad : 1f;

        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            r.y -= speed * dt * mul;
            if (r.y + r.height < 0) {
                objetos.removeIndex(i);
                tipos.removeIndex(i);
            }
        }

        puntosFrac += puntosPorSegundo * dt * mul;
        if (puntosFrac >= 1f) {
            int inc = (int) puntosFrac;
            puntos += inc;
            puntosFrac -= inc;
        }

        if (escudoActivo && TimeUtils.timeSinceMillis(escudoStartMs) > DURACION_ESCUDO_MS) escudoActivo = false;
        if (turboActivo  && TimeUtils.timeSinceMillis(turboStartMs)  > DURACION_TURBO_MS)  turboActivo  = false;
        if (justPickedVida && TimeUtils.timeSinceMillis(vidaStartMs) > DURACION_VIDA_MS)   justPickedVida = false;
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < objetos.size; i++) {
            Rectangle r = objetos.get(i);
            int tipo = tipos.get(i);
            Texture t;
            switch (tipo) {
                case BONUS_ESCUDO: t = texEscudo; break;
                case BONUS_TURBO:  t = texTurbo;  break;
                case BONUS_VIDA:   t = texVida;   break;
                default:           t = texEnemigo; break;
            }
            float drawW = r.width;
            float drawH = r.height;

            // Reducir tamaño solo para bonus
            if (tipo == BONUS_ESCUDO || tipo == BONUS_TURBO || tipo == BONUS_VIDA) {
                drawW *= 0.85f;  // 75% del tamaño original
                drawH *= 0.f;
            }

            batch.draw(t, r.x + (r.width - drawW) / 2f, r.y, drawW, drawH);

        }
    }

    public void chequearColision(Vehiculo vehiculo) {
        // Construye hitbox “reducida” del jugador
        shrinkInto(vehiculo.getBounds(), tmpV, COLLISION_SHRINK);

        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            shrinkInto(r, tmpO, COLLISION_SHRINK); // hitbox reducida del objeto

            if (tmpV.overlaps(tmpO)) {
                int tipo = tipos.get(i);

                if (tipo == ENEMIGO) {
                    if (!escudoActivo) errores++;
                } else {
                    // Interfaz Bonus
                    Bonus bonus = null;
                    switch (tipo) {
                        case BONUS_ESCUDO: bonus = new BonusEscudo(); break;
                        case BONUS_TURBO:  bonus = new BonusTurbo();  break;
                        case BONUS_VIDA:   bonus = new BonusVida();   break;
                    }
                    if (bonus != null) bonus.apply(this, vehiculo);
                }

                if (dropSound != null) dropSound.play();
                objetos.removeIndex(i);
                tipos.removeIndex(i);
            }
        }
    }

    // Reduce un rectángulo para usar como “hitbox” (menos borde invisible)
    private static void shrinkInto(Rectangle src, Rectangle dst, float frac) {
        float dx = src.width  * frac;
        float dy = src.height * frac;
        dst.set(src.x + dx, src.y + dy, src.width - 2f * dx, src.height - 2f * dy);
    }

    // Métodos invocados por Bonus.*
    public void activarEscudo() { escudoActivo = true; escudoStartMs = TimeUtils.millis(); }
    public void activarTurbo()  { turboActivo  = true; turboStartMs  = TimeUtils.millis(); }
    public void repararVida()   { if (errores > 0) errores--; justPickedVida = true; vidaStartMs = TimeUtils.millis(); }

    // Getters usados por Main/HUD
    public boolean isEscudoActivo()   { return escudoActivo; }
    public boolean isTurboActivo()    { return turboActivo; }
    public boolean justPickedVida()   { return justPickedVida; }
    public float  getBonusVelocidad() { return bonusVelocidad; }
    public int    getPuntos()         { return puntos; }
    public int    getErrores()        { return errores; }

    public void destruir() {
        if (texEnemigo != null) texEnemigo.dispose();
        if (texEscudo  != null) texEscudo.dispose();
        if (texTurbo   != null) texTurbo.dispose();
        if (texVida    != null) texVida.dispose();
        if (dropSound  != null) dropSound.dispose();
        if (rainMusic  != null) rainMusic.dispose();
    }
}
