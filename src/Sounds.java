
import java.io.*;
import javax.sound.sampled.*;
import sun.audio.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Willian
 */
public class Sounds implements Runnable {

    private Clip clip, clip2;
    private volatile boolean fly = true;

    public void uncouple() {
        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("src//sounds//decoupling_sound.wav"));
            clip2 = AudioSystem.getClip();
            clip2.open(inputStream);
            clip2.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void turbines_off() {
        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("src//sounds//turbines_off_sound.wav"));
            clip2 = AudioSystem.getClip();
            clip2.open(inputStream);
            clip2.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void stopflight()  {
        fly = false;
        int i;
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);;
        for (i = 1; i < 10; i++) {
            gainControl.setValue(-10.0f);
        }
    }

    public void run() {
        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("src//sounds//rocket_sound.wav"));
            clip = AudioSystem.getClip();
            clip.open(inputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            while (fly) {
            }
            clip.stop();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
