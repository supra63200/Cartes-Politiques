package GUI;

import DataManager.Converter;
import DataManager.Save;
import Debug.TimeDebug;
import Entities.Boundary;
import Entities.HexGrid;
import Entities.GeoMap;
import Entities.Region;
import Loader.MapLoader;
import LoggerUtils.LoggerManager;
import Resolver.IResolver;
import Resolver.SimpleAggregerResolver;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.logging.Level;


public class Main extends Application {
    public static HexGrid grid;
    public static GeoMap geoMap;
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n/!\\ Attention /!\\ : ce \"main\" n'est pas fini.\nPour afficher les cartes, il faut utiliser le main du package Window.\n");
        chargement();
        Application.launch(args);
    }

    public static void chargement() throws Exception {
        TimeDebug.timeStart(0);
        // Chargement des régions en mémoire
        MapLoader ml = new MapLoader(
                "test/FRA_adm1.shp",
                "test/FRA_adm1.dbf"
                //"test/world.shp",
                //"test/world.dbf"
                //"test/usstate500k.shp",
                //"test/usstate500k.dbf"
                //"test/usstate20m.shp",
                //"test/usstate20m.dbf"
        );

        geoMap = ml.load();
        
        // Le champ par défaut correspond au nom de la colonne contenant le nom de la région dans le .dbf
        geoMap.debug_getManager().setRegionsName("NAME_1");
        //geoMap.debug_getManager().setRegionsName("name");
        //geoMap.debug_getManager().setRegionsName("NAME");

        for (Region r : geoMap.getRegions()) {
            afficherRegion(r);
        }

        IResolver algo = new SimpleAggregerResolver();
        grid = algo.resolve(geoMap.getRegions());
        
        TimeDebug.timeStop(0);
        TimeDebug.setTimeLabel(0, "Temps de chargement total de la carte");
        TimeDebug.displayTime(0);
        
        TimeDebug.setTimeLabel(1, "Simplification de la carte");
        TimeDebug.displayPourcentage(1, 0);
        
        TimeDebug.setTimeLabel(20, "Calcul des voisins");
        TimeDebug.displayPourcentage(20, 0);
    }
    
    public static void afficherRegion(Region r){
        System.out.println("<"+r.getName()+">");
        Map<Region,List<Boundary>> neighbors = r.getNeighbors();
        for(Entry<Region,List<Boundary>> e : neighbors.entrySet()){
            Region neighbor = e.getKey();
            List<Boundary> boundary = e.getValue();
            if(neighbor != null){
                System.out.println("\t"+neighbor.getName()+" : "+boundary.size());
            }
            else{
                System.out.println("\t *Vide* : "+boundary.size());
            }
        }
        
        //System.out.println("\tCentreX : "+r.getCenter().x);
        //System.out.println("\tCentreY : "+r.getCenter().y);
    }

    @Override
    public void start(Stage stage) {
        LoggerManager.getInstance().getLogger().log(Level.INFO, "Starting...");
        HBox root = new HBox();
        // Création et affichage de la grille hexagonale
        Canvas hexgrid = new HexCanvas(800, 700, grid);
        root.getChildren().add(hexgrid);
        Canvas polygrid = new PolyCanvas(geoMap);
        root.getChildren().add(polygrid);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Canvas Test");
        stage.show();
        Save.saveToImage(stage, Converter.CanvasToImage(hexgrid));
        /*try {
            Load.loadMultiple(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        
    }
}
