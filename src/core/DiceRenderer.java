package core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DiceRenderer {
    private static DiceRenderer instance;
    private BufferedImage spriteSheet;
    private BufferedImage[] faces;
    private int faceWidth;
    private int faceHeight;

    private DiceRenderer() {
        loadSpriteSheet();
    }

    public static DiceRenderer getInstance() {
        if (instance == null) {
            instance = new DiceRenderer();
        }
        return instance;
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/dados.png"));
            if (spriteSheet != null) {
                int w = spriteSheet.getWidth();
                int h = spriteSheet.getHeight();
                faces = new BufferedImage[6];
                
                if (w > h * 4) {
                    faceWidth = w / 6;
                    faceHeight = h;
                    for (int i = 0; i < 6; i++) {
                        faces[i] = spriteSheet.getSubimage(i * faceWidth, 0, faceWidth, faceHeight);
                    }
                } else if (h > w * 4) {
                    faceWidth = w;
                    faceHeight = h / 6;
                    for (int i = 0; i < 6; i++) {
                        faces[i] = spriteSheet.getSubimage(0, i * faceHeight, faceWidth, faceHeight);
                    }
                } else if (w > h) {
                    faceWidth = w / 3;
                    faceHeight = h / 2;
                    for (int i = 0; i < 6; i++) {
                        int col = i % 3;
                        int row = i / 3;
                        faces[i] = spriteSheet.getSubimage(col * faceWidth, row * faceHeight, faceWidth, faceHeight);
                    }
                } else {
                    faceWidth = w / 2;
                    faceHeight = h / 3;
                    for (int i = 0; i < 6; i++) {
                        int col = i % 2;
                        int row = i / 2;
                        faces[i] = spriteSheet.getSubimage(col * faceWidth, row * faceHeight, faceWidth, faceHeight);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar sprite sheet do dado: " + e.getMessage());
        }
    }

    public void drawFace(Graphics2D g, int faceValue, int x, int y, int width, int height) {
        if (faces != null && faceValue >= 1 && faceValue <= 6) {
            g.drawImage(faces[faceValue - 1], x, y, width, height, null);
        } else {
            // Fallback drawing if image fails
            g.setColor(Color.WHITE);
            g.fillRoundRect(x, y, width, height, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, width, height, 20, 20);
            g.drawString(String.valueOf(faceValue), x + width/2 - 5, y + height/2 + 5);
        }
    }
}
