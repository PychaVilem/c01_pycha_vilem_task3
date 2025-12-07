package solids;

import transforms.Point3D;

import java.awt.*;

public class AsiZ extends Solid {
    public AsiZ() {
        color = new Color(0, 186, 255);
        vertexBuffer.add(new Point3D(0,0,0));
        vertexBuffer.add(new Point3D(1,0,0));

        addIndisces(0,1);
    }
}
