import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

public class RocketLaunch {

    public static void main(String[] args) {
        RocketLaunch rl = new RocketLaunch();
        rl.setUp();
    }

    public void setUp() {
        JFrame jf = new JFrame("Rocket Launch vAlpha");
        // kill the window on close
        jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvent) {
                System.exit(0);
            }
        });
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1, 2, 2));

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setSize(800, 600);
        SimpleUniverse universe = new SimpleUniverse(canvas3D);
        BranchGroup group = new BranchGroup();
        addObjects(group);
        addLights(group);
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(group);
        panel.add(canvas3D);
        jf.getContentPane().add(panel, BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);
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
        TransformGroup rocket_tg = new TransformGroup();
        Transform3D rocket_t3d = new Transform3D();
        Vector3f rocket_v3f = new Vector3f(0.0f, -0.5f, -4.5f);
        rocket_t3d.setTranslation(rocket_v3f);
        rocket_tg.setTransform(rocket_t3d);
        Shape3D r = new Rocket().getRocket();
        rocket_tg.addChild(r);

        group.addChild(ground_tg);
        group.addChild(rocket_tg);

    }
}