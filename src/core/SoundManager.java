package core;

import javax.sound.sampled.*;
import java.io.File;
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
    
    // Radio variables
    private List<File> radioPlaylist;
    private int currentRadioIndex = 0;
    private Timer radioCrossfadeTimer;
    private float radioLocalVolume = 1.0f; // For per-track fading
    private boolean isFadingOutForNext = false;

    private SoundManager() {
        // Load menu music
        try {
            File musicFile = new File("musics/menu_os.wav");
            if (!musicFile.exists()) {
                musicFile = new File("musics/menu_st.wav");
            }
            
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
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
        File musicDir = new File("musics");
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            System.out.println("Pasta de músicas não encontrada: musics/");
            return;
        }
        
        File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (files == null || files.length == 0) {
            return;
        }
        
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (name.equals("menu_st.wav")) {
                continue; // excluir música do menu
            }
            radioPlaylist.add(f);
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
                menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
                menuMusic.start();
                isMenuMusicPlaying = true;
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
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(track);
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
        float menuFinal = baseVolume * globalFadeFactor;
        
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
