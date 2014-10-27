import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.*;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import java.util.Random;

/**
 * Created by raul on 10/27/14.
 */
public class Ground extends Shape3D {

    private Shape3D ground = null;

    public Ground() {
        this.ground = loadGround();
    }

    public Shape3D getGround(){
        return this.ground;
    }

    private Shape3D loadGround(){
        Shape3D ground = new Shape3D();
        Point3f[] pts;
        int[] stripCounts;
        int[] contourCounts;
        int width = 20;
        int length = 20;
        float[] texts;
        float lenUnit = (float) (length / 4);
        float widUnit = (float) (width / 4);
        float[] yPoints = new float[5];
        float[] xPoints = new float[5];
        for (int i = 0; i < 5; i++) {
            yPoints[i] = (float) (i * lenUnit);
            xPoints[i] = (float) (i * widUnit);
        }
        pts = new Point3f[144];
        texts = new float[288];
        int counter = 0;
        int checker = 0;
        int texter = 0;

        int edgesize = 5;
        int moderator = 2;
        float[] zlist = new float[20];
        Random zMaker = new Random(System.currentTimeMillis());

        for (int i = 0; i < 20; i++)
            zlist[i] = (zMaker.nextFloat() * moderator) - 1;

        Point3f[][] grid = new Point3f[7][7];

        grid[0][0] = new Point3f((0 - edgesize), zlist[0], (0 - edgesize));

        for (int i = 0; i < 5; i++)
            grid[i + 1][0] = new Point3f((i * lenUnit), zlist[i + 1], (0 - edgesize));
        grid[6][0] = new Point3f((width + edgesize), zlist[5], (0 - edgesize));

        for (int j = 1; j < 6; j++) {
            grid[0][j] = new Point3f((0 - edgesize), zlist[5 + ((j - 1) * 2)], ((j - 1) * widUnit));
            for (int i = 1; i < 6; i++)
                grid[i][j] = new Point3f(((i - 1) * lenUnit), 0, ((j - 1) * widUnit));
            grid[6][j] = new Point3f((width + edgesize), zlist[5 + (j * 2)], ((j - 1) * widUnit));
        }
        grid[0][6] = new Point3f((0 - edgesize), zlist[14], (length + edgesize));
        for (int i = 0; i < 5; i++)
            grid[i + 1][6] = new Point3f((i * lenUnit), zlist[15 + i], (length + edgesize));
        grid[6][6] = new Point3f((width + edgesize), zlist[19], (length + edgesize));
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) {
                pts[counter] = grid[i][j];
                counter++;
                pts[counter] = grid[i][j + 1];
                counter++;
                pts[counter] = grid[i + 1][j + 1];
                counter++;
                pts[counter] = grid[i + 1][j];
                counter++;
            }

        for (int v = 0; v < 288; v++) {
            texts[v] = 0.0f;
            texts[++v] = 0.0f;
            texts[++v] = 0.0f;
            texts[++v] = widUnit;
            texts[++v] = lenUnit;
            texts[++v] = widUnit;
            texts[++v] = lenUnit;
            texts[++v] = 0.0f;
        }

        stripCounts = new int[36];
        contourCounts = new int[36];
        for (int i = 0; i < 36; i++) {
            stripCounts[i] = 4;
            contourCounts[i] = 1;
        }

        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);

        gi.setTextureCoordinateParams(1, 2);
        gi.setTextureCoordinates( 0, texts);

        gi.setCoordinates(pts);
        gi.setStripCounts(stripCounts);
        gi.setContourCounts(contourCounts);

        NormalGenerator ng= new NormalGenerator();
        ng.setCreaseAngle ((float) Math.toRadians(30));
        ng.generateNormals(gi);
        Appearance aper = new Appearance();
        Material Mser = new Material();
        Mser.setShininess(0.01f);
        Mser.setEmissiveColor(0.0f, 0.0f, 0.0f);
        Mser.setAmbientColor(0.1f, 0.1f, 0.1f);
        aper.setMaterial(Mser);
        TextureLoader Texget=new TextureLoader("//home//raul//estudos//cg//trab//src//texture.jpg", null);
        Texture2D ourTex=(Texture2D) Texget.getTexture();
        TextureAttributes texatt=new TextureAttributes(TextureAttributes.BLEND, new Transform3D(), new Color4f(1.0f, 1.0f, 1.0f, 1.0f), TextureAttributes.NICEST);
        aper.setTextureAttributes(texatt);
        aper.setTexture(ourTex);
        ground.setGeometry(gi.getGeometryArray());
        ground.setAppearance(aper);
        return ground;
    }
}
