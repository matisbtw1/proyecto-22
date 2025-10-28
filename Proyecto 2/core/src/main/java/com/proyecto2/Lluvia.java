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
    private final Array<Rectangle> drops = new Array<Rectangle>();
    private final Array<Integer> dropType = new Array<Integer>(); // 0 = azul (+puntos), 1 = roja (+error)
    private Texture blueDropTex;
    private Texture redDropTex;
    private Sound dropSound;
    private Music rainMusic;
    private long lastSpawnTime = 0L;
    private float speed = 200f;
    private int puntos = 0;
    private int errores = 0;

    public void crear() {
    	blueDropTex = new Texture(Gdx.files.internal("bucket.png"));
    	redDropTex  = new Texture(Gdx.files.internal("bucket.png"));

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        spawnDrop();
    }

    private void spawnDrop() {
        Rectangle rect = new Rectangle();
        rect.x = MathUtils.random(0, 800 - 64);
        rect.y = 480;
        rect.width = 64;
        rect.height = 64;
        drops.add(rect);
        dropType.add(MathUtils.randomBoolean(0.75f) ? 0 : 1); // mayoría azules
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > 500) spawnDrop();

        for (int i = drops.size - 1; i >= 0; i--) {
            Rectangle r = drops.get(i);
            r.y -= speed * dt;
            if (r.y + r.height < 0) {
                drops.removeIndex(i);
                dropType.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < drops.size; i++) {
            Texture t = dropType.get(i) == 0 ? blueDropTex : redDropTex;
            Rectangle r = drops.get(i);
            batch.draw(t, r.x, r.y, r.width, r.height);
        }
    }

    public void chequearColision(Auto auto) {
        for (int i = drops.size - 1; i >= 0; i--) {
            if (auto.getBounds().overlaps(drops.get(i))) {
                if (dropType.get(i) == 0) {
                    puntos += 10;
                } else {
                    errores += 1;
                }
                dropSound.play();
                drops.removeIndex(i);
                dropType.removeIndex(i);
            }
        }
    }

    public int getPuntos() { return puntos; }
    public int getErrores() { return errores; }

    public void destruir() {
        if (blueDropTex != null) blueDropTex.dispose();
        if (redDropTex != null) redDropTex.dispose();
        if (dropSound != null) dropSound.dispose();
        if (rainMusic != null) rainMusic.dispose();
    }
}
