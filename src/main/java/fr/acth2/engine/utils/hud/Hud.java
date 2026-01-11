package fr.acth2.engine.utils.hud;

import fr.acth2.engine.engine.models.items.Item;
import fr.acth2.engine.engine.models.items.TextItem;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.awt.Font;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class Hud implements IHud {

    private final List<TemporaryText> temporaryTexts;
    private final Map<String, TextItem> persistentTexts;
    private long windowId;

    private static class TemporaryText {
        TextItem textItem;
        long deathTime;

        TemporaryText(TextItem textItem, long deathTime) {
            this.textItem = textItem;
            this.deathTime = deathTime;
        }
    }

    public Hud() {
        this.temporaryTexts = new ArrayList<>();
        this.persistentTexts = new HashMap<>();
    }

    public void showInformation(String text, int duration) {
        Font font = new Font("Arial", Font.PLAIN, 20);
        TextItem textItem = new TextItem(text, font);
        textItem.getMesh().getMaterial().setColour(new Vector4f(1, 1, 1, 1));
        long deathTime = System.currentTimeMillis() + duration;
        temporaryTexts.add(new TemporaryText(textItem, deathTime));
        updateTextPositions();
    }

    public void showError(String text, int duration) {
        Font font = new Font("Arial", Font.PLAIN, 20);
        TextItem textItem = new TextItem(text, font);
        textItem.getMesh().getMaterial().setColour(new Vector4f(1, 0, 0, 1));
        long deathTime = System.currentTimeMillis() + duration;
        temporaryTexts.add(new TemporaryText(textItem, deathTime));
        updateTextPositions();
    }

    public void setPersistentText(String key, String text) {
        TextItem textItem = persistentTexts.get(key);
        if (textItem == null) {
            Font font = new Font("Arial", Font.PLAIN, 20);
            textItem = new TextItem(text, font);
            textItem.getMesh().getMaterial().setColour(new Vector4f(1, 1, 1, 1));
            persistentTexts.put(key, textItem);
        } else {
            textItem.setText(text);
        }
        updateTextPositions();
    }

    public void removePersistentText(String key) {
        TextItem textItem = persistentTexts.remove(key);
        if (textItem != null) {
            textItem.getMesh().cleanUp();
            updateTextPositions();
        }
    }

    public void update() {
        long now = System.currentTimeMillis();
        Iterator<TemporaryText> iterator = temporaryTexts.iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            TemporaryText tempText = iterator.next();
            if (now >= tempText.deathTime) {
                tempText.textItem.getMesh().cleanUp();
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            updateTextPositions();
        }
    }

    @Override
    public Item[] getItems() {
        List<Item> items = new ArrayList<>();
        for (TemporaryText tempText : temporaryTexts) {
            items.add(tempText.textItem);
        }
        items.addAll(persistentTexts.values());
        return items.toArray(new Item[0]);
    }

    public void updateSize(long windowId) {
        this.windowId = windowId;
        updateTextPositions();
    }
    
    public void cleanUp() {
        for (TemporaryText tempText : temporaryTexts) {
            tempText.textItem.getMesh().cleanUp();
        }
        for (TextItem textItem : persistentTexts.values()) {
            textItem.getMesh().cleanUp();
        }
    }

    private void updateTextPositions() {
        if (windowId == 0) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(windowId, width, height);

            int i = 0;
            for (TemporaryText tempText : temporaryTexts) {
                tempText.textItem.setPosition(10f, height.get(0) - 50f - (i * 20f), 0);
                i++;
            }
            for (TextItem textItem : persistentTexts.values()) {
                textItem.setPosition(10f, 10f + (i * 20f), 0);
                i++;
            }
        }
    }
}
