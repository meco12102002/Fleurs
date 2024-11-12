package com.example.fleursonthego.Models;
import java.io.Serializable;

import java.io.Serializable;

public class BouquetImageState implements Serializable {

    private String componentType;
    private int imageResource;  // Store the image resource ID
    private float x, y, scaleX, scaleY, rotation;

    public BouquetImageState(String componentType, int imageResource, float x, float y, float scaleX, float scaleY, float rotation) {
        this.componentType = componentType;
        this.imageResource = imageResource;
        this.x = x;
        this.y = y;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.rotation = rotation;
    }

    // Getter methods
    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
