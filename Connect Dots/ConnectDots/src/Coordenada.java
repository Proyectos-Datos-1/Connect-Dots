import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordenada {
    @JsonProperty("startX")
    private double startX;

    @JsonProperty("startY")
    private double startY;

    @JsonProperty("endX")
    private double endX;

    @JsonProperty("endY")
    private double endY;

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }
}