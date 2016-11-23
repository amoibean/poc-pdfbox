package com.pasi.pdfbox.bean;

/**
 * Represent an (x, y) coordinate.
 *
 * Created by bean on 11/23/16.
 */
public class XYLocation {
    private float x;
    private float y;

    public XYLocation() {
        //
    }

    public XYLocation(float x, float y) {
        this.x = x;
        this.y = y;
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
}
