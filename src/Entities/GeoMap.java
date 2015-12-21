package Entities;

import Debug.TimeDebug;
import java.util.List;

/**
 * Classe permettant la gestion de toutes les données relatives à la carte non traitée
 */
public class GeoMap {
    private final double _width;
    private final double _height;
    private final RegionManager _manager;
    private final List<Boundary> _simpleBoundaries;

    // à arranger
    private static final double COEF_SIMPLIFY = 0.3;
    
    /**
     * Constructeur complet
     * @param width Largeur totale de la carte (X)
     * @param height Longueur totale de la carte (Y)
     * @param manager
     */
    public GeoMap(double width, double height, RegionManager manager) {
        this._width = width;
        this._height = height;
        this._manager = manager;
        
        TimeDebug.timeStart(1);
        // à déplacer
        this._simpleBoundaries = Geometry.getSimplifyBoundaries(manager.getBoundaries(), COEF_SIMPLIFY);
        TimeDebug.timeStop(1);
    }

    /**
     * Retourne la largeur de la carte
     * @return Largeur de la carte
     */
    public double getWidth() {
        return _width;
    }
    
    /**
     * Retourne la longueur de la carte
     * @return Longueur de la carte
     */
    public double getHeight() {
        return _height;
    }

    /**
     * Retourne la liste des régions de la carte
     * @return Liste des régions
     */
    public List<Region> getRegions() {
        return _manager.getRegions();
    }
    
    public List<Boundary> getBoundaries(){
        return _manager.getBoundaries();
    }
    
    public List<Boundary> getSimpleBoundaries(){
        return _simpleBoundaries;
    }
    
    public RegionManager debug_getManager(){
        return _manager;
    }
}
