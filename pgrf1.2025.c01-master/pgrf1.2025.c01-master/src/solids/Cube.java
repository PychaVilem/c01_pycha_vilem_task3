package solids;

import transforms.Col;
import transforms.Point3D;

public class Cube extends Solid {
    
    public Cube() {
        color = new Col(0x00ff00); // zelená barva
        
        // 8 vrcholů krychle (strana délky 1, střed v počátku)
        // Dolní plocha
        vertexBuffer.add(new Point3D(-0.5, -0.5, -0.5)); // v0
        vertexBuffer.add(new Point3D(0.5, -0.5, -0.5));  // v1
        vertexBuffer.add(new Point3D(0.5, -0.5, 0.5));   // v2
        vertexBuffer.add(new Point3D(-0.5, -0.5, 0.5)); // v3
        // Horní plocha
        vertexBuffer.add(new Point3D(-0.5, 0.5, -0.5));  // v4
        vertexBuffer.add(new Point3D(0.5, 0.5, -0.5));   // v5
        vertexBuffer.add(new Point3D(0.5, 0.5, 0.5));     // v6
        vertexBuffer.add(new Point3D(-0.5, 0.5, 0.5));   // v7
        
        // 12 hran krychle
        // Dolní plocha (4 hrany)
        addIndices(0, 1);
        addIndices(1, 2);
        addIndices(2, 3);
        addIndices(3, 0);
        
        // Horní plocha (4 hrany)
        addIndices(4, 5);
        addIndices(5, 6);
        addIndices(6, 7);
        addIndices(7, 4);
        
        // Svislé hrany (4 hrany)
        addIndices(0, 4);
        addIndices(1, 5);
        addIndices(2, 6);
        addIndices(3, 7);
    }
}
