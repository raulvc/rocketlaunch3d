
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.*;
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.vecmath.*;

public class RocketLaunch extends JFrame implements KeyListener, MouseMotionListener {

    // fuel related
    private float fuel_base, fuel_top;
    private TransformGroup hud_tg = null;
    private Text3D hud_text = null;
    private Text3D hud_text_height = null;
    // positioning of the top part of the rocket
    private float xpos = 0.0f;
    private float ypos = -0.1f;
    private float zpos = -4.5f;
    private float angle = 0;
    // positioning of the bot part of the rocket
    private float top_xfactor = 0.037f;
    private float top_yfactor = 2.362f;
    private float top_zfactor = -0.2f;
    // bot part when detached
    private float d_xpos;
    private float d_ypos;
    private float d_zpos;
    private float d_angle = 0;
    // default acceleration
    private float def_movespeed = 1.0f;
    // current flight speed
    private float cur_speed = 0.0f;
    private float bot_falling_speed = 1.0f; // when positive it's still going up
    // for swapping x values when shaking
    private int shakeleft = 1;
    private int shakecount = 0;
    // sounds
    private Sounds sound = new Sounds();
    private Thread s1 = new Thread(sound);

    /*
     States the scene can assume:
     0 -> initial, static
     1 -> initial flight
     2 -> detachment
     3 -> out of gas / free falling
     4 -> stopped simulation
     */
    private int state = 0;

    // camera and grouping
    private TransformGroup rocket_bot_tg = null; // will use it to apply transformations on bottom part of the rocket
    private TransformGroup rocket_top_tg = null; // will use it to apply transformations on top part of the rocket
    private GraphicsConfiguration config = null;
    private Canvas3D canvas = null;
    private SimpleUniverse universe = null;
    private BranchGroup root = null;
    private TransformGroup camera = null;
    private OrbitBehavior orbit = null;
    private Transform3D orbit_reset = null;

    public static void main(String[] args) {
        RocketLaunch rl = new RocketLaunch();
        rl.setUp();

    }

