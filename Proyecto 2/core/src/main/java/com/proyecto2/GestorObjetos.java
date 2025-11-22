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
 *
 */
public class GestorObjetos {

    // Tipos de objeto
    private static final int ENEMIGO      = 0;
    private static final int BONUS_ESCUDO = 1;
    private static final int BONUS_TURBO  = 2;
    private static final int BONUS_VIDA   = 3;
    private static final int MALUS_INVERT = 4;
    private static final int MALUS_ACEITE = 5;
    private static final int MALUS_HOYO   = 6;
    private static final int MALUS_FRENO  = 7;

    private FabricaObjetosJuego fabricaObjetos;


    private final Array<Rectangle> objetos = new Array<>();
    private final Array<Integer>  tipos    = new Array<>();

    // Spawner / movimiento
    private long  lastSpawnTime   = 0L;
    private long  spawnIntervalMs = 750;
    private float speed           = 260f;

    // Valores base para la dificultad
    private static final long  BASE_SPAWN_INTERVAL_MS = 750L;
    private static final float BASE_SPEED             = 260f;
    private static final int   BASE_MAX_ENEMIGOS      = 4;

    // Estrategia de dificultad
    private DificultadStrategy dificultadStrategy;

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

    // Malus (aceite: derrape lateral forzado)
    private boolean aceiteActivo   = false;
    private long    aceiteStartMs  = 0L;
    private long    aceiteDurationMs = 0L;
    private int     aceiteDir      = 0;   // -1 = izquierda, 1 = derecha
    private static final long DURACION_ACEITE_MS = 1500L; // 1.5s aprox
    private static final float ACEITE_FORCE = 0.7f; // % de la velocidad base

    // Malus (freno brutal: jugador no se puede mover)
    private boolean frenoActivo   = false;
    private long    frenoStartMs  = 0L;
    private static final long DURACION_FRENO_MS = 1200L; // 1.2s aprox

    public boolean isFrenoActivo() { return frenoActivo; }

    
    public static boolean isAceiteActivo() {
        return instance != null && instance.aceiteActivo;
    }

    public static int getAceiteDir() {
        return instance != null ? instance.aceiteDir : 0;
    }
    
    public static float getAceiteForce() {
        return ACEITE_FORCE;
    }

    // Malus (hoyo: slow)
    private boolean slowActivo   = false;
    private long    slowStartMs  = 0L;
    private static final long DURACION_HOYO_MS = 2000L; // 2s
    private float   slowFactor   = 0.5f; // 50% de la velocidad

