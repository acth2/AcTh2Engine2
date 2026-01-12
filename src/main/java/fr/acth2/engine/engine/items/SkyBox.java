package fr.acth2.engine.engine.items;

import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.utils.loader.Loader;

public class SkyBox extends Item {

    public SkyBox(String objModel, String textureFile) throws Exception {
        super();
        Mesh skyBoxMesh = Loader.loadMesh(objModel);
        Texture skyBoxtexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture, 0.0f));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}