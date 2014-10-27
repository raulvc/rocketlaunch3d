import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import java.awt.*;
import java.util.Random;

/**
 * Created by raul on 10/27/14.
 */
public class Rocket{

    private Shape3D rocket = null;

    public Rocket(){
        this.rocket = loadRocket();
    }

    public Shape3D getRocket(){
        return this.rocket;
    }

    // rocket
    private Shape3D loadRocket(){
        Shape3D rocket = null;
        try{
            Scene s = null;
            ObjectFile f = new ObjectFile();
            f.setFlags (ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
            s = f.load("//home//raul//estudos//cg//trab//src//rocket.obj");

            // rocket texture
            TextureLoader loader = new TextureLoader("//home//raul//estudos//cg//trab//src//textures//rocket_texture.jpg", new Container());
            Texture texture = loader.getTexture();
            texture.setBoundaryModeS(Texture.WRAP);
            texture.setBoundaryModeT(Texture.WRAP);
            texture.setBoundaryColor( new Color4f( 0.0f, 1.0f, 0.0f, 0.0f ) );
            TextureAttributes texAttr = new TextureAttributes();
            //could be REPLACE, BLEND or DECAL instead of MODULATE
            texAttr.setTextureMode(TextureAttributes.BLEND);
            Appearance ap = new Appearance();
            ap.setTexture(texture);
            ap.setTextureAttributes(texAttr);
            ap.setMaterial(new Material(new Color3f(Color.black), new Color3f(Color.black), new Color3f(Color.lightGray), new Color3f(Color.black), 1.0f));
            rocket = (Shape3D) s.getSceneGroup().getChild(0);
            rocket.setAppearance(ap);
            s.getSceneGroup().removeChild(0);
        }
        catch (java.io.FileNotFoundException ex){
            ex.printStackTrace();
        }
        return rocket;
    }

}
