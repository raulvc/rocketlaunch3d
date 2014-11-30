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
        wakeupOn(wakeupCondition);
    }

    @Override
    public void processStimulus(Enumeration enumeration) {
        this.rl.update();
        wakeupOn(wakeupCondition);
    }
}