package controller;


import rasterize.LineRasterizer;
import rasterize.LineRasterizerGraphics;
import render.Renderer;
import solids.*;
import transforms.*;
import view.Panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

public class Controller3D {
    private final Panel panel;
    private LineRasterizer lineRasterizer;
    private Renderer renderer;

    // Solids
    private Solid cube;
    private Solid arrow;
    private Solid axisX;
    private Solid axisY;
    private Solid axisZ;
    private Solid curve;
    private Solid cylinder;
    private Solid surface;
    
    // vyber aktivniho telesa
    private Solid[] solids; // pole vsech transformovatelnych teles
    private int activeSolidIndex = 0; // index aktivniho telesa
    private Col[] originalColors; // puvodni barvy teles
    private static final Col SELECTED_COLOR = new Col(0xffffff); // bila barva pro vybrane teleso

    //Camera
    private Camera camera;
    private Mat4 proj;
    private boolean isPerspective = true; // true = perspektivni, false = pravouhla
    
    // vychozi hodnoty kamery pro reset
    private static final Vec3D DEFAULT_CAMERA_POSITION = new Vec3D(0.4, -1.5, 1);
    private static final double DEFAULT_AZIMUTH = Math.toRadians(90);
    private static final double DEFAULT_ZENITH = Math.toRadians(-25);
    
    // mouse look
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    private boolean isRightMousePressed = false;
    private static final double MOUSE_SENSITIVITY = 0.005; // citlivost rozhlizeni
    
    // animace
    private Timer animationTimer;
    private boolean isAnimating = false;
    private long animationStartTime;
    private static final long ANIMATION_DURATION = 3000; // 3 sekundy v milisekundach
    private Mat4 animationStartModel;
    private Solid animatingSolid;

    public Controller3D(Panel panel) {

        this.panel = panel;
        this.lineRasterizer = new LineRasterizerGraphics(panel.getRaster());

        camera = new Camera()
                .withPosition(new Vec3D(0.4,-1.5,1))
                .withAzimuth(Math.toRadians(90)) //doleva doprava, doleva roste, doprava klesa v RADIANECH
                .withZenith(Math.toRadians(-25)) //nahoru dolu, nahoru roste
                .withFirstPerson(true);

        proj = new Mat4PerspRH(
                Math.toRadians(90),
                panel.getRaster().getHeight() / (double) panel.getRaster().getWidth(),
                0.1,
                100);


        this.renderer = new Renderer(
                lineRasterizer,
                panel.getRaster().getWidth(),
                panel.getRaster().getHeight(),
                camera.getViewMatrix(),
                proj
                );

        cube = new Cube();
        arrow = new Arrow();
        axisX = new AxisX();
        axisY = new AxisY();
        axisZ = new AxisZ();
        curve = new Curve();
        cylinder = new Cylinder();
        surface = new Surface();
        
        // inicializace pole teles a puvodnich barev
        solids = new Solid[]{cube, arrow, curve, cylinder, surface};
        originalColors = new Col[solids.length];
        for(int i = 0; i < solids.length; i++){
            originalColors[i] = new Col(solids[i].getColor());
        }
        
        // nastavit prvni teleso jako aktivni
        selectSolid(0);
        
        // inicializace animacniho timeru
        animationTimer = new Timer(16, e -> updateAnimation()); // ~60 FPS
        animationTimer.setRepeats(true);

        initListeners();
        drawScene();
    }

    private void initListeners() {
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Solid activeSolid = solids[activeSolidIndex];
                
                // vyber telesa pomoci ciselne klavesnice (1-5)
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_5) {
                    int index = e.getKeyCode() - KeyEvent.VK_1;
                    if (index < solids.length) {
                        selectSolid(index);
                        drawScene();
                        return;
                    }
                }
                
