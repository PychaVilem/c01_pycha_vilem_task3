package render;


import model.Point;
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
        render(solid, true);
    }
    
    // renderuje osu bez modelovaci transformace
    public void renderAxis(Solid axis){
        render(axis, false);
    }
    
    // vnitrni metoda pro renderovani
    private void render(Solid solid, boolean useModelTransform){
        lineRasterizer.setColor(solid.getColor());
        
        // prochazeni index bufferu (ib) - kazde 2 indexy tvori usecku
        for(int i = 0; i < solid.getIndexBuffer().size(); i += 2){
            int indexA = solid.getIndexBuffer().get(i);
            int indexB = solid.getIndexBuffer().get(i + 1);

            //model space
            // ziskani vrcholu z vertex bufferu (vb) podle indexu
            Point3D a = solid.getVertexBuffer().get(indexA);
            Point3D b = solid.getVertexBuffer().get(indexB);

            // slo by to dat do jednoho radku a = a.mul(solid.getMode()).mul(view).mul(proj)

            //todo pronasobit model matici
            //model space -> world space
            // pro osy nepouzit model transformaci (pouzit identitu)
            if(useModelTransform){
                a = a.mul(solid.getModel());
                b = b.mul(solid.getModel());
            }
            // pro osy (useModelTransform == false) preskocit model transformaci

            //todo pronasobit view matici
            //world space -> view space
            a = a.mul(view);
            b = b.mul(view);

            //todo pronasobit proj matici
            //view space -> clip space
            a = a.mul(proj);
            b = b.mul(proj);

            // rychle orezani (clipping) - zjednusena verze
            // pokud je usecka zcela mimo, preskocit ji
            int codeA = computeOutCode(a);
            int codeB = computeOutCode(b);
            
            // pokud AND kodu != 0, usecka je zcela mimo
            if ((codeA & codeB) != 0) {
                continue;
            }
            
            // pokud oba body jsou uvnitr, pokracovat normalne
            // pokud je castecne viditelna, take pokracovat (nechame rasterizeru, aby to zvladl)

            //todo dehomogenizace - pozor na deleni 0
            //clip space -> ndc
            // kontrola w - musi byt kladne a dostatecne velke
            double epsilon = 1e-10; // velmi mala hodnota, ale ne tak prisna
            if(Math.abs(a.getW()) < epsilon || Math.abs(b.getW()) < epsilon){
                continue;
            }
            // pokud je w zaporne, bod je za kamerou - preskocit
            if(a.getW() < 0 || b.getW() < 0){
                continue;
            }
            
            a = a.mul(1 / a.getW());
            b = b.mul(1 / b.getW());

            // transformace z NDC (normalized device coordinates) [-1,1] do viewportu [0,width] x [0,height]
            Vec3D vecA = transformToWindow(a);
            Vec3D vecB = transformToWindow(b);

            // kontrola vyslednych souradnic pred rasterizaci - pouze filtrovat opravdu extremni hodnoty
            // povolime vetsi rozsah, aby se vykreslovaly i usecky, ktere jsou castecne mimo obrazovku
            double maxCoord = 1e6; // maximalni souradnice, kterou jeste akceptujeme
            
            // kontrola pouze na opravdu extremni hodnoty
            if(Math.abs(vecA.getX()) > maxCoord || Math.abs(vecA.getY()) > maxCoord ||
               Math.abs(vecB.getX()) > maxCoord || Math.abs(vecB.getY()) > maxCoord){
                continue; // souradnice jsou prilis velke (prakticky nekonecno), preskocit
            }

            // spojeni bodu useckou pomoci transformovanych souradnic
            lineRasterizer.rasterize(
                    (int)Math.round(vecA.getX()),
                    (int)Math.round(vecA.getY()),
                    (int)Math.round(vecB.getX()),
                    (int)Math.round(vecB.getY())
            );
        }
    }
    // metoda do ktere bude vstupovat point a vystupovat bude vektor
    private Vec3D transformToWindow(Point3D p){
        return new Vec3D(p).mul(new Vec3D(1, -1, 1))  // otoceni Y osy
                .add(new Vec3D(1, 1, 0))     // posunuti do [0,2]
                .mul(new Vec3D((double)(width - 1) / 2, (double)(height - 1) / 2, 1)); // skalovani na viewport
    }

    public void setView(Mat4 view) {
        this.view = view;
    }

    public void setProj(Mat4 proj) {
        this.proj = proj;
    }
    
    // vypocita 6-bit outcode pro bod v clip space
    // bit 0=left, 1=right, 2=bottom, 3=top, 4=near, 5=far
    private int computeOutCode(Point3D p) {
        int code = 0;
        double w = p.getW();
        
        if (p.getX() < -w) code |= 0x01; // left
        if (p.getX() > w)  code |= 0x02; // right
        if (p.getY() < -w) code |= 0x04; // bottom
        if (p.getY() > w)  code |= 0x08; // top
        if (p.getZ() < -w) code |= 0x10; // near
        if (p.getZ() > w)  code |= 0x20; // far
        
        return code;
    }
}
