import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.*;

public class RocketLaunch extends JFrame implements KeyListener, ActionListener {

    private float height = -0.5f;
    private float sign = 1.0f; // going up by default
    private Timer timer;
    private boolean inFlight = false; // settings config phase
    private TransformGroup rocket_tg = null; // will use it to apply tranformations on the rocket
    // camera
    private ViewingPlatform view_cam;
    private Vector3d cam_vector;

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
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setSize(800, 600);
        // connecting canvas to keyboard listener
        canvas3D.addKeyListener(this);
        // connection timer to action listener
        timer = new Timer(10,this);
        SimpleUniverse universe = new SimpleUniverse(canvas3D);
        BranchGroup group = new BranchGroup();
        addBackground(group);
        addObjects(group);
        addLights(group);
//        universe.getViewingPlatform().setNominalViewingTransform();
//        ViewInfo information = new ViewInfo(universe.getViewingPlatform().getViewers()[0].getView());

        view_cam = universe.getViewingPlatform();
        initCam();
        universe.addBranchGraph(group);
        panel.add(canvas3D);
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
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);

        Color3f light1Color = new Color3f(Color.white);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color,
                light1Direction);
        light1.setInfluencingBounds(bounds);
        group.addChild(light1);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(Color.black);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        group.addChild(ambientLightNode);
    }

    private void addObjects(BranchGroup group) {
        // positioning and loading ground
        TransformGroup ground_tg = new TransformGroup();
        Transform3D ground_t3d = new Transform3D();
        Vector3f ground_v3f = new Vector3f(0.0f, -1.5f, 0.0f);
        ground_t3d.setTranslation(ground_v3f);
        ground_tg.setTransform(ground_t3d);
        Shape3D g = new Ground().getGround();
        ground_tg.addChild(g);

        // positioning and loading rocket
        rocket_tg = new TransformGroup();
        Transform3D rocket_t3d = new Transform3D();
        Vector3f rocket_v3f = new Vector3f(0.0f, -0.5f, -4.5f);
        rocket_t3d.setTranslation(rocket_v3f);
        rocket_tg.setTransform(rocket_t3d);
        Shape3D r = new Rocket().getRocket();
        rocket_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rocket_tg.addChild(r);

        group.addChild(ground_tg);
        group.addChild(rocket_tg);
    }

    private void initCam(){
        cam_vector = new Vector3d();
        Transform3D initialPos = new Transform3D();
        initialPos.setTranslation(new Vector3f(0, 0f, 4f));
        initialPos.lookAt(new Point3d(0, 2f, 2f), new Point3d(0, -1.5f, -5f), new Vector3d(0, 0f, -5f));
        initialPos.invert();
        view_cam.getViewPlatformTransform().setTransform(initialPos);
    }

    private void moveCam(Transform3D translation){
        Vector3d v = new Vector3d();
        Vector3d up = new Vector3d(0,0.05,0);
        translation.get(v);
        v.add(cam_vector);

        Point3d cam_pos = new Point3d();
        Point3d rocket_pos = new Point3d();

        Transform3D camTrans = new Transform3D();
        view_cam.getViewPlatformTransform().getTransform(camTrans);
        camTrans.setTranslation(v);
        translation.transform(rocket_pos);
        camTrans.transform(cam_pos);
        camTrans.lookAt(cam_pos, rocket_pos, up);
        camTrans.invert();
        view_cam.getViewPlatformTransform().setTransform(camTrans);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_SPACE) {
            // launch start
            inFlight = true;
            timer.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        // action monitoring
        if (inFlight == true){
            height += 0.05 * sign;
            Transform3D trans = new Transform3D();
            trans.setTranslation(new Vector3f(0.0f, height, -4.5f));
            rocket_tg.setTransform(trans);
//            moveCam(trans);
        }
    }
}