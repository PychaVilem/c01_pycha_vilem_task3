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
       for(int i = 0; i< solid.getIndexBuffer().size(); i+=2){
           int indexA  = solid.getIndexBuffer().get(i);
           int indexB = solid.getIndexBuffer().get(i+1);

           Point3D a = solid.getVertexBuffer().get(indexA);
           Point3D b = solid.getVertexBuffer().get(indexB);

           //transformace do okna obrazovky
           Vec3D vecA = new Vec3D(a);
           Vec3D vecB = new Vec3D(b);

           vecA = vecA.mul(new Vec3D(1,-1,1)).add(new Vec3D(1,1,0)).mul(new Vec3D((double));//musime ukladat vysledek, u mul
           vecA = vecA.mul(new Vec3D(1,1,0));

           //spojeni bodu
           lineRasterizer.rasterize(
                   (int)Math.round( a.getX()),
                   (int)Math.round( a.getY()),
                   (int)Math.round(b.getX()),
                   (int)Math.round(b.getY())
           );
       }
        // TODO: naprogramovat
        // - mám vb a ib
        // - procházím ib, podle indexu si vezmu 2 vrcholy z vb
        // - spojím se úsečkou
    }

}
