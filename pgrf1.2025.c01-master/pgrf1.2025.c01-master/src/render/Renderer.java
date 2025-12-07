package render;

import rasterize.LineRasterizer;
import solids.Solid;
import transforms.Mat4;
import transforms.Point3D;
import transforms.Vec3D;

public class Renderer {

    private LineRasterizer lineRasterizer;
    private int width, height;
    private Mat4 view,proj;

    public Renderer(LineRasterizer lineRasterizer, int width, int height,Mat4 view, Mat4 proj) {
        this.lineRasterizer = lineRasterizer;
        this.width = width;
        this.height = height;
        this.view = view;
        this.proj = proj;
    }

    public void render(Solid solid){
        lineRasterizer.setColor(solid.getColor());
        
        // Procházení index bufferu (ib) - každé 2 indexy tvoří úsečku
        for(int i = 0; i < solid.getIndexBuffer().size(); i += 2){
            int indexA = solid.getIndexBuffer().get(i);
            int indexB = solid.getIndexBuffer().get(i + 1);

            //model space
            // Získání vrcholů z vertex bufferu (vb) podle indexů
            Point3D a = solid.getVertexBuffer().get(indexA);
            Point3D b = solid.getVertexBuffer().get(indexB);

            // slo by to dat do jednoho radku a = a.mul(solid.getMode()).mul(view).mul(proj)

            //todo pronasobit model matici
            //model space -> world space
            a = a.mul(solid.getModel());
            b= b.mul(solid.getModel());

            //todo pronasobit view matici
            //world space -> view space
            a = a.mul(view);
            b = b.mul(view);

            //todo pronasobit proj matici
            //view space -> clip space
            a = a.mul(proj);
            b = b.mul(proj);

            //todo orezani

            //todo dehomogenizace - pozor na deleni 0
            //clip space -> ndc
            if(a.getW() == 0 || b.getW() == 0){
                continue;
            }
            a = a.mul(1 / a.getW());
            b = b.mul(1 / b.getW());




            // Transformace z NDC (normalized device coordinates) [-1,1] do viewportu [0,width] x [0,height]
            Vec3D vecA = transformToWindow(a);
            Vec3D vecB = transformToWindow(b);



            // Spojení bodů úsečkou pomocí transformovaných souřadnic
            lineRasterizer.rasterize(
                    (int)Math.round(vecA.getX()),
                    (int)Math.round(vecA.getY()),
                    (int)Math.round(vecB.getX()),
                    (int)Math.round(vecB.getY())
            );
        }
    }
    //metoda do ktere bude vstupovat point a vystupovat bude vektor
    private Vec3D transformToWindow(Point3D p){
        return new Vec3D(p).mul(new Vec3D(1, -1, 1))  // Otočení Y osy
                .add(new Vec3D(1, 1, 0))     // Posunutí do [0,2]
                .mul(new Vec3D((double)(width - 1) / 2, (double)(height - 1) / 2, 1)); // Škálování na viewport
    }

    public void setView(Mat4 view) {
        this.view = view;
    }

    public void setProj(Mat4 proj) {
        this.proj = proj;
    }
}
