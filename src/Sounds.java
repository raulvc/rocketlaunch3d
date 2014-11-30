
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

    private Clip clip;

    public void run() {
        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("src//sounds//rocket_sound.wav "));
            clip = AudioSystem.getClip();
            clip.open(inputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            while (true) {
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
