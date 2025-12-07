package controller;


import rasterize.LineRasterizer;
import rasterize.LineRasterizerGraphics;
import render.Renderer;
import solids.Arrow;
import solids.AsiX;
import solids.Cube;
import solids.Solid;
import transforms.*;
import view.Panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Controller3D {
    private final Panel panel;
    private LineRasterizer lineRasterizer;
    private Renderer renderer;

    // Solids
    private Solid cube;
    private Solid arrow;
    private Solid axisX;

    //Camera
    private Camera camera;
    private Mat4 model;
// TODO jaky teleso budu tranformovat a jak ho bude
    public Controller3D(Panel panel) {

        this.panel = panel;
        this.lineRasterizer = new LineRasterizerGraphics(panel.getRaster());

        camera = new Camera()
                .withPosition(new Vec3D(0.4,-1.5,1))
                .withAzimuth(Math.toRadians(90)) //doleva doprava, doleva roste , doprava klesa v RADIANECH takze pres metodu
                .withZenith(Math.toRadians(-25)) //nahoru dolu, nahoru roste
                .withFirstPerson(true);

        proj = new Mat4PerspRH(
                Math.toRadians(90),
                panel.getRaster().getHeight() / (double) panel.getRaster().getHeight(),

           // dodelat     orthodopeln = new Mat4OrthoRH();

        this.renderer = new Renderer(
                lineRasterizer,
                panel.getRaster().getWidth(),
                panel.getRaster().getHeight(),
                camera.getViewMatrix(),
                proj
                );

        cube = new Cube();
        arrow = new Arrow();
        axisX = new AsiX();


        initListeners();
        drawScene();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drawScene();
            }
        });
        panel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    arrow.setModel(new Mat4Transl(0.5,0,0));
                }
                if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                    arrow.setModel(new Mat4Transl(-0.5,0,0));
                }
                if(e.getKeyCode() == KeyEvent.VK_R) {
                    arrow.setModel(new Mat4Transl(-0.5,0,0)
                            .mul(new Mat4RotZ(Math.toRadians(15)))
                            .mul(new Mat4Transl(0.5,0,0)));

                }

                if(e.getKeyCode() == KeyEvent.VK_A) {
                    camera = camera.left(0.5);
                    renderer.setView(camera.getViewMatrix());
                }
                if(e.getKeyCode() == KeyEvent.VK_D) {
                    camera = camera.right(0.5);
                    renderer.setView(camera.getViewMatrix());
                }

                if(e.getKeyCode() == KeyEvent.VK_S) {
                    camera = camera.forward(0.5);
                    renderer.setView(camera.getViewMatrix());
                }

                if(e.getKeyCode() == KeyEvent.VK_A) {
                    camera = camera.backward(0.5);
                    renderer.setView(camera.getViewMatrix());
                }


                drawScene();
            }
        });
    }

    private void drawScene() {
        panel.getRaster().clear();

        renderer.render(arrow);

        panel.repaint();
    }
}
