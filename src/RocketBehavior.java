import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupOnElapsedTime;
import java.util.Enumeration;

public class RocketBehavior extends Behavior {

    private WakeupOnElapsedTime wakeupCondition = new WakeupOnElapsedTime(10);
    private RocketLaunch rl;

    RocketBehavior(RocketLaunch rl){
        this.rl = rl;
    }

    @Override
    public void initialize() {
//        System.out.println("passei aqui");
        wakeupOn(wakeupCondition);
    }

    @Override
    public void processStimulus(Enumeration enumeration) {
        System.out.println("passei aqui");
        this.rl.update();
        wakeupOn(wakeupCondition);
    }
}