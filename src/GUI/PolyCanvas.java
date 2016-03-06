package GUI;

import Entities.Boundary;
import Entities.GeoMap;
import Entities.Region;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.TextAlignment;

/**
 * Décrit un canvas spécialisé dans l'affichage de polygones
 */
public class PolyCanvas extends Canvas {
    private final IntegerProperty decalageX = new SimpleIntegerProperty();
    private final IntegerProperty decalageY = new SimpleIntegerProperty();
    private final DoubleProperty zoom = new SimpleDoubleProperty();
    private final BooleanProperty nomPays = new SimpleBooleanProperty();
    private  double _canvasWidth;
    private  double _canvasHeight;
    private GeoMap map;

    private double _ratio;
    private double oldX;
    private double oldY;
    /**
     * Canvas affichant la carte avec le nom de la région. Le ratio est toujours respecté
     *
     * @param map Structure Map contenant la carte à afficher
     */
    public PolyCanvas(GeoMap map) {
        super();
        this.map = map;
        setEvents();
    }

    private void setMouseReleasedEvent() {
        setOnMouseReleased(event -> {
            oldX = 0;
            oldY = 0;
        });
    }

    private void setMouseDraggedEvent() {
        setOnMouseDragged(event -> {
            int x =(int) (event.getX());
            int y =(int) (event.getY());
            if(oldX != 0) {
                decalageXProperty().setValue(getDecalageX() + x - oldX );
                decalageYProperty().setValue(getDecalageY() + y - oldY );
            }
            oldX = x;
            oldY = y;
        });
    }

    private void setEvents() {
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
        decalageXProperty().addListener(evt -> draw());
        decalageYProperty().addListener(evt -> draw());
        nomPaysProperty().addListener(evt -> draw());
        zoomProperty().addListener(evt -> draw());
        setOnScroll(event -> zoomProperty().setValue(event.getDeltaY()/200+zoomProperty().getValue()));
        setMouseDraggedEvent();
        setMouseReleasedEvent();
    }

    public void changeMap(GeoMap map) {
        this.map = map;
        initialize();
        draw();
    }

    public double getZoom() {
        return zoom.get();
    }

    public void setZoom(double zoom) {
        if(zoom>=0.5)
            this.zoom.set(zoom);
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public int getDecalageX() {
        return decalageX.get();
    }

    public void setDecalageX(int decalageX) {
        this.decalageX.set(decalageX);
    }

    public IntegerProperty decalageXProperty() {
        return decalageX;
    }

    public int getDecalageY() {
        return decalageY.get();
    }

    public void setDecalageY(int decalageY) {
         this.decalageY.set(decalageY);
    }

    public IntegerProperty decalageYProperty() {
        return decalageY;
    }

    public boolean getNomPays() {
        return nomPays.get();
    }

    public void setNomPays(boolean nomPays) {
        this.nomPays.set(nomPays);
    }

    public BooleanProperty nomPaysProperty() {
        return nomPays;
    }

    public void initialize()
    {
        setZoom(1);
        setDecalageX(0);
        setDecalageY(0);
    }

    public void draw()
    {
        _canvasWidth = getWidth();
        _canvasHeight = getHeight();
        GraphicsContext gc = super.getGraphicsContext2D();
        _ratio = resize();
        drawInitialize(gc);
        for (Boundary b : map.getSimpleBoundaries()) {
            drawPolygon(gc, b);
        }
        if(getNomPays())
            for (Region region : map.getRegions()) {
                gc.fillText(region.getName(), computeX(region.getCenter().x), computeY(region.getCenter().y));
        }
    }

    private void drawInitialize(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0,getWidth(),getHeight());
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
    }

    private void drawPolygon(GraphicsContext gc, Boundary b) {
        int size = b.getPoints().size();
        double x[] = new double[size];
        double y[] = new double[size];
        for (int i = 0; i < size; i++) {
            y[i] =computeY(b.getPoints().get(i).y);
            x[i] =computeX(b.getPoints().get(i).x);
        }

        gc.strokePolyline(x, y, size);
    }


    private double computeX(double x){
        return x*_ratio*getZoom() + getDecalageX() +(_canvasWidth / 2)*(1- getZoom());
    }
    
    private double computeY(double y){
        return -y*_ratio*getZoom() + getDecalageY() + (_canvasHeight / 2)*(getZoom()+1);
    }

    private double resize() {
        double ratioX, ratioY;
        ratioX = _canvasWidth / map.getWidth();
        ratioY = _canvasHeight / map.getHeight();
        if (ratioX < 1 && ratioY < 1)
        {
            ratioX = map.getWidth() / _canvasWidth;
            ratioY = map.getHeight() / _canvasHeight;
        }
        return ratioX < ratioY ? ratioX : ratioY;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

}