                // transformace aktivniho telesa
                // translace sipkami
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    activeSolid.mulModel(new Mat4Transl(0.1, 0, 0));
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    activeSolid.mulModel(new Mat4Transl(-0.1, 0, 0));
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    activeSolid.mulModel(new Mat4Transl(0, 0.1, 0));
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    activeSolid.mulModel(new Mat4Transl(0, -0.1, 0));
                }
                if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    activeSolid.mulModel(new Mat4Transl(0, 0, 0.1));
                }
                if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    activeSolid.mulModel(new Mat4Transl(0, 0, -0.1));
                }
                
                // rotace kolem os
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    // rotace kolem X osy (dopredu)
                    rotateAroundCenter(activeSolid, new Mat4RotX(Math.toRadians(5)));
                }
                if (e.getKeyCode() == KeyEvent.VK_E) {
                    // rotace kolem X osy (dozadu)
                    rotateAroundCenter(activeSolid, new Mat4RotX(Math.toRadians(-5)));
                }
                if (e.getKeyCode() == KeyEvent.VK_Z) {
                    // rotace kolem Z osy (doleva)
                    rotateAroundCenter(activeSolid, new Mat4RotZ(Math.toRadians(5)));
                }
                if (e.getKeyCode() == KeyEvent.VK_X) {
                    // rotace kolem Z osy (doprava)
                    rotateAroundCenter(activeSolid, new Mat4RotZ(Math.toRadians(-5)));
                }
                if (e.getKeyCode() == KeyEvent.VK_T) {
                    // rotace kolem Y osy (doleva)
                    rotateAroundCenter(activeSolid, new Mat4RotY(Math.toRadians(5)));
                }
                if (e.getKeyCode() == KeyEvent.VK_Y) {
                    // rotace kolem Y osy (doprava)
                    rotateAroundCenter(activeSolid, new Mat4RotY(Math.toRadians(-5)));
                }
                
                // Scale
                if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD) {
                    activeSolid.mulModel(new Mat4Scale(1.1));
                }
                if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                    activeSolid.mulModel(new Mat4Scale(0.9));
                }

                // Kamera - WSAD
                if(e.getKeyCode() == KeyEvent.VK_W) {
                    camera = camera.forward(0.5);
                }

                if(e.getKeyCode() == KeyEvent.VK_A) {
                    camera = camera.left(0.5);
                }

                if(e.getKeyCode() == KeyEvent.VK_S) {
                    camera = camera.backward(0.5);
                }

                if(e.getKeyCode() == KeyEvent.VK_D) {
                    camera = camera.right(0.5);
                }

                if(e.getKeyCode() == KeyEvent.VK_P) {
                    toggleProjection();
                }
                
                // spusteni animace na Space
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!isAnimating) {
                        startRotationAnimation(activeSolid);
                    }
                }
                
                // prepinani typu kubik (pouze pokud je aktivni Curve)
                if(activeSolid instanceof Curve) {
                    Curve curve = (Curve) activeSolid;
                    if(e.getKeyCode() == KeyEvent.VK_B) {
                        curve.setCubicType(Curve.CubicType.BEZIER);
                    }
                    if(e.getKeyCode() == KeyEvent.VK_C) {
                        curve.setCubicType(Curve.CubicType.COONS);
                    }
                    if(e.getKeyCode() == KeyEvent.VK_F) {
                        curve.setCubicType(Curve.CubicType.FERGUSON);
                    }
                }

                drawScene();
            }
        });

        // mouse look - rozhlizeni pri drzeni praveho tlacitka mysi
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // prave tlacitko
                    isRightMousePressed = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isRightMousePressed = false;
                    lastMouseX = -1;
                    lastMouseY = -1;
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isRightMousePressed) {
                    handleMouseLook(e.getX(), e.getY());
                }
            }
        });

    }
    
    private void handleMouseLook(int currentX, int currentY) {
        if (lastMouseX == -1 || lastMouseY == -1) {
            lastMouseX = currentX;
            lastMouseY = currentY;
            return;
        }
        
        // vypocitat relativni pohyb mysi
        int deltaX = currentX - lastMouseX;
        int deltaY = currentY - lastMouseY;
        
        // aktualizovat kameru - deltaX ovlivnuje azimuth (horizontalni rotace)
        // deltaY ovlivnuje zenith (vertikalni rotace)
        camera = camera.addAzimuth(deltaX * MOUSE_SENSITIVITY);
        camera = camera.addZenith(-deltaY * MOUSE_SENSITIVITY); // negace pro ovladani
        
        // ulozit aktualni pozici pro pristi vypocet
        lastMouseX = currentX;
        lastMouseY = currentY;
        
        drawScene();
    }
    
    private void toggleProjection() {
        isPerspective = !isPerspective;
        
        // reset kamery na vychozi hodnoty pri prepnuti zpet na perspektivni projekci
        if (isPerspective) {
            camera = new Camera()
                    .withPosition(DEFAULT_CAMERA_POSITION)
                    .withAzimuth(DEFAULT_AZIMUTH)
                    .withZenith(DEFAULT_ZENITH)
                    .withFirstPerson(true);
        }
        
        double aspectRatio = panel.getRaster().getHeight() / (double) panel.getRaster().getWidth();
        double zn = 0.1;
        double zf = 100.0;
        
        if (isPerspective) {
            // perspektivni projekce
            proj = new Mat4PerspRH(
                    Math.toRadians(90), // FOV
                    aspectRatio,
                    zn,
                    zf
            );
        } else {
            // pravouhla projekce
            double width = 10.0; // sirka viditelneho prostoru
            double height = width * aspectRatio; // vyska podle pomeru stran
            proj = new Mat4OrthoRH(width, height, zn, zf);
        }
        
        renderer.setProj(proj);
    }

    private void drawScene() {
        panel.getRaster().clear();

        renderer.setView(camera.getViewMatrix());

        renderer.renderAxis(axisX);
        renderer.renderAxis(axisY);
        renderer.renderAxis(axisZ);

        renderer.render(cube);
        renderer.render(arrow);
        renderer.render(curve);
        renderer.render(cylinder);
        renderer.render(surface);

        panel.repaint();
    }
    
    // vybere teleso podle indexu a zmeni jeho barvu na bilou
    private void selectSolid(int index) {
        // vratit puvodni barvu predchozimu aktivnimu telesu
        if (activeSolidIndex >= 0 && activeSolidIndex < solids.length) {
            solids[activeSolidIndex].setColor(originalColors[activeSolidIndex]);
        }
        
        // nastavit nove aktivni teleso
        activeSolidIndex = index;
        solids[activeSolidIndex].setColor(SELECTED_COLOR);
    }
    
    // rotuje teleso kolem jeho stredu
    private void rotateAroundCenter(Solid solid, Mat4 rotation) {
        // pro jednoduchost rotujeme kolem pocatku (0,0,0)
        // pokud by teleso melo stred jinde, museli bychom:
        // 1. translace do pocatku
        // 2. rotace
        // 3. translace zpet
        solid.mulModel(rotation);
    }
    
    // spusti animaci - teleso se posune doprava, rotuje a vrati se zpet
    private void startRotationAnimation(Solid solid) {
        if (isAnimating) {
            return; // animace uz bezi
        }
        
        animatingSolid = solid;
        animationStartModel = new Mat4(solid.getModel()); // ulozit pocatecni transformaci
        animationStartTime = System.currentTimeMillis();
        isAnimating = true;
        
        animationTimer.start();
    }
    
    // aktualizuje animaci - kombinace translace a rotace, pak navrat
    private void updateAnimation() {
        if (!isAnimating || animatingSolid == null) {
            animationTimer.stop();
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - animationStartTime;
        
        if (elapsed >= ANIMATION_DURATION) {
            // animace dokoncena - vratit na pocatecni transformaci
            animatingSolid.setModel(new Mat4(animationStartModel));
            isAnimating = false;
            animationTimer.stop();
        } else {
            // vypocitat progress (0.0 az 1.0)
            double t = (double) elapsed / ANIMATION_DURATION;
            
            // pouzit easing funkci pro plynulejsi pohyb (ease-in-out)
            double easedT = t < 0.5 
                ? 2 * t * t  // zrychleni na zacatku
                : 1 - Math.pow(-2 * t + 2, 2) / 2; // zpomaleni na konci
            
            // prvni polovina: pohyb doprava + rotace
            // druha polovina: navrat zpet + pokracovani rotace
            double translationX;
            if (t < 0.5) {
                // jede doprava (0 -> max)
                double forwardProgress = t * 2; // 0 -> 1
                translationX = forwardProgress * 2.0; // posun o 2 jednotky doprava
            } else {
                // vraci se zpet (max -> 0)
                double backwardProgress = (t - 0.5) * 2; // 0 -> 1
                translationX = (1 - backwardProgress) * 2.0; // z 2 zpet na 0
            }
            
            // rotace - celkem 2x360° behem animace (rychlejsi rotace)
            double totalRotation = 4 * Math.PI * easedT; // 0 az 4π (2x360°)
            
            // slozena transformace: translace + rotace
            // poradi: nejdriv rotace, pak translace (aby se rotovalo kolem vlastniho stredu)
            Mat4 rotation = new Mat4RotY(totalRotation);
            Mat4 translation = new Mat4Transl(translationX, 0, 0);
            
            // aplikovat: start * rotace * translace
            animatingSolid.setModel(animationStartModel.mul(rotation).mul(translation));
        }
        
        drawScene();
    }
}
