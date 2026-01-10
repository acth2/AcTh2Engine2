package fr.acth2.engine.engine.models;


import fr.acth2.engine.Main;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
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

    private boolean textured;

    public Mesh(float[] positions, float[] texCoords, float[] normals, int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer texBuffer = null;
        FloatBuffer normalsBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            this.textured = texCoords != null && texCoords.length > 0;
            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            posVboId = glGenBuffers();
            posBuffer = memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Texture coordinates VBO
            if (textured) {
                texVboId = glGenBuffers();
                texBuffer = memAllocFloat(texCoords.length);
                texBuffer.put(texCoords).flip();
                glBindBuffer(GL_ARRAY_BUFFER, texVboId);
                glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            }

            // Normals VBO
            normalsVboId = glGenBuffers();
            normalsBuffer = memAllocFloat(normals.length);
            normalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normalsVboId);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // Index VBO
            idxVboId = glGenBuffers();
            indicesBuffer = memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (texBuffer != null) {
                MemoryUtil.memFree(texBuffer);
            }
            if (normalsBuffer != null) {
                MemoryUtil.memFree(normalsBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        if (textured) {
            glDeleteBuffers(texVboId);
        }
        glDeleteBuffers(normalsVboId);
        glDeleteBuffers(idxVboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        if (textured) {
            glEnableVertexAttribArray(1);
        }
        glEnableVertexAttribArray(2);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, Main.getInstance().textureId);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        if (textured) {
            glDisableVertexAttribArray(1);
        }
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public Vector3f getColour() {
        return colour;
    }

    public boolean isTextured() {
        return textured;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
        this.textured = false;
    }

    public void setTextured(boolean textured) {
        this.textured = textured;
    }
}
