import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.image.TextureLoader;
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
        Color3f ambientColor = new Color3f(0.5f, 0.5f, 0.5f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        group.addChild(ambientLightNode);
    }

    private void addObjects(BranchGroup group) {
        TransformGroup tg = new TransformGroup();
        Transform3D t3d = new Transform3D();
        Vector3f v3f = new Vector3f(0.0f, -1.35f, -6.5f);
        t3d.setTranslation(v3f);
        tg.setTransform(t3d);

        // Rocket
        Shape3D rocket = null;
        try{
            Scene s = null;
            ObjectFile f = new ObjectFile();
            f.setFlags (ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
            s = f.load("//home//raul//estudos//cg//trab//src//rocket.obj");

            // rocket texture
            TextureLoader loader = new TextureLoader("//home//raul//estudos//cg//trab//src//texture.jpg", new Container());
            Texture texture = loader.getTexture();
            texture.setBoundaryModeS(Texture.WRAP);
            texture.setBoundaryModeT(Texture.WRAP);
            texture.setBoundaryColor( new Color4f( 0.0f, 1.0f, 0.0f, 0.0f ) );
            TextureAttributes texAttr = new TextureAttributes();
            //could be REPLACE, BLEND or DECAL instead of MODULATE
            texAttr.setTextureMode(TextureAttributes.MODULATE);
            Appearance ap = new Appearance();
            ap.setTexture(texture);
            ap.setTextureAttributes(texAttr);
            ap.setMaterial(new Material(new Color3f(Color.black), new Color3f(Color.black), new Color3f(Color.red), new Color3f(Color.black), 1.0f));
            rocket = (Shape3D) s.getSceneGroup().getChild(0);
            rocket.setAppearance(ap);
            s.getSceneGroup().removeChild(0);
        }
        catch (java.io.FileNotFoundException ex){
            ex.printStackTrace();
        }

        tg.addChild(rocket);
        group.addChild(tg);

    }
}