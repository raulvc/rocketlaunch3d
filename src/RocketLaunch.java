import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.microcrowd.loader.java3d.max3ds.Loader3DS;
import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;
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
        canvas3D.setSize(360, 160);
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
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                1000.0);

        Color3f light1Color = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color,
                light1Direction);
        light1.setInfluencingBounds(bounds);
        group.addChild(light1);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        group.addChild(ambientLightNode);
    }

    private void addObjects(BranchGroup group) {
        TransformGroup tg = new TransformGroup();
        Transform3D t3d = new Transform3D();
        Vector3f v3f = new Vector3f(-1.6f, -1.35f, -6.5f);
        t3d.setTranslation(v3f);
        tg.setTransform(t3d);

        // Rocket test
        try{
            Scene s = null;
            ObjectFile f = new ObjectFile();
            f.setFlags (ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
            s = f.load("//home//raul//estudos//cg//trab//src//rocket.obj");
//            s = new Loader3DS().load("//home//raul//estudos//cg//trab//src//rocket.3ds");
            tg.addChild (s.getSceneGroup());
        }
        catch (java.io.FileNotFoundException ex){
            //
            ex.printStackTrace();
        }

        group.addChild(tg);

    }
}