    public boolean isSlowActivo() { return slowActivo; }
    public float getSlowFactor()  { return slowFactor; }

    
    // Probabilidades
    private float probEscudo      = 0.06f;
    private float probTurbo       = 0.06f;
    private float probVida        = 0.06f;
    private float probMalusInvert = 0.05f;
    private float probMalusAceite = 0.05f;
    private float probMalusHoyo   = 0.05f;
    private float probMalusFreno  = 0.05f;
    // el resto del rango (hasta 1.0) se lo queda ENEMIGO


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
        this.dificultadStrategy = new DificultadNormalStrategy();
        this.fabricaObjetos     = new FabricaNormal();
    }

    public GestorObjetos(DificultadStrategy dificultadStrategy,
                         FabricaObjetosJuego fabricaObjetos) {
        instance = this;
        this.dificultadStrategy = dificultadStrategy;
        this.fabricaObjetos     = fabricaObjetos;
    }


    public void crear() {
        spawnObjeto();
    }

    // === Lógica de spawn ===

    private void spawnObjeto() {
        // 1) decidir tipo
        float rand = MathUtils.random();
        int tipo;

        float p1 = probEscudo;
        float p2 = p1 + probTurbo;
        float p3 = p2 + probVida;
        float p4 = p3 + probMalusInvert;
        float p5 = p4 + probMalusAceite;
        float p6 = p5 + probMalusHoyo;
        float p7 = p6 + probMalusFreno;

        if      (rand < p1) tipo = BONUS_ESCUDO;
        else if (rand < p2) tipo = BONUS_TURBO;
        else if (rand < p3) tipo = BONUS_VIDA;
        else if (rand < p4) tipo = MALUS_INVERT;
        else if (rand < p5) tipo = MALUS_ACEITE;
        else if (rand < p6) tipo = MALUS_HOYO;
        else if (rand < p7) tipo = MALUS_FRENO;
        else                tipo = ENEMIGO;

        
        // Si es enemigo, verificar máximo permitido por la dificultad
        if (tipo == ENEMIGO && dificultadStrategy != null) {
            int maxEnemigos = dificultadStrategy.calcularMaxEnemigos(
                    BASE_MAX_ENEMIGOS, puntos);

            int actuales = 0;
            for (int i = 0; i < tipos.size; i++) {
                if (tipos.get(i) == ENEMIGO) actuales++;
            }
            if (actuales >= maxEnemigos) {
                // No spawneamos más enemigos ahora
                lastSpawnTime = TimeUtils.millis();
                return;
            }
        }


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
            case MALUS_ACEITE: return a.texAceite;
            case MALUS_HOYO: return a.texHoyo;
            case MALUS_FRENO: return a.texFreno;
            case ENEMIGO:
            default:           return a.texPolicia;
        }
    }

    // === Update / movimiento ===

    public void update(float dt) {
    	// Actualizar parámetros en función de la dificultad y el puntaje
        if (dificultadStrategy != null) {
            spawnIntervalMs = dificultadStrategy.calcularSpawnIntervalMs(
                    BASE_SPAWN_INTERVAL_MS, puntos);
            speed = dificultadStrategy.calcularSpeed(
                    BASE_SPEED, puntos);
        }
    	
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) {
            spawnObjeto();
        }

        float mul = 1f;
        if (turboActivo) mul *= bonusVelocidad;
        if (slowActivo)  mul *= slowFactor;

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
        if (aceiteActivo && TimeUtils.timeSinceMillis(aceiteStartMs) > aceiteDurationMs) {
            aceiteActivo = false;
            aceiteDir    = 0;
        }
        if (slowActivo && TimeUtils.timeSinceMillis(slowStartMs) > DURACION_HOYO_MS) slowActivo = false;
        if (frenoActivo && TimeUtils.timeSinceMillis(frenoStartMs) > DURACION_FRENO_MS) frenoActivo = false;

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

                } else if (tipo == MALUS_ACEITE) {
                    Malus malus = new MalusAceite(DURACION_ACEITE_MS);
                    malus.apply(this, vehiculo);

                } else if (tipo == MALUS_HOYO) {
                    Malus malus = new MalusHoyo(DURACION_HOYO_MS, 0.5f);
                    malus.apply(this, vehiculo);

                } else if (tipo == MALUS_FRENO) {
                    Malus malus = new MalusFrenos(DURACION_FRENO_MS);
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
    
    public void activarAceite(long durMs) {
        aceiteActivo    = true;
        aceiteStartMs   = TimeUtils.millis();
        aceiteDurationMs = durMs;
        // Elegir aleatorio si patina hacia izquierda (-1) o derecha (1)
        aceiteDir = MathUtils.randomBoolean() ? -1 : 1;
    }
    
    public void activarHoyo(long durMs, float factor) {
        slowActivo  = true;
        slowStartMs = TimeUtils.millis();
        slowFactor  = factor;
    }

    public void activarFrenos(long durMs) {
        frenoActivo  = true;
        frenoStartMs = TimeUtils.millis();
    }



    // === Getters para HUD / lógica externa ===

    public int  getPuntos()          { return puntos; }
    public int  getErrores()         { return errores; }
    public boolean isEscudoActivo()  { return escudoActivo; }
    public boolean isTurboActivo()   { return turboActivo; }
    public boolean justPickedVida()  { return justPickedVida; }
    public boolean isControlsInverted() { return controlsInverted; }
    public float getBonusVelocidad() { return bonusVelocidad; }

    public void destruir() {
        objetos.clear();
        tipos.clear();
        if (AssetsJuego.get().musicFondo.isPlaying()) {
            AssetsJuego.get().musicFondo.stop();
        }
    }
}
