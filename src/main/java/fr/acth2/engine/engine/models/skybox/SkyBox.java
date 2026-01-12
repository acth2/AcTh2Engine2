package fr.acth2.engine.engine.models.skybox;

import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.items.Item;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;

public class SkyBox extends Item {

    public SkyBox(String[] textureFiles) throws Exception {
        super();
        Mesh skyBoxMesh = buildSkyBoxMesh();
        Texture skyBoxtexture = new Texture(textureFiles);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture, 0.0f));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }

    private Mesh buildSkyBoxMesh() {
        float[] positions = {
            // V0
            -1,  1, -1,
            // V1
            -1, -1, -1,
            // V2
             1, -1, -1,
            // V3
             1,  1, -1,
            // V4
            -1,  1,  1,
            // V5
             1,  1,  1,
            // V6
            -1, -1,  1,
            // V7
             1, -1,  1,
        };
        int[] indices = {
            // Front
            0, 1, 3, 3, 1, 2,
            // Top
            4, 0, 5, 5, 0, 3,
            // Right
            3, 2, 5, 5, 2, 7,
            // Left
            4, 6, 0, 0, 6, 1,
            // Bottom
            1, 6, 2, 2, 6, 7,
            // Back
            4, 5, 6, 6, 5, 7
        };
        return new Mesh(positions, null, null, indices);
    }
}
