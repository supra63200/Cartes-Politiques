package Loader;

import CustomException.InvalidMapException;
import Entities.Point;
import Entities.RawPolygon;
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.AbstractPolyShape;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Classe permettant la lecture d'un fichier shapefile pour en recuperer les informations globales et les polygones representant chaque pays
 */
class ShapeStreamReader {
    private static final double NO_DATA = -1e38;
    private ShapeFileReader _reader;

    /**
     * Constructeur de la classe
     *
     * @param stream                Flux du fichier shapefile a lire
     * @param validationPreferences Preferences de lecture du fichier shapefile
     * @throws InvalidShapeFileException Le flux n'est pas un fichier shapefile valide
     * @throws IOException               Erreur de lecture du flux de donnees
     * @throws InvalidMapException       Le format de la carte n'est pas reconnu
     * @throws NullPointerException      Le flux de donnees est invalide ou absent
     */
    public ShapeStreamReader(FileInputStream stream, ValidationPreferences validationPreferences) throws InvalidShapeFileException, IOException, InvalidMapException, NullPointerException {
        if (stream != null) {
            if (validationPreferences == null) {
                createShapeFileReader(stream);
            } else {
                createShapeFileReader(stream, validationPreferences);
            }
        } else {
            _reader = null;
            throw new NullPointerException("Le flux de données est absent");
        }
    }

    private void createShapeFileReader(FileInputStream stream) throws InvalidShapeFileException, IOException, InvalidMapException {
        try {
            _reader = new ShapeFileReader(stream);
        } catch (InvalidShapeFileException e) {
            throw new InvalidShapeFileException("La forme est invalide", e.getCause());
        } catch (IOException e) {
            throw new IOException("Une erreur de lecture est survenue", e.getCause());
        }
        isMapValid(_reader);
    }

    private void createShapeFileReader(FileInputStream stream, ValidationPreferences validationPreferences) throws InvalidShapeFileException, IOException, InvalidMapException {
        try {
            _reader = new ShapeFileReader(stream, validationPreferences);
        } catch (InvalidShapeFileException e) {
            throw new InvalidShapeFileException("La forme est invalide", e.getCause());
        } catch (IOException e) {
            throw new IOException("Une erreur de lecture est survenue", e.getCause());
        }
        isMapValid(_reader);
    }

    private void isMapValid(ShapeFileReader reader) throws InvalidMapException {
        if ((reader.getHeader().getBoxMaxZ() != 0 && reader.getHeader().getBoxMaxZ() != NO_DATA) || (reader.getHeader().getBoxMinZ() != 0 && reader.getHeader().getBoxMinZ() != NO_DATA) || (reader.getHeader().getBoxMaxM() != 0 && reader.getHeader().getBoxMaxM() != NO_DATA) || (reader.getHeader().getBoxMinM() != 0 && reader.getHeader().getBoxMinM() != NO_DATA)) {
            throw new InvalidMapException("Seules les cartes 2D sont gérées");
        }
        if (reader.getHeader().getShapeType() != ShapeType.POLYGON && reader.getHeader().getShapeType() != ShapeType.POLYGON_Z && reader.getHeader().getShapeType() != ShapeType.POLYGON_M) {
            throw new InvalidMapException("Seuls les polygones sont gérés pour les régions");
        }
    }


    /**
     * Retourne la taille en X de la carte
     *
     * @return Taille en X
     */
    public double getMapSizeX() {
        return (Math.abs(_reader.getHeader().getBoxMaxX()) - _reader.getHeader().getBoxMinX());
    }

    /**
     * Retourne la taille en Y de la carte
     *
     * @return Taille en Y
     */
    public double getMapSizeY() {
        return (Math.abs(_reader.getHeader().getBoxMaxY()) - _reader.getHeader().getBoxMinY());
    }

    /**
     * Retourne la valeur minimale de X
     *
     * @return Minimum de X
     */
    private double getMapMinX() {
        return _reader.getHeader().getBoxMinX();
    }

    /**
     * Retourne la valeur minimale de Y
     *
     * @return Minimum de Y
     */
    private double getMapMinY() {
        return _reader.getHeader().getBoxMinY();
    }

    /**
     * Retourne la prochaine forme contenue dans le flux de données
     *
     * @return Polygone JavaFX contenant les frontières de la région
     * @throws IOException
     * @throws InvalidShapeFileException
     */
    public List<RawPolygon> getNextShape() throws IOException, InvalidShapeFileException {
        AbstractShape shape = _reader.next();
        List<RawPolygon> polygons = new ArrayList<>();
        if (shape != null) {
            AbstractPolyShape polygonShape = (AbstractPolyShape) shape;
            for (int i = 0; i < polygonShape.getNumberOfParts(); i++) {
                polygons.add(addPointDataArrayToPolygon(polygonShape.getPointsOfPart(i)));
            }
        }
        return polygons;
    }

    private RawPolygon addPointDataArrayToPolygon(PointData[] points) {
        RawPolygon polygon = new RawPolygon();
        for (PointData point : points) {
            polygon.getPoints().add(new Point(point.getX() - getMapMinX(), point.getY() - getMapMinY()));
        }
        return polygon;
    }
}
