package core;

/**
 * Gerencia as configurações do jogo
 */
public class Settings {
    private static Settings instance;
    private int volume;
    
    // Resolução base do jogo
    public static final int GAME_WIDTH = 1000;
    public static final int GAME_HEIGHT = 700;
    
    private Settings() {
        this.volume = 50; // Volume padrão 50%
    }
    
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
    
    public int getVolume() {
        return volume;
    }
    
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        System.out.println("Volume ajustado para: " + this.volume + "%");
        SoundManager.getInstance().updateVolume();
    }
}
