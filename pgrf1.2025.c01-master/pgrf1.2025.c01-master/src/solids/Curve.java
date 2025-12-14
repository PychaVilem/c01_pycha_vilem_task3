package solids;

import transforms.Col;
import transforms.Cubic;
import transforms.Mat4;
import transforms.Point3D;

public class Curve extends Solid {
    
    public enum CubicType {
        BEZIER, COONS, FERGUSON
    }
    
    private Point3D[] controlPoints;
    private CubicType currentType = CubicType.BEZIER;
    private static final int SEGMENTS = 100;
    
    public Curve() {
        color = new Col(0x00ffff); // cyan color
        
        // ulozit kontrolni body
        controlPoints = new Point3D[]{
            new Point3D(0, 0, 0),
            new Point3D(0.25, 0, 0.5),
            new Point3D(0.75, 0, 2.75),
            new Point3D(1, 0, 0)
        };
        
        // vygenerovat krivku s vychozim typem (Bezier)
        regenerateCurve();
    }
    
    // zmeni typ kubiky a prepocita vertex buffer
    public void setCubicType(CubicType type) {
        if (this.currentType != type) {
            this.currentType = type;
            regenerateCurve();
        }
    }
    
    // vrati aktualni typ kubiky
    public CubicType getCubicType() {
        return currentType;
    }
    
    // prepocita vertex buffer podle aktualniho typu kubiky
    private void regenerateCurve() {
        // vycistit stare vrcholy a indexy
        vertexBuffer.clear();
        indexBuffer.clear();
        
        // vybrat spravnou base matici podle typu
        Mat4 baseMat;
        switch (currentType) {
            case BEZIER:
                baseMat = Cubic.BEZIER;
                break;
            case COONS:
                baseMat = Cubic.COONS;
                break;
            case FERGUSON:
                baseMat = Cubic.FERGUSON;
                break;
            default:
                baseMat = Cubic.BEZIER;
        }
        
        // vytvorit kubiku s aktualnimi kontrolnimi body
        Cubic cubic = new Cubic(
                baseMat,
                controlPoints[0],
                controlPoints[1],
                controlPoints[2],
                controlPoints[3]
        );
        
        // generovat body krivky
        for (int i = 0; i < SEGMENTS; i++) {
            float step = i / (float) SEGMENTS;
            vertexBuffer.add(cubic.compute(step));
        }
        
        // vytvorit index buffer (spojit body useckami)
        for (int i = 0; i < vertexBuffer.size() - 1; i++) {
            indexBuffer.add(i);
            indexBuffer.add(i + 1);
        }
    }
}
