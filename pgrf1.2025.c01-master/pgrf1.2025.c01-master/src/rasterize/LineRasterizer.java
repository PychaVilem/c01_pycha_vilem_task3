package rasterize;

import model.Line;
import raster.RasterBufferedImage;
import transforms.Col;

import java.awt.*;

public abstract class LineRasterizer {
    protected RasterBufferedImage raster;
    // TODO: vyřešit barvu.
    protected Col color;

    public LineRasterizer(RasterBufferedImage raster) {
        this.raster = raster;
    }

    public void rasterize(int x1, int y1, int x2, int y2) {

    }

    public void rasterize(Line line) {
        rasterize(line.getX1(), line.getY1(), line.getX2(), line.getY2());
    }

    public int getWidth() {
        return raster.getWidth();
    }

    public int getHeight() {
        return raster.getHeight();
    }

    public setColor()
}
