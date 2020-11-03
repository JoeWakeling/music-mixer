package audioPlayer.ui;

import audioPlayer.Engine;
import audioPlayer.Track;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ControlPanel extends JPanel {
    Track track;
    Engine stream;
    JTextField titleText;
    JLabel artLabel;
    ControlWavePanel controlWavePanel;
    GridBagConstraints c;

    public ControlPanel(Track newTrack, Engine newStream) throws MalformedURLException {
        track = newTrack;
        stream = newStream;
        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(500, 600));

        //Creates WaveformPanel object
        controlWavePanel = new ControlWavePanel(track, stream);

        //Create button components
        JButton playBtn = new JButton("▶");
        JButton stopBtn = new JButton("◻");
        JButton muteBtn = new JButton("MUTE");

        //Adds ActionListeners to each button to trigger Engine procedures when clicked
        playBtn.addActionListener(actionEvent -> stream.playPause());
        stopBtn.addActionListener(actionEvent -> {
            try {
                stream.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        muteBtn.addActionListener(actionEvent -> stream.toggleMute());

        //Adds buttons to JPanel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playBtn);
        buttonPanel.add(stopBtn);
        buttonPanel.add(muteBtn);

        //Create song title JTextField component
        titleText = new JTextField(track.getTitle());

        //Load image from cover art folder
        artLabel = getArt();

        //Adds waveformpanel using GBL constraints
        c = new GridBagConstraints();
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        add(controlWavePanel, c);

        //Add button panel to JPanel using GBL constraints
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        add(buttonPanel, c);

        //Add cover art to JPanel using GBL constraints
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10,5,10,5);
        c.weightx = 0.5;
        c.gridwidth = 3;
        c.gridheight = 3;
        c.gridx = 0;
        c.gridy = 2;
        add(artLabel, c);

        // Add song name to JPanel using GBL constraints
        c = new GridBagConstraints();
        c.insets = new Insets(0,5,0,5);
        c.weightx = 0.5;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 5;
        add(titleText, c);

    }

    public JLabel getArt() throws MalformedURLException {
        Image artImg;
        if (track.getArtLink() == null || track.getArtLink().equals("")) {
            System.out.println(track.getArtLink());
            artImg = new ImageIcon("/Users/josephwakeling/Library/Mobile Documents/com~apple~CloudDocs/Desktop/PersonalProject/src/assets/notrack.png").getImage();
        } else {
            artImg = new ImageIcon(new URL(track.getArtLink())).getImage();
        }
        artImg = artImg.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
        artLabel = new JLabel("", new ImageIcon(artImg), JLabel.CENTER);
        return artLabel;
    }

    public void newTrack(Track newTrack, Engine newStream) throws MalformedURLException {
        track = newTrack;
        stream = newStream;

        remove(controlWavePanel);
        controlWavePanel = new ControlWavePanel(track, stream);
        c = new GridBagConstraints();
        c.insets = new Insets(0,5,0,5);
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        add(controlWavePanel,c);

        remove(artLabel);
        artLabel = getArt();
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10,5,10,5);
        c.weightx = 0.5;
        c.gridwidth = 3;
        c.gridheight = 3;
        c.gridx = 0;
        c.gridy = 2;
        add(artLabel, c);

        remove(titleText);
        titleText = new JTextField(track.getTitle());
        c = new GridBagConstraints();
        c.insets = new Insets(0,5,0,5);
        c.weightx = 0.5;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 5;
        add(titleText,c);

        revalidate();
        repaint();
    }
}
