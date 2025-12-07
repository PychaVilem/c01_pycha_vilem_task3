package solids;

import transforms.Col;
import transforms.Point3D;

public class Arrow extends Solid {

    public Arrow() {
    color = new Col( 0xffff00);
        vertexBuffer.add(new Point3D(-0.5,0,1)); //0
        vertexBuffer.add(new Point3D(0.5,0,1));//1
        vertexBuffer.add(new Point3D(0.5,-0.1,0));//2
        vertexBuffer.add(new Point3D(0.6,0,0)); //3
        vertexBuffer.add(new Point3D(0.5,0.1,0));//4

        addIndisces(0,1);
        addIndisces(1,2);
        addIndisces(2,3);
        addIndisces(3,4);
        addIndisces(4,2);
    }
}
