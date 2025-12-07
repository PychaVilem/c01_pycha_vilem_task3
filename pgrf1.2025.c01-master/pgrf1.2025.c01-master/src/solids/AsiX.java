package solids;

import transforms.Point3D;

import java.awt.*;

public class AsiX  extends Solid {
    public AsiX() {
        color = new Color(255, 0, 0);
        vertexBuffer.add(new Point3D(0,0,0));
        vertexBuffer.add(new Point3D(1,0,0));

        addIndisces(0,1);
    }
}
