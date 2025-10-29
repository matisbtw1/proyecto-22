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
    // Tipos de objeto
    private static final int ENEMIGO      = 0;
    private static final int BONUS_ESCUDO = 1;
    private static final int BONUS_TURBO  = 2;
    private static final int BONUS_VIDA   = 3;

    // Objetos y tipos
    private final Array<Rectangle> objetos = new Array<>();
    private final Array<Integer>   tipos   = new Array<>();

    // Texturas y audio
    private Texture texEnemigo, texEscudo, texTurbo, texVida;
    private Sound dropSound;
    private Music rainMusic;

    // Spawner / movimiento
    private long  lastSpawnTime = 0L;
    private long  spawnIntervalMs = 700;  // ↑ = menos tráfico
    private float speed = 270f;           // velocidad de caída

    // Puntuación y errores
    private int   errores = 0;
    private int   puntos  = 0;
    private float puntosFrac = 0f;
    private float puntosPorSegundo = 10f;

    // Carriles (ajusta estos márgenes para encajar con tu fondo 16:9 "carretera.png")
    public static final int   LANES = 4;
    public static final float ROAD_LEFT  = 180f;         // borde izquierdo asfalto
    public static final float ROAD_RIGHT = 800f - 180f;  // borde derecho asfalto

    // Tamaños de sprite
    private static final float OBJ_W = 90f;
    private static final float OBJ_H = 160f;

    private int lastLane = -1;

    // ESCUDO
    private boolean escudoActivo = false;
    private long escudoStartMs = 0L;
    private static final long DURACION_ESCUDO_MS = 5000L;

    // TURBO
    private boolean turboActivo = false;
    private long turboStartMs = 0L;
    private static final long DURACION_TURBO_MS = 5000L;
    private float bonusVelocidad = 1.15f; // +15%

    // VIDA (halo breve)
    private boolean justPickedVida = false;
    private long vidaStartMs = 0L;
    private static final long DURACION_VIDA_MS = 1000L; // 1s halo verde

    // Probabilidades de bonus
    private float probEscudo = 0.07f;  // 7%
    private float probTurbo  = 0.07f;  // 7%
    private float probVida   = 0.07f;  // 7%

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

        // Carril aleatorio (evita repetir el mismo)
        float laneWidth = (ROAD_RIGHT - ROAD_LEFT) / (float) LANES;
        int lane;
        do { lane = MathUtils.random(0, LANES - 1); }
        while (LANES > 1 && lane == lastLane);
        lastLane = lane;

        float laneCenterX = ROAD_LEFT + laneWidth * (lane + 0.5f);
        r.x = laneCenterX - OBJ_W / 2f;

        // Decide el tipo según probabilidades
        float rand = MathUtils.random();
        int tipo;
        if (rand < probEscudo) {
            tipo = BONUS_ESCUDO;
        } else if (rand < probEscudo + probTurbo) {
            tipo = BONUS_TURBO;
        } else if (rand < probEscudo + probTurbo + probVida) {
            tipo = BONUS_VIDA;
        } else {
            tipo = ENEMIGO;
        }

        objetos.add(r);
        tipos.add(tipo);
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) spawnObjeto();

        // Mover objetos hacia abajo (con turbo la escena va un poco más rápida)
        float worldSpeedMul = turboActivo ? bonusVelocidad : 1f;
        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            r.y -= speed * dt * worldSpeedMul;
            if (r.y + r.height < 0) {
                objetos.removeIndex(i);
                tipos.removeIndex(i);
            }
        }

        // Puntos por tiempo (también acelera con turbo)
        puntosFrac += puntosPorSegundo * dt * worldSpeedMul;
        if (puntosFrac >= 1f) {
            int inc = (int) puntosFrac;
            puntos += inc;
            puntosFrac -= inc;
        }

        // Expiraciones de efectos
        if (escudoActivo && TimeUtils.timeSinceMillis(escudoStartMs) > DURACION_ESCUDO_MS)
            escudoActivo = false;

        if (turboActivo && TimeUtils.timeSinceMillis(turboStartMs) > DURACION_TURBO_MS)
            turboActivo = false;

        if (justPickedVida && TimeUtils.timeSinceMillis(vidaStartMs) > DURACION_VIDA_MS)
            justPickedVida = false;
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < objetos.size; i++) {
            Rectangle r = objetos.get(i);
            int tipo = tipos.get(i);
            Texture t;
            switch (tipo) {
                case BONUS_ESCUDO:
                    t = texEscudo; break;
                case BONUS_TURBO:
                    t = texTurbo; break;
                case BONUS_VIDA:
                    t = texVida; break;
                default:
                    t = texEnemigo; break;
            }
            batch.draw(t, r.x, r.y, r.width, r.height);
        }
    }

    public void chequearColision(Vehiculo vehiculo) {
        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            if (vehiculo.getBounds().overlaps(r)) {
                int tipo = tipos.get(i);

                if (tipo == ENEMIGO) {
                    if (!escudoActivo) errores++;
                } else if (tipo == BONUS_ESCUDO) {
                    escudoActivo = true;
                    escudoStartMs = TimeUtils.millis();
                } else if (tipo == BONUS_TURBO) {
                    turboActivo = true;
                    turboStartMs = TimeUtils.millis();
                } else if (tipo == BONUS_VIDA) {
                    if (errores > 0) errores--;
                    justPickedVida = true;
                    vidaStartMs = TimeUtils.millis();
                }

                if (dropSound != null) dropSound.play();
                objetos.removeIndex(i);
                tipos.removeIndex(i);
            }
        }
    }

    // Getters para Main/HUD
    public boolean isEscudoActivo()      { return escudoActivo; }
    public boolean isTurboActivo()       { return turboActivo; }
    public boolean justPickedVida()      { return justPickedVida; }
    public float  getBonusVelocidad()    { return bonusVelocidad; }
    public int    getPuntos()            { return puntos; }
    public int    getErrores()           { return errores; }

    public void destruir() {
        if (texEnemigo != null) texEnemigo.dispose();
        if (texEscudo  != null) texEscudo.dispose();
        if (texTurbo   != null) texTurbo.dispose();
        if (texVida    != null) texVida.dispose();
        if (dropSound  != null) dropSound.dispose();
        if (rainMusic  != null) rainMusic.dispose();
    }
}
