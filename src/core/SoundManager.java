package core;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Timer;

public class SoundManager {
    private static SoundManager instance;
    private Clip menuMusic;
    private Clip radioClip;
    
    private float globalFadeFactor = 1.0f; // Managed by ScreenManager for transitions
    private boolean isMenuMusicPlaying = false;
    private boolean isRadioPlaying = false;
    private Timer menuFadeTimer;
    private float menuLocalVolume = 1.0f; // Fade-in do menu
    
    // Radio variables
    private List<File> radioPlaylist;
    private int currentRadioIndex = 0;
    private Timer radioCrossfadeTimer;
    private float radioLocalVolume = 1.0f; // For per-track fading
    private boolean isFadingOutForNext = false;

    private SoundManager() {
        // Load menu music
        try {
            AudioInputStream audioInput = null;
            InputStream is = getClass().getResourceAsStream("/musics/menu_os.wav");
            if (is != null) {
                audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            }
            if (audioInput == null) {
                InputStream fallback = getClass().getResourceAsStream("/musics/menu_st.wav");
                if (fallback != null) {
                    audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(fallback));
                }
            }
            
            if (audioInput != null) {
                menuMusic = AudioSystem.getClip();
                menuMusic.open(audioInput);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setupRadioPlaylist();
    }
    
    private void setupRadioPlaylist() {
        radioPlaylist = new ArrayList<>();
        
        // Load from resources inside JAR
        String[] musicFiles = {
            "/musics/audio-club-amapiano-319840.wav",
            "/musics/memphis-trap-wav-349366.wav",
            "/musics/tokyo-rain-serenade-archer-sounds-321180.wav",
            "/musics/warm-nights-amp-lofi-dreams-archer-sounds-321177.wav"
        };
        
        for (String path : musicFiles) {
            try {
                if (getClass().getResourceAsStream(path) != null) {
                    // We'll store the path as string and load on demand
                    File tempFile = new File(path); // Just to store reference
                    radioPlaylist.add(tempFile);
                }
            } catch (Exception e) {
                System.out.println("Could not load: " + path);
            }
        }
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // --- MENU MUSIC CONTROL ---
    public void playMenuMusic() {
        stopRadio(); // Ensure radio is off
        
        if (menuMusic != null) {
            if (!isMenuMusicPlaying) {
                menuMusic.setFramePosition(0);
                menuLocalVolume = 0.0f;
                updateVolume();
                menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
                menuMusic.start();
                isMenuMusicPlaying = true;
                startMenuFadeIn();
            }
            updateVolume(); 
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && isMenuMusicPlaying) {
            menuMusic.stop();
            isMenuMusicPlaying = false;
        }
    }

    private void startMenuFadeIn() {
        if (menuFadeTimer != null && menuFadeTimer.isRunning()) {
            menuFadeTimer.stop();
        }
        menuFadeTimer = new Timer(50, e -> {
            menuLocalVolume += 0.05f;
            if (menuLocalVolume >= 1.0f) {
                menuLocalVolume = 1.0f;
                ((Timer) e.getSource()).stop();
            }
            updateVolume();
        });
        menuFadeTimer.start();
    }

    // --- RADIO CONTROL ---
    public void startRadio() {
        stopMenuMusic(); // Ensure menu music is off
        if (radioPlaylist.isEmpty()) return;
        
        // Start with a random track
        currentRadioIndex = (int) (Math.random() * radioPlaylist.size());
        playRadioTrack(currentRadioIndex);
    }
    
    public void stopRadio() {
        if (radioClip != null && radioClip.isRunning()) {
            radioClip.stop();
            radioClip.close();
        }
        isRadioPlaying = false;
    }
    
    public void nextTrack() {
        if (radioPlaylist.isEmpty()) return;
        fadeOutAndChangeTrack(1);
    }
    
    public void prevTrack() {
        if (radioPlaylist.isEmpty()) return;
        fadeOutAndChangeTrack(-1);
    }
    
    public String getCurrentTrackName() {
        if (radioPlaylist.isEmpty() || currentRadioIndex < 0 || currentRadioIndex >= radioPlaylist.size()) {
            return "No Signal";
        }
        return radioPlaylist.get(currentRadioIndex).getName().replace(".wav", "");
    }

    private void fadeOutAndChangeTrack(int direction) {
        if (isFadingOutForNext) return; // Prevent double trigger
        
        isFadingOutForNext = true;
        
        if (radioCrossfadeTimer != null && radioCrossfadeTimer.isRunning()) {
            radioCrossfadeTimer.stop();
        }
        
        radioCrossfadeTimer = new Timer(50, e -> {
            radioLocalVolume -= 0.1f;
            if (radioLocalVolume <= 0.0f) {
                radioLocalVolume = 0.0f;
                ((Timer)e.getSource()).stop();
                isFadingOutForNext = false;
                
                // Change track
                currentRadioIndex = (currentRadioIndex + direction);
                // Handle wrap around
                if (currentRadioIndex >= radioPlaylist.size()) currentRadioIndex = 0;
                if (currentRadioIndex < 0) currentRadioIndex = radioPlaylist.size() - 1;
                
                playRadioTrack(currentRadioIndex);
            }
            updateVolume();
            
        });
        radioCrossfadeTimer.start();
    }

    private void playRadioTrack(int index) {
        stopRadio(); // Stop current
        
        try {
            File track = radioPlaylist.get(index);
            String resourcePath = track.getPath().replace("\\", "/");
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                return;
            }
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            radioClip = AudioSystem.getClip();
            radioClip.open(audioInput);
            
            // Loop the selected track continuously
            radioClip.loop(Clip.LOOP_CONTINUOUSLY);
            radioClip.start();
            isRadioPlaying = true;
            
            // Fade In
            radioLocalVolume = 0.0f;
            if (radioCrossfadeTimer != null) radioCrossfadeTimer.stop();
            
            radioCrossfadeTimer = new Timer(50, e -> {
                radioLocalVolume += 0.05f;
                if (radioLocalVolume >= 1.0f) {
                    radioLocalVolume = 1.0f;
                    ((Timer)e.getSource()).stop();
                }
                updateVolume();
            });
            radioCrossfadeTimer.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- SHARED VOLUME ---
    public void setFadeFactor(float factor) {
        this.globalFadeFactor = Math.max(0.0f, Math.min(1.0f, factor));
        updateVolume();
    }

    public void updateVolume() {
        int globalSettingsVolume = Settings.getInstance().getVolume();
        float baseVolume = (globalSettingsVolume / 100.0f);
        
        // Calculate final volumes
        float menuFinal = baseVolume * globalFadeFactor * menuLocalVolume;
        
        // Radio is affected by global fade AND local fade (crossfade)
        float radioFinal = baseVolume * globalFadeFactor * radioLocalVolume;
        
        if (menuMusic != null) setClipVolume(menuMusic, menuFinal);
        if (radioClip != null) setClipVolume(radioClip, radioFinal);
    }

    private void setClipVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                float dB;
                if (volume <= 0.0001f) {
                    dB = gainControl.getMinimum();
                } else {
                    dB = 20f * (float) Math.log10(volume);
                }
                
                // Clamp
                if (dB < gainControl.getMinimum()) dB = gainControl.getMinimum();
                if (dB > gainControl.getMaximum()) dB = gainControl.getMaximum();
                
                gainControl.setValue(dB);
            }
        } catch (Exception e) {
            // Ignore closed clip errors
        }
    }
}
