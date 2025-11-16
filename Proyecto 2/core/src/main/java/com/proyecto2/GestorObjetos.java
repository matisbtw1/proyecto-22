package com.proyecto2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * GestorObjetos:
 * - Spawnea enemigos, bonus y malus.
 * - Actualiza posiciones.
 * - Maneja colisiones, puntaje, vidas y estados (escudo, turbo, invert).
 * 
 * YA NO carga ni libera assets: usa AssetsJuego.
 */
public class GestorObjetos {

    // Tipos de objeto
    private static final int ENEMIGO      = 0;
    private static final int BONUS_ESCUDO = 1;
    private static final int BONUS_TURBO  = 2;
    private static final int BONUS_VIDA   = 3;
    private static final int MALUS_INVERT = 4;

    private final Array<Rectangle> objetos = new Array<>();
    private final Array<Integer>  tipos    = new Array<>();

    // Spawner / movimiento
    private long  lastSpawnTime   = 0L;
    private long  spawnIntervalMs = 750;
    private float speed           = 260f;

    // Puntaje y errores
    private int   errores          = 0;
    private int   puntos           = 0;
    private float puntosFrac       = 0f;
    private float puntosPorSegundo = 10f;

    // === Geometría de la carretera ===
    public static final int   LANES      = 4;
    public static final float ROAD_LEFT  = 140f;
    public static final float ROAD_RIGHT = 800f - 140f;

    private static final float ENEMY_W_FACTOR = 0.58f;
    private static final float ITEM_W_FACTOR  = 0.42f;

    private static final float ENEMY_H_FACTOR = 0.82f;
    private static final float ITEM_H_FACTOR  = 0.92f;

    private static final float MAX_AR_ENEMY = 1.75f;
    private static final float MAX_AR_ITEM  = 1.35f;

    // Colisión
    private static final float COLLISION_SHRINK = 0.12f;
    private final Rectangle tmpV = new Rectangle();
    private final Rectangle tmpO = new Rectangle();
    private int lastLane = -1;

    // Bonus estados
    private boolean escudoActivo   = false;
    private long    escudoStartMs  = 0L;
    private static final long DURACION_ESCUDO_MS = 5000L;

    private boolean turboActivo    = false;
    private long    turboStartMs   = 0L;
    private static final long DURACION_TURBO_MS = 5000L;
    private float   bonusVelocidad = 1.15f;

    private boolean justPickedVida = false;
    private long    vidaStartMs    = 0L;
    private static final long DURACION_VIDA_MS = 1000L;

    // Malus (controles invertidos)
    private boolean controlsInverted = false;
    private long    invertStartMs    = 0L;
    private long    invertDurationMs = 0L;
    private static final long DURACION_INVERT_MS = 5000L;

    // Probabilidades
    private float probEscudo      = 0.07f;
    private float probTurbo       = 0.07f;
    private float probVida        = 0.07f;
    private float probMalusInvert = 0.06f;

    // Singleton "lógico" para la consulta areControlsInverted()
    private static GestorObjetos instance = null;

    // === Helpers estáticos (usados desde Main, Auto, Moto) ===
    public static float laneWidth() {
        return (ROAD_RIGHT - ROAD_LEFT) / (float) LANES;
    }

    public static float laneCenterX(int laneIndex) {
        return ROAD_LEFT + laneWidth() * (laneIndex + 0.5f);
    }

    public static float roadMinX() { return ROAD_LEFT; }
    public static float roadMaxX() { return ROAD_RIGHT; }

    public static boolean areControlsInverted() {
        return instance != null && instance.controlsInverted;
    }

    // === Ciclo de vida ===
    public GestorObjetos() {
        instance = this;
    }

    public void crear() {
        // la música de fondo la maneja el singleton de assets
        AssetsJuego.get().musicFondo.setLooping(true);
        AssetsJuego.get().musicFondo.play();
        spawnObjeto();
    }

    // === Lógica de spawn ===

    private void spawnObjeto() {
        // 1) decidir tipo
        float rand = MathUtils.random();
        int tipo;
        if (rand < probEscudo) tipo = BONUS_ESCUDO;
        else if (rand < probEscudo + probTurbo) tipo = BONUS_TURBO;
        else if (rand < probEscudo + probTurbo + probVida) tipo = BONUS_VIDA;
        else if (rand < probEscudo + probTurbo + probVida + probMalusInvert) tipo = MALUS_INVERT;
        else tipo = ENEMIGO;

        // 2) carril aleatorio distinto al anterior
        int lane;
        do { lane = MathUtils.random(0, LANES - 1); }
        while (LANES > 1 && lane == lastLane);
        lastLane = lane;

        float lw = laneWidth();
        boolean esEnemigoOMalus = (tipo == ENEMIGO || tipo == MALUS_INVERT);
        float targetW = esEnemigoOMalus ? (lw * ENEMY_W_FACTOR) : (lw * ITEM_W_FACTOR);

        // 3) textura para calcular aspect ratio
        Texture tex = textureForType(tipo);
        float texAR = tex.getHeight() / (float) tex.getWidth(); // alto/ancho
        float maxAR = esEnemigoOMalus ? MAX_AR_ENEMY : MAX_AR_ITEM;
        float usedAR = Math.min(texAR, maxAR);

        float flat    = esEnemigoOMalus ? ENEMY_H_FACTOR : ITEM_H_FACTOR;
        float targetH = targetW * usedAR * flat;

        Rectangle r = new Rectangle();
        r.width  = targetW;
        r.height = targetH;
        r.y      = 480;
        r.x      = Math.round(laneCenterX(lane) - r.width / 2f);

        objetos.add(r);
        tipos.add(tipo);
        lastSpawnTime = TimeUtils.millis();
    }

