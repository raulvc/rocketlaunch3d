import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.*;


public class RocketLaunch extends JFrame implements KeyListener, ActionListener, MouseMotionListener {
    // initial pos
    private Sounds sound = new Sounds();
    private Thread s1 = new Thread(sound);
    private float xpos = 0.0f;
    private float ypos = -0.1f;
    private float zpos = -4.5f;
    private float top_xfactor = 0.037f;
    private float top_yfactor = 2.362f;
    private float top_zfactor = -0.2f;
    private float def_movespeed = 1.0f; // acceleration
    private int shakeleft = 1; // for swapping x direction when shaking
    private int shakecount = 0;
    private boolean inFlight = false; // settings config phase
    private boolean deallocated = false;

    // camera and grouping
    private TransformGroup rocket_bot_tg  = null; // will use it to apply tranformations on bottom part of the rocket
    private TransformGroup rocket_top_tg  = null; // will use it to apply tranformations on top part of the rocket
    private GraphicsConfiguration config  = null;
    private Canvas3D canvas 			  = null;
    private SimpleUniverse universe 	  = null;
    private BranchGroup root			  = null;
    private TransformGroup camera		  = null;
    private OrbitBehavior orbit           = null;
    private Transform3D orbit_reset       = null;

    public static void main(String[] args) {
        RocketLaunch rl = new RocketLaunch();
        rl.setUp();
    }

    public RocketLaunch(){
        this.setName("Rocket Launch vAlpha");
        // kill the window on close
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvent) {
                System.exit(0);
            }
        });
    }

    public void setUp() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1, 2, 2));

        // setting up the canvas
        this.config = SimpleUniverse.getPreferredConfiguration();
        this.canvas = new Canvas3D(config);
        this.canvas.setSize(800, 600);
        // connecting canvas to keyboard/mousemotion listener
        this.canvas.addKeyListener(this);
        this.canvas.addMouseMotionListener(this);
        this.universe = new SimpleUniverse(this.canvas);

        this.camera = this.universe.getViewingPlatform().getViewPlatformTransform();
        // setting mouse rotation behaviour
        this.orbit = new OrbitBehavior(this.canvas, OrbitBehavior.REVERSE_ROTATE + OrbitBehavior.DISABLE_TRANSLATE
            + OrbitBehavior.STOP_ZOOM);
        this.orbit_reset = new Transform3D();
        this.universe.getViewingPlatform().getViewPlatformTransform().getTransform(this.orbit_reset);
        this.orbit.setHomeTransform(this.orbit_reset);
        this.orbit.setMinRadius(1.0);
        this.orbit.setRotationCenter(new Point3d(0.0f, -0.1f, -4.5f));
        this.orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0,0.0,0.0), 2000.0));
        this.universe.getViewingPlatform().setViewPlatformBehavior(this.orbit);

        this.root = new BranchGroup();


        // adding stuff to the scene
        addBackground(this.root);
        addObjects(this.root);
        addLights(this.root);

        RocketBehavior test = new RocketBehavior(this);
        test.setSchedulingBounds(new BoundingSphere(new Point3d( 0.0, 0.0, 0.0 ), 100000.0));
        this.root.addChild(test);

        this.root.compile();

        this.universe.addBranchGraph(this.root);

        panel.add(this.canvas);
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
    }

    public void addBackground(BranchGroup group){
        Background bg = new Background();
        BoundingSphere sphere = new BoundingSphere(new Point3d(0,0,0), 100000);
        bg.setApplicationBounds(sphere);
        BranchGroup backGeoBranch = new BranchGroup();
        Sphere s = new Skydome().getSkydome();
        backGeoBranch.addChild(s);
        bg.setGeometry(backGeoBranch);
//        bg.setGeometry(group);
        group.addChild(bg);
    }

    public void addLights(BranchGroup group) {
        // putting up some directional light on the rocket
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10000.0);

        Color3f light1Color = new Color3f(Color.white);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color,
                light1Direction);
        light1.setInfluencingBounds(bounds);
        group.addChild(light1);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(Color.white);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        group.addChild(ambientLightNode);
    }

    private void addObjects(BranchGroup group) {
        // positioning and loading ground
        TransformGroup ground_tg = new TransformGroup();
        Transform3D ground_t3d = new Transform3D();
        Vector3f ground_v3f = new Vector3f(-10.0f, -1.1f, -10.0f);
        ground_t3d.setTranslation(ground_v3f);
        ground_tg.setTransform(ground_t3d);
        Shape3D g = new Ground().getGround();
        ground_tg.addChild(g);

        // loading and positioning rocket
        Shape3D r_bot = new RocketBottom().getRocket();
        Shape3D r_top = new RocketTop().getRocket();

        // bottom part
        Transform3D r_bot_pos = new Transform3D();
        r_bot_pos.setTranslation(new Vector3f(this.xpos, this.ypos, this.zpos));
        rocket_bot_tg = new TransformGroup();
        rocket_bot_tg.addChild(r_bot);
        rocket_bot_tg.setTransform(r_bot_pos);
        rocket_bot_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        // top part
        Transform3D r_top_pos = new Transform3D();
        r_top_pos.setScale(1.4);
        r_top_pos.setTranslation(new Vector3f(xpos + top_xfactor, ypos + top_yfactor, this.zpos + top_zfactor));
        rocket_top_tg = new TransformGroup();
        rocket_top_tg.addChild(r_top);
        rocket_top_tg.setTransform(r_top_pos);
        rocket_top_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        group.addChild(ground_tg);
        group.addChild(rocket_bot_tg);
        group.addChild(rocket_top_tg);
    }

    private void moveCam(Transform3D trans){
        Vector3d vector = new Vector3d();
        this.camera.getTransform(trans);
        trans.get(vector);
        vector.y = (2*ypos+top_yfactor)/2;
        // moving camera to the back during init flight
        if (vector.z < 5.0)
            vector.z += 0.006;
        trans.set(vector);
        this.camera.setTransform(trans);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_SPACE) {
            if (!this.inFlight) {
                // launch start
                s1.start();
                this.orbit.goHome();
                this.inFlight = true;
            }
            else{
                // deallocate rocket base
                this.inFlight = false;
                this.deallocated = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    private void accelerate(){
        // determine acceleration
        if (ypos < 1.0)
            ypos += 0.01;
        else if (ypos < 3.0)
            ypos += 0.02;
        else if (ypos < 5.0)
            ypos += 0.03;
        else if (ypos < 10.0)
            ypos += 0.05;
        else
            // default speed
            ypos += def_movespeed;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       update();
    }

    public void update(){
        // action monitoring
        if (inFlight){
            accelerate();
            if (this.ypos > 25.0){
                // making some shaky cam effects
                if (this.shakeleft>0)
                    this.xpos += 0.002;
                else
                    this.xpos -= 0.002;
                this.shakecount += 1;
                if (shakecount > 8) {
                    this.shakeleft *= -1;
                    this.shakecount = 0;
                }
            }
            Transform3D bot_trans = new Transform3D();
            bot_trans.setTranslation(new Vector3f(xpos, ypos, zpos));
            Transform3D top_trans = new Transform3D();
            top_trans.setScale(1.4);
            top_trans.setTranslation(new Vector3f(xpos + top_xfactor, ypos + top_yfactor, zpos + top_zfactor));
            this.rocket_bot_tg.setTransform(bot_trans);
            this.rocket_top_tg.setTransform(top_trans);
            moveCam(bot_trans);
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }
}