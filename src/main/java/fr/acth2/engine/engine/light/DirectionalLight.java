package fr.acth2.engine.engine.light;

import org.joml.Vector3f;

public class DirectionalLight {

    private Vector3f color;
    private Vector3f position;
    private float intensity;

    public DirectionalLight(Vector3f color, Vector3f position, float intensity) {
        this.color = color;
        this.position = position;
        this.intensity = intensity;
    }

    public DirectionalLight(DirectionalLight light) {
        this(new Vector3f(light.getColor()), new Vector3f(light.getPosition()), light.getIntensity());
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
