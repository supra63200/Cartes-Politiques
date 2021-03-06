package Entities;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Théophile
 */
@SuppressWarnings("JavaDoc")
class Geometry {

    // à arranger (utiliser l'aire)
    static public RawPolygon getMainPolygon(List<RawPolygon> polygons) {
        RawPolygon larger = null;
        int maxPoints = -1;
        for (RawPolygon p : polygons) {
            int nbPoints = p.getPoints().size();
            if (nbPoints > maxPoints) {
                maxPoints = nbPoints;
                larger = p;
            }
        }
        return larger;
    }

    /**
     * Récupère le centre de masse d'un RawPolygon
     *
     * @param polygon Polygone
     * @return Centre de masse du polygone
     */
    static public Point getCentreDeMasse(RawPolygon polygon) {
        double somme_X = 0, somme_Y = 0, somme_aire = 0;
        Point pt0;
        Point pt1 = null;
        Point debut = null;
        for (Point point : polygon.getPoints()) {
            pt0 = pt1;
            pt1 = point;
            if (pt0 != null) {
                somme_X += calculSommeX(pt0, pt1);
                somme_Y += calculSommeY(pt0, pt1);
                somme_aire += calculMembre2(pt0, pt1);
            } else {
                debut = pt1;
            }
        }
        pt0 = pt1;
        pt1 = debut;
        if (pt1 != null) {
            somme_X += calculSommeX(pt0, pt1);
            somme_Y += calculSommeY(pt0, pt1);
            somme_aire += calculMembre2(pt0, pt1);
        }

        double aire = 0.5 * somme_aire;
        double xG = somme_X / (6.0 * aire);
        double yG = somme_Y / (6.0 * aire);

        return new Point(xG, yG);
    }

    static private double calculSommeX(Point pt0, Point pt1) {
        return (pt0.x + pt1.x) * calculMembre2(pt0, pt1);
    }

    static private double calculSommeY(Point pt0, Point pt1) {
        return (pt0.y + pt1.y) * calculMembre2(pt0, pt1);
    }

    static private double calculMembre2(Point pt0, Point pt1) {
        return pt1.x * pt0.y - pt0.x * pt1.y;
    }

    /**
     * Calcule la distance entre deux points
     *
     * @param pointA Point A
     * @param pointB Point B
     * @return Distance entre le point A et le point B
     */
    public static double distanceBetween2Points(Point pointA, Point pointB) {
        return Math.sqrt((Math.pow(pointB.x - pointA.x, 2) + Math.pow(pointB.y - pointA.y, 2)));
    }

    /**
     * Calcule la distance entre deux points contenus dans une frontière en suivant celle-ci
     *
     * @param boundary   Frontière contenant les deux points
     * @param firstIndex Index du premier point dans la frontière
     * @param lastIndex  Index du dernier point dans la frontière
     * @return Distance entre deux points suivant la frontière
     */
    private static double distanceBetween2PointsAlongBoundary(Boundary boundary, int firstIndex, int lastIndex) {
        if (firstIndex > lastIndex) {
            int tmp = lastIndex;
            lastIndex = firstIndex;
            firstIndex = tmp;
        }

        double somme_dist = 0;
        Point ptA;
        Point ptB = boundary.getPoints().get(firstIndex);
        for (int i = firstIndex + 1; i <= lastIndex; i++) {
            ptA = ptB;
            ptB = boundary.getPoints().get(i);
            somme_dist += Geometry.distanceBetween2Points(ptA, ptB);
        }
        return somme_dist;
    }

    /**
     * Angle du point "pt" à partir du point "reference".
     * Ex 1 : Si "pt" est au-dessus de "reference", l'angle sera 0°
     * Ex 2 : Si "pt" est à droite de "reference", l'angle sera 90°
     *
     * @param reference Point de référence
     * @param pt        Point de
     * @return Angle entre reference et pt
     */
    static public double angleBetween2Points(Point reference, Point pt) {
        double vecteurX = pt.x - reference.x;
        double vecteurY = pt.y - reference.y;
        return (Math.atan2(vecteurY, vecteurX) * -(180 / Math.PI) + 450) % 360;
    }

    /**
     * Simplifie une liste de frontières.
     * Les frontières simplifiés contiennent moins de points.
     *
     * @param boundaries
     * @param coef       TODO : à arranger
     * @return Frontières simplifiées
     */
    public static List<Boundary> getSimplifyBoundaries(List<Boundary> boundaries, double coef) {
        return boundaries.stream().map(b -> simplifyBoundary(b, coef)).collect(Collectors.toList());
    }

    private static Boundary simplifyBoundary(Boundary b, double coef) {
        TreeMap<Integer, Point> simplifiedPoints = simplify(b, 0, b.getPoints().size() - 1, coef);
        List<Point> points = simplifiedPoints.values().stream().collect(Collectors.toList());
        return new Boundary(points);
    }

    private static TreeMap<Integer, Point> simplify(Boundary boundary, int firstIndex, int lastIndex, double coef) {
        if (firstIndex + 1 == lastIndex || Geometry.distanceBetween2PointsAlongBoundary(boundary, firstIndex, lastIndex) < coef) {
            TreeMap<Integer, Point> includedPoint = new TreeMap<>();
            includedPoint.put(firstIndex, boundary.getPoints().get(firstIndex));
            includedPoint.put(lastIndex, boundary.getPoints().get(lastIndex));
            return includedPoint;
        }

        int indexFurthestPoint = indexOfTheFurthestPoint(boundary, firstIndex, lastIndex);
        TreeMap<Integer, Point> firstPart = simplify(boundary, firstIndex, indexFurthestPoint, coef);
        TreeMap<Integer, Point> secondPart = simplify(boundary, indexFurthestPoint, lastIndex, coef);

        return mergeTreeMap(firstPart, secondPart);
    }

    private static TreeMap<Integer, Point> mergeTreeMap(TreeMap<Integer, Point> treeA, TreeMap<Integer, Point> treeB) {
        for (Entry<Integer, Point> entry : treeB.entrySet()) {
            int index = entry.getKey();
            Point pt = entry.getValue();
            if (!treeA.containsKey(index)) {
                treeA.put(index, pt);
            }
        }
        return treeA;
    }

    private static int indexOfTheFurthestPoint(Boundary boundary, int firstIndex, int lastIndex) {
        Point firstPoint = boundary.getPoints().get(firstIndex);
        Point lastPoint = boundary.getPoints().get(lastIndex);
        double longueurBase = Geometry.distanceBetween2Points(firstPoint, lastPoint);

        int indexFurthestPoint = -1;
        double hauteurTriangleFurthestPoint = -1;

        Point pointTmp;
        double distA, distB, aire, hauteur;

        for (int i = firstIndex + 1; i < lastIndex; i++) {
            pointTmp = boundary.getPoints().get(i);
            distA = Geometry.distanceBetween2Points(firstPoint, pointTmp);
            distB = Geometry.distanceBetween2Points(pointTmp, lastPoint);
            aire = longueurBase + distA + distB;
            hauteur = hauteurTriangle(longueurBase, aire);
            if (hauteur > hauteurTriangleFurthestPoint) {
                hauteurTriangleFurthestPoint = hauteur;
                indexFurthestPoint = i;
            }
        }
        return indexFurthestPoint;
    }

    private static double hauteurTriangle(double longueurBase, double aire) {
        return (2 * aire) / longueurBase;
    }

}
