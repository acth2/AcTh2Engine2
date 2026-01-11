package fr.acth2.engine.engine;

import de.matthiasmann.twl.utils.PNGDecoder;
import java.io.InputStream;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {

    private int id;
    private int width;
    private int height;

    public Texture(ByteBuffer imageBuffer, int width, int height) {
        this.width = width;
        this.height = height;
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public Texture(String fileName) {
        try (InputStream in = Texture.class.getResourceAsStream(fileName)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + fileName);
            }

            PNGDecoder decoder = new PNGDecoder(in);
            this.width = decoder.getWidth();
            this.height = decoder.getHeight();

            ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
            decoder.decode(buf, width * 4, PNGDecoder.Format.RGBA);
            buf.flip();

            this.id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.id);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            System.err.println("Error loading texture: " + fileName);
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void cleanup() {
        glDeleteTextures(id);
    }
}