    private Texture textureForType(int tipo) {
        AssetsJuego a = AssetsJuego.get();
        switch (tipo) {
            case BONUS_ESCUDO: return a.texEscudo;
            case BONUS_TURBO:  return a.texTurbo;
            case BONUS_VIDA:   return a.texVida;
            case MALUS_INVERT: return a.texCono;
            case ENEMIGO:
            default:           return a.texPolicia;
        }
    }

    // === Update / movimiento ===

    public void update(float dt) {
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) {
            spawnObjeto();
        }

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

        if (escudoActivo   && TimeUtils.timeSinceMillis(escudoStartMs)  > DURACION_ESCUDO_MS)  escudoActivo   = false;
        if (turboActivo    && TimeUtils.timeSinceMillis(turboStartMs)   > DURACION_TURBO_MS)   turboActivo    = false;
        if (justPickedVida && TimeUtils.timeSinceMillis(vidaStartMs)    > DURACION_VIDA_MS)    justPickedVida = false;
        if (controlsInverted && TimeUtils.timeSinceMillis(invertStartMs) > invertDurationMs)   controlsInverted = false;
    }

    // === Render ===

    public void render(SpriteBatch batch) {
        for (int i = 0; i < objetos.size; i++) {
            Rectangle r = objetos.get(i);
            int tipo = tipos.get(i);
            Texture t = textureForType(tipo);
            batch.draw(t, r.x, r.y, r.width, r.height);
        }
    }

    // === Colisiones ===

    public void chequearColision(Vehiculo vehiculo) {
        shrinkInto(vehiculo.getBounds(), tmpV, COLLISION_SHRINK);

        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            shrinkInto(r, tmpO, COLLISION_SHRINK);

            if (tmpV.overlaps(tmpO)) {
                int tipo = tipos.get(i);

                if (tipo == ENEMIGO && !escudoActivo) {
                    errores++;
                    AssetsJuego.get().sfxChoque.play();
                } else if (tipo == MALUS_INVERT) {
                    Malus malus = new MalusInvertControls(DURACION_INVERT_MS);
                    malus.apply(this, vehiculo);
                } else {
                    Bonus bonus = null;
                    switch (tipo) {
                        case BONUS_ESCUDO: bonus = new BonusEscudo(); break;
                        case BONUS_TURBO:  bonus = new BonusTurbo();  break;
                        case BONUS_VIDA:   bonus = new BonusVida();   break;
                        default: break;
                    }
                    if (bonus != null) bonus.apply(this, vehiculo);
                    AssetsJuego.get().sfxPickup.play();
                }

                objetos.removeIndex(i);
                tipos.removeIndex(i);
            }
        }
    }

    private static void shrinkInto(Rectangle src, Rectangle dst, float frac) {
        float dx = src.width  * frac;
        float dy = src.height * frac;
        dst.set(src.x + dx, src.y + dy, src.width - 2f * dx, src.height - 2f * dy);
    }

    // === Métodos invocados por Bonus / Malus ===

    public void activarEscudo() {
        escudoActivo  = true;
        escudoStartMs = TimeUtils.millis();
    }

    public void activarTurbo() {
        turboActivo  = true;
        turboStartMs = TimeUtils.millis();
    }

    public void repararVida() {
        if (errores > 0) errores--;
        justPickedVida = true;
        vidaStartMs    = TimeUtils.millis();
    }

    public void activateInvertControls(long dur) {
        controlsInverted = true;
        invertStartMs    = TimeUtils.millis();
        invertDurationMs = dur;
    }

    // === Getters para HUD / lógica externa ===

    public int  getPuntos()          { return puntos; }
    public int  getErrores()         { return errores; }
    public boolean isEscudoActivo()  { return escudoActivo; }
    public boolean isTurboActivo()   { return turboActivo; }
    public boolean justPickedVida()  { return justPickedVida; }
    public boolean isControlsInverted() { return controlsInverted; }
    public float getBonusVelocidad() { return bonusVelocidad; }

    // Ya no hacemos dispose de assets aquí, porque no somos dueños.
    public void destruir() {
        objetos.clear();
        tipos.clear();
        if (AssetsJuego.get().musicFondo.isPlaying()) {
            AssetsJuego.get().musicFondo.stop();
        }
    }
}
