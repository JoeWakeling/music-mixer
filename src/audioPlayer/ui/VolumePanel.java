package audioPlayer.ui;

import audioPlayer.Engine;

import javax.swing.*;
import java.awt.*;

public class VolumePanel extends JPanel {
    private final JSlider volSlider1, volSlider2, crossfadeSlider, panSlider;
    private Engine stream1, stream2;

    public VolumePanel(Engine newStream1, Engine newStream2) {

        stream1 = newStream1;
        stream2 = newStream2;

        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(250, 300));

        //Create faders for volumes, crossfade and pan slider
        volSlider1 = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        volSlider1.setMajorTickSpacing(10);
        volSlider1.setMinorTickSpacing(1);

        volSlider2 = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        volSlider2.setMajorTickSpacing(10);
        volSlider2.setMinorTickSpacing(1);

        crossfadeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        crossfadeSlider.setMajorTickSpacing(10);
        crossfadeSlider.setMinorTickSpacing(1);

        panSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        panSlider.setMajorTickSpacing(10);
        panSlider.setMinorTickSpacing(1);

        //Add listeners for slider functions: 100- makes it so volumes change inversely proportionate to each other
        volSlider1.addChangeListener(event -> stream1.setVol(volSlider1.getValue(), 100-crossfadeSlider.getValue()));
        volSlider2.addChangeListener(event -> stream2.setVol(volSlider2.getValue(), crossfadeSlider.getValue()));
        crossfadeSlider.addChangeListener(event -> {
                    stream1.setVol(volSlider1.getValue(), 100-crossfadeSlider.getValue());
                    stream2.setVol(volSlider2.getValue(), crossfadeSlider.getValue());
        });
        panSlider.addChangeListener(event -> {
            stream1.setPan(panSlider.getValue());
            stream2.setPan(panSlider.getValue());
        });

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(20,0,0,0);
        c.weightx = 0.5;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.gridx = 1;
        c.gridy = 0;
        add(volSlider1, c);

        c.gridx = 3;
        c.gridy = 0;
        add(volSlider2, c);

        c.insets = new Insets(25,100,25,100);
        c.gridwidth = 3;
        c.gridheight = 1;
        c.gridx = 1;
        c.gridy = 4;
        add(crossfadeSlider, c);

        c.gridx = 1;
        c.gridy = 6;
        add(panSlider, c);
    }

    public void newTrack(Engine stream1, Engine stream2) {
        this.stream1 = stream1;
        this.stream2 = stream2;

        stream1.setVol(volSlider1.getValue(), 100-crossfadeSlider.getValue());
        stream2.setVol(volSlider2.getValue(), crossfadeSlider.getValue());

        stream1.setPan(panSlider.getValue());
        stream2.setPan(panSlider.getValue());

        revalidate();
        repaint();

    }

}