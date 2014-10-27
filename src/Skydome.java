import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

/**
 * Created by raul on 10/27/14.
 */

// giant sphere with sky-like texture
public class Skydome {
    private Sphere skydome = null;

    public Skydome() {
        this.skydome = loadSkydome();
    }

    public Sphere getSkydome(){
        return this.skydome;
    }

    private Sphere loadSkydome(){
        Sphere skydome = new Sphere(1.1f, Sphere.GENERATE_NORMALS | Sphere.GENERATE_NORMALS_INWARD
                | Sphere.GENERATE_TEXTURE_COORDS, 45);
        Appearance skydome_ap = skydome.getAppearance();
        TextureLoader texget = new TextureLoader("src//textures//sky_texture.jpg", null);
        skydome_ap.setTexture(texget.getTexture());
        return skydome;
    }
}
