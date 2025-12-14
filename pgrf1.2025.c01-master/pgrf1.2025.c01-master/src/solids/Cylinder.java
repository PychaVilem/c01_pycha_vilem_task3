package solids;

import transforms.Col;
import transforms.Point3D;

public class Cylinder extends Solid {
    
    public Cylinder() {
        color = new Col(0xff00ff); // magenta barva
        
        int segments = 20; // pocet segmentu kruhu
        double radius = 0.5;
        double height = 1.0;
        
        // generovani bodu pro dolni kruh
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            vertexBuffer.add(new Point3D(x, -height / 2, z));
        }
        
        // generovani bodu pro horni kruh
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            vertexBuffer.add(new Point3D(x, height / 2, z));
        }
        
        // hrany dolniho kruhu
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            addIndices(i, next);
        }
        
        // hrany horniho kruhu
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            addIndices(segments + i, segments + next);
        }
        
        // svisle hrany spojujici dolni a horni kruh
        for (int i = 0; i < segments; i++) {
            addIndices(i, segments + i);
        }
    }
}

