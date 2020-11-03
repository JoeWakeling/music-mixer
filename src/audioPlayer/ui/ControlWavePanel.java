package audioPlayer.ui;

import audioPlayer.Engine;
import audioPlayer.Status;
import audioPlayer.Track;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class ControlWavePanel extends JPanel {
    static Track track;
    static Engine stream;
    static Dimension d = new Dimension(400, 100);
    CursorPanel cursorPanel;
    WaveformPanel waveformPanel;
    double cursorPos;
    double newCursorPos;

    public ControlWavePanel(Track track, Engine stream) {
        setPreferredSize(d);
        setLayout(new OverlayLayout(this));

        ControlWavePanel.track = track;
        ControlWavePanel.stream = stream;

        cursorPanel = new CursorPanel();

        cursorPos = 0;
        newCursorPos = 0;

        Timer timer = new Timer();

        double pixelsPerSecond = d.getWidth() / track.getDuration();
        int updatesPerSecond = 1; //More updates = more laggy UI, smoother cursor
        double pixelJump = pixelsPerSecond / updatesPerSecond;
        int jumpTime = (int) Math.round(1000 * (1.0 / updatesPerSecond)); //Time (in ms) between each time the cursor moves

        int delay = 0; //0ms delay of cursor playing after track plays

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (stream.status == Status.WAITING) {
                    cursorPos = 0;
                    newCursorPos = 0;
                    cursorPanel.repaint();
                } else if (stream.status == Status.PLAYING) {
                    cursorPos = cursorPos + pixelJump;
                    newCursorPos = Math.round(cursorPos);
                }
            }
        }, delay, jumpTime); //Delay, period between each TimerTask being executed

        waveformPanel = new WaveformPanel();

        add(cursorPanel);
        add(waveformPanel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mE) {
                int xPos = mE.getX();
                float progress = (float) xPos / getWidth(); //fraction how far through song is clicked
                try {
                    stream.changePos(progress);
                    changePos(xPos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void changePos(int xPos) {
        cursorPos = xPos;
    }

    class CursorPanel extends JPanel {

        public CursorPanel() {
            setPreferredSize(d);
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine((int) newCursorPos, 0, (int) newCursorPos, getHeight());

            int[] xPoints = new int[]{
                    (int) (newCursorPos - 5),
                    (int) newCursorPos,
                    (int) (newCursorPos + 5)
            };

            int[] yPoints = new int[]{
                    (int) d.getHeight(),
                    (int) (d.getHeight() - 10),
                    (int) d.getHeight()
            };
            g2.fillPolygon(xPoints, yPoints, 3);
            System.out.println("Cursor repainted");
        }
    }

    static class WaveformPanel extends WavePanel {
        public WaveformPanel() {
            getWaveData(track);
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);
            //Todo: fix controlwave repainting every time cursor does
            generateWaveform(track, d, g2);
            System.out.println("ControlWave repainted");
        }
    }
}