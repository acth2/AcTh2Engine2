package fr.acth2.engine.engine.models;

import fr.acth2.engine.engine.Texture;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

public class Mesh {

    private final int vaoId;
    private final int posVboId;
    private int texVboId;
    private final int normalsVboId;
    private final int idxVboId;
    private final int vertexCount;
    private Vector3f colour = new Vector3f(0.0f, 0.0f, 0.0f);
    private Texture texture;

    public Mesh(float[] positions, float[] texCoords, float[] normals, int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer texBuffer = null;
        FloatBuffer normalsBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posVboId = glGenBuffers();
            posBuffer = memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            if (texCoords != null && texCoords.length > 0) {
                texVboId = glGenBuffers();
                texBuffer = memAllocFloat(texCoords.length);
                texBuffer.put(texCoords).flip();
                glBindBuffer(GL_ARRAY_BUFFER, texVboId);
                glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            }

            normalsVboId = glGenBuffers();
            normalsBuffer = memAllocFloat(normals.length);
            normalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normalsVboId);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            idxVboId = glGenBuffers();
            indicesBuffer = memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) MemoryUtil.memFree(posBuffer);
            if (texBuffer != null) MemoryUtil.memFree(texBuffer);
            if (normalsBuffer != null) MemoryUtil.memFree(normalsBuffer);
            if (indicesBuffer != null) MemoryUtil.memFree(indicesBuffer);
        }
    }

    public void attachTexture(Texture texture) {
        this.texture = texture;
    }

    public void render() {
        if (texture != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        if (isTextured()) {
            glEnableVertexAttribArray(1);
        }
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        if (isTextured()) {
            glDisableVertexAttribArray(1);
        }
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        if (isTextured()) {
            glDeleteBuffers(texVboId);
        }
        glDeleteBuffers(normalsVboId);
        glDeleteBuffers(idxVboId);

        if (texture != null) {
            texture.cleanup();
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }



    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Vector3f getColour() {
        return colour;
    }

    public boolean isTextured() {
        return this.texture != null;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }
}
