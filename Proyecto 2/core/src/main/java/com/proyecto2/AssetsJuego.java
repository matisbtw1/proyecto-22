package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

/**
 * Singleton encargado de la CARGA y LIBERACIÓN de todos los assets del juego.
 * Ninguna otra clase hace new Texture / new Sound / new Music.
 */
public final class AssetsJuego {

    private static AssetsJuego instancia;

    // === Texturas ===
    public final Texture texCarretera;
    public final Texture texAutoJugador;
    public final Texture texMotoJugador;
    public final Texture texPolicia;
    public final Texture texEscudo;
    public final Texture texTurbo;
    public final Texture texVida;
    public final Texture texCono;
    public final Texture texAceite;
    public final Texture texHoyo;
    public final Texture texFreno;

    // === Audio ===
    public final Music musicFondo;
    public final Sound sfxPickup;
    public final Sound sfxChoque; // si no tienes hurt.ogg puedes dejarlo sin usar

    private AssetsJuego() {
        // OJO: los nombres deben coincidir con los archivos en tu carpeta assets
        texCarretera   = new Texture(Gdx.files.internal("carretera.png"));
        texAutoJugador = new Texture(Gdx.files.internal("player_lambo.png"));
        texMotoJugador = new Texture(Gdx.files.internal("MotoRoja.png"));
        texPolicia     = new Texture(Gdx.files.internal("police_explorer.png"));
        texEscudo      = new Texture(Gdx.files.internal("shield.png"));
        texTurbo       = new Texture(Gdx.files.internal("turbo.png"));
        texVida        = new Texture(Gdx.files.internal("vida.png"));
        texCono        = new Texture(Gdx.files.internal("cono.png"));
        texAceite      = new Texture(Gdx.files.internal("aceite.png"));
        texHoyo        = new Texture(Gdx.files.internal("hoyo.png"));
        texFreno       = new Texture(Gdx.files.internal("freno.png"));

        // Suavizar al escalar
        texCarretera.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texAutoJugador.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texMotoJugador.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texPolicia.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texEscudo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texTurbo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texVida.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texCono.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texAceite.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texHoyo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texFreno.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Audio
        musicFondo = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));   // cambia el nombre si tu música es otra
        sfxPickup  = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        sfxChoque  = Gdx.audio.newSound(Gdx.files.internal("hurt.ogg"));   // opcional
    }

    /** Acceso al singleton */
    public static AssetsJuego get() {
        if (instancia == null) {
            instancia = new AssetsJuego();
        }
        return instancia;
    }

    /** Liberar todos los recursos al cerrar el juego */
    public void dispose() {
        texCarretera.dispose();
        texAutoJugador.dispose();
        texMotoJugador.dispose();
        texPolicia.dispose();
        texEscudo.dispose();
        texTurbo.dispose();
        texVida.dispose();
        texCono.dispose();
        texAceite.dispose();
        texHoyo.dispose();
        texFreno.dispose();

        musicFondo.dispose();
        sfxPickup.dispose();
        sfxChoque.dispose();
    }
}
