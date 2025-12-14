package solids;

import transforms.Bicubic;
import transforms.Col;
import transforms.Cubic;
import transforms.Point3D;

public class Surface extends Solid {
    
    public Surface() {
        color = new Col(0xffff00); // zluta barva
        
        // vytvoreni 4x4 kontrolnich bodu pro bicubic plochu
        Point3D p11 = new Point3D(0, 0, 0);
        Point3D p12 = new Point3D(0.33, 0, 0.5);
        Point3D p13 = new Point3D(0.66, 0, 0.5);
        Point3D p14 = new Point3D(1, 0, 0);
        
        Point3D p21 = new Point3D(0, 0.33, 0.5);
        Point3D p22 = new Point3D(0.33, 0.33, 1);
        Point3D p23 = new Point3D(0.66, 0.33, 1);
        Point3D p24 = new Point3D(1, 0.33, 0.5);
        
        Point3D p31 = new Point3D(0, 0.66, 0.5);
        Point3D p32 = new Point3D(0.33, 0.66, 1);
        Point3D p33 = new Point3D(0.66, 0.66, 1);
        Point3D p34 = new Point3D(1, 0.66, 0.5);
        
        Point3D p41 = new Point3D(0, 1, 0);
        Point3D p42 = new Point3D(0.33, 1, 0.5);
        Point3D p43 = new Point3D(0.66, 1, 0.5);
        Point3D p44 = new Point3D(1, 1, 0);
        
        // vytvoreni bicubic plochy pomoci Bezier
        Bicubic bicubic = new Bicubic(
                Cubic.BEZIER,
                p11, p12, p13, p14,
                p21, p22, p23, p24,
                p31, p32, p33, p34,
                p41, p42, p43, p44
        );
        
        // generovani bodu plochy (grid)
        int n = 20; // pocet segmentu v kazdem smeru
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                double u = i / (double) n;
                double v = j / (double) n;
                vertexBuffer.add(bicubic.compute(u, v));
            }
        }
        
        // vytvoreni gridu usecek
        // horizontalni usecky (konstantni v)
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j < n; j++) {
                int index = i * (n + 1) + j;
                addIndices(index, index + 1);
            }
        }
        
        // vertikalni usecky (konstantni u)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= n; j++) {
                int index = i * (n + 1) + j;
                addIndices(index, index + (n + 1));
            }
        }
    }
}

