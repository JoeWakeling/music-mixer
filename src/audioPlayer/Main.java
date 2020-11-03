package audioPlayer;

import audioPlayer.ui.ControlPanel;
import audioPlayer.ui.LibraryPanel;
import audioPlayer.ui.MixPanel;
import audioPlayer.ui.VolumePanel;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main extends JFrame {
    static String filePath1, filePath2;
    static Track track1, track2;
    static Engine stream1, stream2;
    static MixPanel mixPanel;
    static ControlPanel controlPanel1, controlPanel2;
    static VolumePanel volumePanel;

    private Main() throws IOException, CsvValidationException {
        GridBagConstraints c;
        setLayout(new GridBagLayout());
        setSize(1920, 1080);
        setTitle("Audio player");
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        filePath1 = "/Users/josephwakeling/Desktop/PersonalProject/src/tracks/Chase & Status - No Problem.wav";
        filePath2 = "/Users/josephwakeling/Desktop/PersonalProject/src/tracks/Francois & Louis Benton - Only Us.wav";


        LibraryPanel filesPanel = new LibraryPanel();

        track1 = filesPanel.importTrack(filePath1);
        track2 = filesPanel.importTrack(filePath2);

        stream1 = new Engine(track1);
        stream2 = new Engine(track2);

        mixPanel = new MixPanel(track1, track2, stream1, stream2);
        controlPanel1 = new ControlPanel(track1, stream1);
        controlPanel2 = new ControlPanel(track2, stream2);
        volumePanel = new VolumePanel(stream1, stream2);

        JButton importBtn1 = new JButton("IMPORT 1");
        importBtn1.addActionListener(actionEvent -> {
                try {
                    track1 = null;
                    stream1 = null;

                    track1 = filesPanel.importTrack();
                    stream1 = new Engine(track1);

                    mixPanel.newTrack1(track1, stream1);
                    controlPanel1.newTrack(track1, stream1);
                    volumePanel.newTrack(stream1, stream2);

                    revalidate();
                    repaint();

                } catch (IOException e) {
                    e.printStackTrace();
                }
        });

        JButton importBtn2 = new JButton("IMPORT 2");
        importBtn2.addActionListener(actionEvent -> {
            try {
                track2 = null;
                stream2 = null;

                track2 = filesPanel.importTrack();
                stream2 = new Engine(track2);

                mixPanel.newTrack2(track2, stream2);
                controlPanel2.newTrack(track2, stream2);
                volumePanel.newTrack(stream1,stream2);

                revalidate();
                repaint();


            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        c = new GridBagConstraints();
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        add(mixPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        add(controlPanel1, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        add(controlPanel2, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        add(volumePanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        add(importBtn1, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        add(importBtn2, c);

        c = new GridBagConstraints();
        c.insets = new Insets(20, 0, 0, 0);
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 3;
        add(filesPanel, c);

        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            new Main();
            System.out.println("UI Loaded");

        } catch (Exception audioPlayFail) {
            System.out.println("Error loading UI");
            audioPlayFail.printStackTrace();
        }
    }
}