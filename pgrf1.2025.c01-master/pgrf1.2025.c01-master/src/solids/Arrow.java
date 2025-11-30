package solids;

import transforms.Point3D;

public class Arrow extends Solid {

    public Arrow() {

        vertexBuffer.add(new Point3D(-0.5,0,1)); //0
        vertexBuffer.add(new Point3D(0.5,0,1));//1
        vertexBuffer.add(new Point3D(0.5,-0.1,1));//2
        vertexBuffer.add(new Point3D(0.6,0,1)); //3
        vertexBuffer.add(new Point3D(0.5,0.1,1));//4

        addIndisces(0,1);
        addIndisces(1,2);
        addIndisces(2,3);
        addIndisces(3,4);
        addIndisces(4,2);
    }
}
