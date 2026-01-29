package core;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private static SoundManager instance;
    private Clip menuMusic;
    private float currentMusicVolume = 1.0f; // 0.0 to 1.0 (fade factor)
    private boolean isMenuMusicPlaying = false;

    private SoundManager() {
        // Load menu music
        try {
            // Tenta carregar menu_os.wav, se não existir, tenta menu_st.wav
            File musicFile = new File("musics/menu_os.wav");
            if (!musicFile.exists()) {
                musicFile = new File("musics/menu_st.wav");
            }
            
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
                menuMusic = AudioSystem.getClip();
                menuMusic.open(audioInput);
            } else {
                System.err.println("Arquivo de música de menu não encontrado!");
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void playMenuMusic() {
        if (menuMusic != null) {
            if (!isMenuMusicPlaying) {
                menuMusic.setFramePosition(0);
                menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
                menuMusic.start();
                isMenuMusicPlaying = true;
            }
            updateVolume(); // Apply current settings volume + fade
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && isMenuMusicPlaying) {
            menuMusic.stop();
            isMenuMusicPlaying = false;
        }
    }

    public void setFadeFactor(float factor) {
        // factor: 1.0 = volume normal, 0.0 = mudo
        this.currentMusicVolume = Math.max(0.0f, Math.min(1.0f, factor));
        updateVolume();
    }

    public void updateVolume() {
        if (menuMusic != null) {
            int globalVolume = Settings.getInstance().getVolume();
            float finalVolume = (globalVolume / 100.0f) * currentMusicVolume;
            
            // Converter para dB
            // Volume 0 não pode ser log10(0), então tratamos como caso especial
            setClipVolume(menuMusic, finalVolume);
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            float dB;
            if (volume <= 0.0001f) {
                dB = gainControl.getMinimum();
            } else {
                dB = 20f * (float) Math.log10(volume);
            }
            
            // Clamp value
            if (dB < gainControl.getMinimum()) dB = gainControl.getMinimum();
            if (dB > gainControl.getMaximum()) dB = gainControl.getMaximum();
            
            gainControl.setValue(dB);
        }
    }
}
