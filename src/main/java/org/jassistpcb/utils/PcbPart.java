package org.jassistpcb.utils;

public class PcbPart {

    private String designator;
    private String comment;
    private String layer;
    private String footprint;
    private String centerX;
    private String centerY;
    private String rotation;
    private String description;

    public PcbPart(String designator, String layer, String centerX, String centerY, String rotation) {
        this.designator = designator;
        this.layer = layer;
        this.centerX = centerX;
        this.centerY = centerY;
        this.rotation = rotation;

        this.description = "";
        this.footprint = "";
        this.comment = "";
    }

    public void setDesignator(String designator) {
        this.designator = designator;
    }

    public String getDesignator() {
        return designator;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getLayer() {
        return layer;
    }

    public void setCenterX(String centerX) {
        this.centerX = centerX;
    }

    public String getCenterX() {
        return centerX;
    }

    public void setCenterY(String centerY) {
        this.centerY = centerY;
    }

    public String getCenterY() {
        return centerY;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public String getRotation() {
        return rotation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFootprint(String footprint) {
        this.footprint = footprint;
    }

    public String getFootprint() {
        return footprint;
    }

    @Override
    public String toString() {
        return "PcbPart{" +
                "designator='" + designator + '\'' +
                ", comment='" + comment + '\'' +
                ", layer='" + layer + '\'' +
                ", footprint='" + footprint + '\'' +
                ", centerX='" + centerX + '\'' +
                ", centerY='" + centerY + '\'' +
                ", rotation='" + rotation + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String[] toStringArray() {
        return new String[]{designator, comment, layer, footprint, centerX, centerY, rotation, description};
    }
}