    public RocketLaunch() {
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
        // camera
        this.camera = this.universe.getViewingPlatform().getViewPlatformTransform();

        // setting mouse rotation behaviour
        this.orbit = new OrbitBehavior(this.canvas, OrbitBehavior.REVERSE_ROTATE + OrbitBehavior.DISABLE_TRANSLATE
                + OrbitBehavior.STOP_ZOOM);
        this.orbit_reset = new Transform3D();
        this.universe.getViewingPlatform().getViewPlatformTransform().getTransform(this.orbit_reset);
        this.orbit.setHomeTransform(this.orbit_reset);
        this.orbit.setMinRadius(1.0);
        this.orbit.setRotationCenter(new Point3d(0.0f, -0.1f, -4.5f));
        this.orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 2000.0));
        this.universe.getViewingPlatform().setViewPlatformBehavior(this.orbit);

        this.root = new BranchGroup();
        // adding stuff to the scene
        addBackground(this.root);
        addObjects(this.root);
        addLights(this.root);
        addHUD(this.root);
        // adding a behavior to fix flickering when using conventional timers
        RocketBehavior screenUpdater = new RocketBehavior(this);
        screenUpdater.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100000.0));
        this.root.addChild(screenUpdater);
        // make optimizations on the scene objects
        this.root.compile();

        this.universe.addBranchGraph(this.root);

        // swing stuff
        panel.add(this.canvas);

        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        show_config();
    }

    public void show_config(){
        //fuel input box panel
        JTextField fuel1 = new JTextField(5);
        JTextField fuel2 = new JTextField(5);
        JPanel fuel_inputs = new JPanel();
        fuel_inputs.add(new JLabel("Litros de combustível da base:"));
        fuel_inputs.add(fuel1);
        fuel_inputs.setPreferredSize(new Dimension(200, 100));
        fuel_inputs.add(new JLabel("Litros de combustível do topo:"));
        fuel_inputs.add(fuel2);
        JOptionPane.showConfirmDialog(null, fuel_inputs, "Insira a quantidade de combustivel", JOptionPane.OK_CANCEL_OPTION);
        boolean incorrect = true;
        //accept only numbers on fuel fields
        while (incorrect) {
            try {
                fuel_base = Float.parseFloat(fuel1.getText());
                fuel_top = Float.parseFloat(fuel2.getText());
                incorrect = false;
            } catch (NumberFormatException nfe) {
                fuel1.setText("");
                fuel2.setText("");
                JOptionPane.showConfirmDialog(null, fuel_inputs, "Insira a quantidade de combustivel", JOptionPane.OK_CANCEL_OPTION);
            }
        }
    }

    public void reset_simulation(){
        // stoping stuff and resetting values
        sound.shutdown();
        s1.interrupt();
        s1 = null;
        s1 = new Thread(sound);
        state = 0;
        xpos = 0.0f;
        ypos = -0.1f;
        zpos = -4.5f;
        Transform3D trans_bot = new Transform3D();
        trans_bot.setTranslation(new Vector3f(xpos, ypos, zpos));

        Transform3D trans_top = new Transform3D();
        trans_top.setScale(1.4);
        trans_top.setTranslation(new Vector3f(xpos+top_xfactor, ypos+top_yfactor, zpos+top_zfactor));

        rocket_bot_tg.setTransform(trans_bot);
        rocket_top_tg.setTransform(trans_top);
        moveCam(trans_bot);
        hud_text.setString(" ");
        hud_text_height.setString(" ");
        show_config();
    }

    public void rotate_to_zero(TransformGroup tg){
        Transform3D t3d = new Transform3D();
        t3d.rotX(0);
        t3d.rotY(0);
        t3d.rotZ(0);
        tg.setTransform(t3d);
    }

    public void addHUD(BranchGroup group){
        // hud
        Font3D f3d = new Font3D(new Font("TestFont", Font.PLAIN, 2), new FontExtrusion());
        hud_text = new Text3D(f3d, new String("RLv1"), new Point3f(-6.0f,-0.5f, -3.5f));
        hud_text.setCapability(Text3D.ALLOW_STRING_READ);
        hud_text.setCapability(Text3D.ALLOW_STRING_WRITE);
        hud_text_height =new Text3D (f3d, new String(" "), new Point3f(-6.0f,-2.0f, -3.5f));
        hud_text_height.setCapability(Text3D.ALLOW_STRING_READ);
        hud_text_height.setCapability(Text3D.ALLOW_STRING_WRITE);
        // text appearance
        Shape3D fuel_sh = new Shape3D();
        Shape3D height_sh = new Shape3D();
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
        Color3f blue = new Color3f(.2f, 0.2f, 0.6f);
        Appearance a = new Appearance();
        Material m = new Material(blue, blue, blue, white, 80.0f);
        m.setLightingEnable(true);
        a.setMaterial(m);
        fuel_sh.setAppearance(a);
        fuel_sh.setGeometry(hud_text);
        height_sh.setAppearance(a);
        height_sh.setGeometry(hud_text_height);
        hud_tg = new TransformGroup();
        hud_tg.addChild(fuel_sh);
        hud_tg.addChild(height_sh);
        hud_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        group.addChild(hud_tg);
    }


    public void addBackground(BranchGroup group) {
        Background bg = new Background();
        BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
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

    private void moveCam(Transform3D trans) {
        Vector3d vector = new Vector3d();
        this.camera.getTransform(trans);
        trans.get(vector);
        switch (state){
            case 0: // resetting camera
            case 1: // cam moves along both parts of the rocket
                vector.y = (2 * ypos + top_yfactor) / 2;
                break;
            case 2: // cam follows top part
            case 3:
                vector.y = (2 * ypos - top_yfactor) / 2;
                break;
        }

        // moving camera to the back during init flight
        if (vector.z < 6.0) {
            vector.z += 0.012;
        }
        trans.set(vector);
        this.camera.setTransform(trans);
        update_hud();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            switch (state) {
                case 0:
                    // launch start
                    s1.start();
                    this.orbit.goHome();
                    state = 1;
                    break;
                case 1:
                    // deallocate rocket base
                    initDetachedFall();
                    state = 2;
                    break;
                case 2:
                    // start free fall
                    state = 3;
                    sound.stopflight();
                    sound.turbines_off();
                    break;
                case 3: // out of fuel
                case 4: // stopped simulation
                    reset_simulation();
            }
        }
    }

    private void initDetachedFall() {
        // initializing bottom part fall
        sound.uncouple();
        d_xpos = xpos;
        d_ypos = ypos;
        d_zpos = zpos;
        xpos += top_xfactor;
        ypos += top_yfactor;
        zpos += top_zfactor;
        bot_falling_speed = cur_speed;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void setNewCoordinates(Transform3D trans, boolean dealloc) {
        Vector3d vector = new Vector3d();
        trans.get(vector);
        if (!dealloc) {
            this.xpos = (float) vector.x;
            this.ypos = (float) vector.y;
            this.zpos = (float) vector.z;
        } else {
            this.d_xpos = (float) vector.x;
            this.d_ypos = (float) vector.y;
            this.d_zpos = (float) vector.z;
        }
    }

    private Transform3D accelerate(float x, float y, float z, boolean topPart) {
        // determine acceleration
        Transform3D trans = new Transform3D();
        if (y < 1.0) {
            y += 0.01;
            cur_speed = (float) 0.01;
        } else if (y < 3.0) {
            y += 0.02;
            cur_speed = (float) 0.02;
        } else if (y < 5.0) {
            y += 0.03;
            cur_speed = (float) 0.03;
        } else if (y < 10.0) {
            y += 0.05;
            cur_speed = (float) 0.05;
        } else {
            // default speed
            y += def_movespeed;
            cur_speed = def_movespeed;
        }
        if (topPart) {
            trans.setScale(1.4);
        }
        trans.setTranslation(new Vector3f(x, y, z));
        return trans;
    }

    private Transform3D decelerate(float x, float y, float z, float speed, float angle, boolean topPart) {
        // for slowing down and free falling objects
        Transform3D trans = new Transform3D();
        Transform3D secondAxis = new Transform3D();
        switch (state){
            case 2:
                if (speed > 0.999) {
                    // don't rotate immediately
                    speed -= 0.0004;
                } else if (speed > 0.2) {
                    // just started losing speed
                    speed -= 0.0004;
                    angle += Math.PI / 250;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                } else if (speed > -1.0) {
                    speed -= 0.0005;
                    angle += Math.PI / 240;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                } else if (speed > -2.0) {
                    // losing speed steadily
                    speed -= 0.001;
                    angle += Math.PI / 230;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                } else if (speed > -6.0) {
                    // losing speed steadily
                    speed -= 0.01;
                    angle += Math.PI / 220;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                } else {
                    // stabilize fall
                    speed -= 1.0;
                    angle += Math.PI / 200;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                }
                break;
            case 3:
                System.out.println(speed);
                if (speed > 0.6) {
                    // don't rotate immediately
                    speed -= 0.004;
                } else if (speed > 0.2) {
                    // just started losing speed
                    speed -= 0.006;
                    angle += Math.PI / 250;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                } else if (speed > -1.0) {
                    speed -= 0.008;
                    angle += Math.PI / 240;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                } else {
                    // stabilize fall
                    speed -= 0.009;
                    angle += Math.PI / 240;
                    trans.rotY(angle);
                    secondAxis.rotZ(angle);
                }
                break;
        }

        if (secondAxis != null) {
            trans.mul(secondAxis);
        }
        if (topPart) {
            trans.setScale(1.4);
            this.cur_speed = speed;
            this.angle = angle;
        } else {
            this.bot_falling_speed = speed;
            this.d_angle = angle;
        }
        y += speed;
        x += 0.001;
        trans.setTranslation(new Vector3f(x, y, z));
        return trans;
    }

    public void update() {
        // action monitoring
        switch (state) {
            case 1:
                /* TAKING FLIGHT */
                if (this.ypos > 23.0) {
                    shakeRocket();
                }
                Transform3D movementTrans = accelerate(xpos, ypos, zpos, false);
                setNewCoordinates(movementTrans, false);
                // my models have different, incompatible scales so I have to do this little workaround
                Transform3D topCorrection = new Transform3D();
                topCorrection.setScale(1.4);
                topCorrection.setTranslation(new Vector3f(xpos + top_xfactor, ypos + top_yfactor, zpos + top_zfactor));

                // adapting obj size
                this.rocket_bot_tg.setTransform(movementTrans);
                this.rocket_top_tg.setTransform(topCorrection);
                moveCam(movementTrans);
                // spending gas
                this.decrement_fuel(1);
                if (fuel_base <= 0) {
                    // deallocate rocket base
                    initDetachedFall();
                    state = 2;
                }
                break;
            case 2:
                /* DETACHING BOTTOM */
                // bot part
                Transform3D detachedBotTrans = decelerate(d_xpos, d_ypos, d_zpos, bot_falling_speed, d_angle, false);
                setNewCoordinates(detachedBotTrans, true);
                this.rocket_bot_tg.setTransform(detachedBotTrans);

                // top part
                shakeRocket();
                Transform3D topMovementTrans = accelerate(xpos, ypos, zpos, true);
                setNewCoordinates(topMovementTrans, false);
                this.rocket_top_tg.setTransform(topMovementTrans);
                moveCam(topMovementTrans);
                // spending gas
                this.decrement_fuel(2);
                if (fuel_top <= 0) {
                    // start free fall
                    state = 3;
                    sound.stopflight();
                    sound.turbines_off();
                }
                break;
            case 3:
                /* FREE FALL */

                // bot part
                if (d_ypos >= -4.0f) {
                    Transform3D botStillFalling = decelerate(d_xpos, d_ypos, d_zpos, bot_falling_speed, d_angle, false);
                    setNewCoordinates(botStillFalling, true);
                    this.rocket_bot_tg.setTransform(botStillFalling);
                }


                Transform3D fallTopTrans = decelerate(xpos, ypos, zpos, cur_speed, angle, true);
                setNewCoordinates(fallTopTrans, false);
                this.rocket_top_tg.setTransform(fallTopTrans);
                moveCam(fallTopTrans);
                if (ypos <= 4.0f){
                    state = 4;
                }
        }
    }

    private void shakeRocket() {
        // making some shaky cam effects
        if (this.shakeleft > 0) {
            this.xpos += 0.002;
        } else {
            this.xpos -= 0.002;
        }
        this.shakecount += 1;
        if (shakecount > 8) {
            this.shakeleft *= -1;
            this.shakecount = 0;
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
    }

    public void decrement_fuel(int state){
        // consumes fuel
        switch (state){
            case 1:
                fuel_base -= 0.02;
                break;
            case 2:
                fuel_top -= 0.02;
        }
    }

    private float ymax;
    public void update_hud() {
        // updates the overlay text
        String fuel_text = "";
        switch (state){
            case 1:
                fuel_text = "BASE: " + String.format("%5.2f",fuel_base) + "L";
                break;
            case 2:
                fuel_text = "TOPO: " + String.format("%5.2f",fuel_top) + "L";
        }

        hud_text.setString(fuel_text);
        hud_text_height.setString("Alt.:   " + String.format("%5.2f", ypos) + "m");

        Transform3D move_text = new Transform3D();
        move_text.setScale(0.2);
        move_text.setTranslation(new Vector3f(xpos-3.1f, ypos-0.5f, zpos-3.5f));
        hud_tg.setTransform(move_text);
    }
}
