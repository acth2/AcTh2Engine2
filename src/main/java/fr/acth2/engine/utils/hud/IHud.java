package fr.acth2.engine.utils.hud;

import fr.acth2.engine.engine.models.items.Item;

public interface IHud {

    public Item[] getItems();

    default void cleanup() {
        Item[] gameItems = getItems();
        for (Item gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}