package solids;

import transforms.Point3D;

import java.awt.*;

public class AsiY extends Solid {
    public AsiY() {
        color = new Color(11, 255, 3);
        vertexBuffer.add(new Point3D(0,0,0));
        vertexBuffer.add(new Point3D(0,1,0));

        addIndisces(0,1);
    }
}
