package de.rostock;

public class ObjectBounds {
    private String x, y, width, height;

    public ObjectBounds(String x, String y, String width, String height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "ObjectBounds{" +
                "x='" + x + '\'' +
                ", y='" + y + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                '}';
    }
}
