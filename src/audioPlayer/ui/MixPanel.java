package audioPlayer.ui;

import audioPlayer.Engine;
import audioPlayer.Status;
import audioPlayer.Track;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class MixPanel extends JPanel {
    static Dimension d1 = new Dimension(1200, 100);
    MixWavePanel mixWavePanel1;
    MixWavePanel mixWavePanel2;

    public MixPanel(Track track1, Track track2, Engine stream1, Engine stream2) {
        setPreferredSize(d1);
        setLayout(new GridLayout(2,0));

        mixWavePanel1 = new MixWavePanel(track1, stream1);
        mixWavePanel2 = new MixWavePanel(track2, stream2);

        add(mixWavePanel1);
        add(mixWavePanel2);
    }

    public void newTrack1(Track track, Engine stream){
        remove(mixWavePanel1);
        mixWavePanel1 = new MixWavePanel(track, stream);
        add(mixWavePanel1);
    }

    public void newTrack2(Track track, Engine stream){
        remove(mixWavePanel2);
        mixWavePanel1 = new MixWavePanel(track, stream);
        add(mixWavePanel2);
    }

    public static class MixWavePanel extends WavePanel {
        Track track;
        Dimension d2;
        BufferedImage image;
        int xPos;
        int newXPos;

        public MixWavePanel(Track track, Engine stream) {
            this.track = track;

            d2 = new Dimension(
                    (int) d1.getWidth(),
                    (int) d1.getHeight()/2
            );
            setPreferredSize(d2);

            getWaveData(track);
            image = new BufferedImage( (int) d2.getWidth(),
                    (int) d2.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            generateWaveform(track, d2, g2);

            xPos = (int) Math.round(d2.getWidth()/2);
            newXPos = xPos;

            java.util.Timer timer = new Timer();
            int delay = 0;
            int periodMS = 100;
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (stream.status == Status.PLAYING) {
                        float pixelsPerSecond = (float) (track.getDuration()/d2.getWidth());
                        float periodS = (float) periodMS/1000;
                        float pixelsPerUpdate = pixelsPerSecond * periodS;
                        xPos -= pixelsPerUpdate;
                        newXPos = Math.round(xPos);
                        repaint();
                    }
                    else if (stream.status == Status.WAITING) {
                        xPos = (int) Math.round(d2.getWidth()/2);
                        newXPos = xPos;
                        repaint();
                    }
                }
            }, delay, periodMS); //Delay, period between each TimerTask being executed
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g);

            g2.drawImage(
                    image,
                    null,
                    newXPos,
                    0
            );

            g2.drawLine(
                    (int)d2.getWidth()/2,
                    0,
                    (int) d2.getWidth()/2,
                    (int) d2.getHeight()
            );
        }
    }
}
