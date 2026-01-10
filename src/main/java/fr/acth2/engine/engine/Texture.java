package fr.acth2.engine.engine;

import de.matthiasmann.twl.utils.PNGDecoder;
import java.io.InputStream;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {

    private final int id;
    private final int width;
    private final int height;

    public Texture(String fileName) throws Exception {
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
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
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
