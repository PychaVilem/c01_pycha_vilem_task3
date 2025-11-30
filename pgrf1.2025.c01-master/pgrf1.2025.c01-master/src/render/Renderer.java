package render;

import rasterize.LineRasterizer;
import solids.Solid;
import transforms.Point3D;
import transforms.Vec3D;

public class Renderer {

    private LineRasterizer lineRasterizer;

    public Renderer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }
    public void render(Solid solid){
        // Získání rozměrů viewportu pro transformaci
        int width = lineRasterizer.getWidth();
        int height = lineRasterizer.getHeight();
        
        // Procházení index bufferu (ib) - každé 2 indexy tvoří úsečku
        for(int i = 0; i < solid.getIndexBuffer().size(); i += 2){
            int indexA = solid.getIndexBuffer().get(i);
            int indexB = solid.getIndexBuffer().get(i + 1);

            // Získání vrcholů z vertex bufferu (vb) podle indexů
            Point3D a = solid.getVertexBuffer().get(indexA);
            Point3D b = solid.getVertexBuffer().get(indexB);

            // Transformace z NDC (normalized device coordinates) [-1,1] do viewportu [0,width] x [0,height]
            Vec3D vecA = new Vec3D(a);
            Vec3D vecB = new Vec3D(b);

            // Transformace bodu A: otočení Y, posunutí do [0,2], škálování na viewport
            vecA = vecA.mul(new Vec3D(1, -1, 1))  // Otočení Y osy
                      .add(new Vec3D(1, 1, 0))     // Posunutí do [0,2]
                      .mul(new Vec3D(width / 2.0, height / 2.0, 1)); // Škálování na viewport

            // Transformace bodu B: stejná transformace
            vecB = vecB.mul(new Vec3D(1, -1, 1))  // Otočení Y osy
                      .add(new Vec3D(1, 1, 0))     // Posunutí do [0,2]
                      .mul(new Vec3D(width / 2.0, height / 2.0, 1)); // Škálování na viewport

            // Spojení bodů úsečkou pomocí transformovaných souřadnic
            lineRasterizer.rasterize(
                    (int)Math.round(vecA.getX()),
                    (int)Math.round(vecA.getY()),
                    (int)Math.round(vecB.getX()),
                    (int)Math.round(vecB.getY())
            );
        }
    }

}
