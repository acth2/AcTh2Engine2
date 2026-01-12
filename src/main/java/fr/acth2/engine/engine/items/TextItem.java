package fr.acth2.engine.engine.items;

import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

public class TextItem extends Item {

    private String text;
    private Font font;
    private int height;

    public TextItem(String text) {
        this(text, new Font("Arial", Font.PLAIN, 20));
    }

    public TextItem(String text, String fontName, int style, int size) {
        this(text, new Font(fontName, style, size));
    }

    public TextItem(String text, Font font) {
        super();
        this.text = text;
        this.font = font;
        this.setMesh(buildMesh());
    }

    private Mesh buildMesh() {
        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dummyImage.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 5;
        this.height = fm.getHeight();
        g2d.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        Texture texture = new Texture(buffer, width, height);
        Material material = new Material();
        material.attachTexture(texture);

        float[] positions = new float[]{
                0, 0, 0,
                0, (float)height, 0,
                (float)width, (float)height, 0,
                (float)width, 0, 0
        };
        float[] texCoords = new float[]{
                0, 0,
                0, 1,
                1, 1,
                1, 0
        };
        int[] indices = new int[]{0, 1, 2, 0, 2, 3};
        float[] normals = new float[0];

        Mesh mesh = new Mesh(positions, texCoords, normals, indices);
        mesh.setMaterial(material);
        return mesh;
    }

    public String getText() {
        return text;
    }
    
    public int getHeight() {
        return height;
    }

    public void setText(String text) {
        this.text = text;
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh());
    }
}
