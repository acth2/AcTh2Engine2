package fr.acth2.engine.engine;

import de.matthiasmann.twl.utils.PNGDecoder;
import java.io.InputStream;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
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

    public Texture(String[] textureFiles) throws Exception {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);

        for (int i = 0; i < textureFiles.length; i++) {
            try (InputStream in = Texture.class.getResourceAsStream(textureFiles[i])) {
                if (in == null) {
                    throw new RuntimeException("Resource not found: " + textureFiles[i]);
                }
                PNGDecoder decoder = new PNGDecoder(in);
                ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
                buf.flip();
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            }
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
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
