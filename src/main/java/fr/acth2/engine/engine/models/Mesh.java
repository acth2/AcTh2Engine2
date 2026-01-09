package fr.acth2.engine.engine.models;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

import fr.acth2.engine.Main;
import org.joml.Vector3f;
import org.lwjgl.openvr.Texture;
import org.lwjgl.system.MemoryUtil;

public class Mesh {

    private final int vaoId;

    private final int vboId;

    private final int vertexCount;

    private final int idxVboId;

    public Mesh(float[] positions, float[] texCoords, int[] indices) {

        vertexCount = indices.length;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // POSITIONS (location = 0)
        vboId = glGenBuffers();
        FloatBuffer posBuffer = memAllocFloat(positions.length);
        posBuffer.put(positions).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        memFree(posBuffer);

        // TEXTURE COORDS (location = 1)
        int texVboId = glGenBuffers();
        FloatBuffer texBuffer = memAllocFloat(texCoords.length);
        texBuffer.put(texCoords).flip();

        glBindBuffer(GL_ARRAY_BUFFER, texVboId);
        glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);
        memFree(texBuffer);

        // INDICES
        idxVboId = glGenBuffers();
        IntBuffer idxBuffer = memAllocInt(indices.length);
        idxBuffer.put(indices).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);
        memFree(idxBuffer);

        glBindVertexArray(0);
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glDeleteBuffers(idxVboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render() {
        glBindVertexArray(vaoId);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, Main.getInstance().textureId);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }
}