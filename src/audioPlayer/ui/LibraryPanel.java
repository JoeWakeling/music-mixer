package audioPlayer.ui;

import audioPlayer.Track;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import dependencies.SelectButtonGroup;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class LibraryPanel extends JPanel {
    static int nextTrackID = 0;
    static String[]  importedFilepaths = new String[]{};
    static SelectButtonGroup selectBtnGrp = new SelectButtonGroup();
    static int[] selectBtnClickCountArr = new int[]{};
    static JPanel filesPanel = new JPanel();

    public LibraryPanel() throws IOException, CsvValidationException {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(1200, 135));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(1200, 120));

        filesPanel.setLayout(new GridLayout(0,1));
        filesPanel.setPreferredSize(new Dimension(800, 105));
        filesPanel.setBackground(new Color(200, 200, 200));

        CSVReader csvReader = new CSVReader(new FileReader("assets/data.csv"));
        String[] nextRecord;
        while ((nextRecord = csvReader.readNext()) != null) {
            nextTrackID ++;

            JPanel csvRow = new JPanel();
            csvRow.setPreferredSize(new Dimension(800, 15));
            csvRow.setLayout(new GridLayout(0, 5));
            csvRow.setBackground(new Color(200, 200, 200));

            JRadioButton selectBtn = new JRadioButton();
            selectBtnGrp.add(selectBtn);
            selectBtnClickCountArr = ArrayUtils.add(selectBtnClickCountArr, 0);

            selectBtn.addActionListener(actionEvent -> {
                if (selectBtn.isSelected()) {
                    int btnIndex = selectBtnGrp.getSelectedIndex();
                    if (++selectBtnClickCountArr[btnIndex] % 2 == 0) {
                        selectBtnGrp.clearSelection();
                    }
                }
            });
            importedFilepaths = ArrayUtils.add(importedFilepaths, nextRecord[4]);

            csvRow.add(selectBtn);
            for (int i = 0; i < nextRecord.length - 1; i++) {
                csvRow.add(new JTextField(nextRecord[i]));
            }
            filesPanel.add(csvRow);
        }

        JPanel headerRow = new JPanel();
        headerRow.setBackground(Color.GRAY);
        headerRow.setPreferredSize(new Dimension(1200, 15));
        headerRow.setLayout(new GridLayout(0, 5));
        headerRow.add(new JTextField("SELECT"));
        headerRow.add(new JTextField("TRACK ID"));
        headerRow.add(new JTextField("TITLE"));
        headerRow.add(new JTextField("ARTIST"));
        headerRow.add(new JTextField("DURATION"));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(headerRow, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        scrollPane.setViewportView(filesPanel);
        add(scrollPane, c);



    }


    public Track importTrack() throws IOException {
        String filepath = "";
        if (selectBtnGrp.getSelectedIndex() == -1) {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                filepath = file.getAbsolutePath();
            }
        } else {
            filepath = importedFilepaths[selectBtnGrp.getSelectedIndex()];
        }

        Track newTrack = new Track(filepath, nextTrackID);
        if (!Arrays.asList(importedFilepaths).contains(filepath)) {
            nextTrackID++;

            CSVWriter csvWriter = new CSVWriter(new FileWriter("assets/data.csv", true));
            String[] record = {
                    Integer.toString(newTrack.getTrackID()),
                    newTrack.getTitle(),
                    newTrack.getArtist(),
                    newTrack.getDurationStr(),
                    newTrack.getFilePath()
            };
            csvWriter.writeNext(record);
            csvWriter.flush();
            csvWriter.close();
            filesPanel.add(new RowPanel(newTrack));
        }
        return newTrack;
    }

    public Track importTrack(String filepath) throws IOException {
        //Manually imports tracks with filepath hardcoded.
        return new Track(filepath, nextTrackID);
    }

    public static class RowPanel extends JPanel {
        public RowPanel(Track track) {
            setPreferredSize(new Dimension(800, 15));
            setLayout(new GridLayout(1, 5));
            setBackground(new Color(200, 200, 200));

            JRadioButton selectBtn = new JRadioButton();
            selectBtnGrp.add(selectBtn);
            selectBtnClickCountArr = ArrayUtils.add(selectBtnClickCountArr, 0);
            selectBtn.addActionListener(actionEvent -> {
                if (selectBtn.isSelected()) {
                    int btnIndex = selectBtnGrp.getSelectedIndex();
                    if (++selectBtnClickCountArr[btnIndex] % 2 == 0) {
                        selectBtnGrp.clearSelection();
                    }
                }
            });
            JTextField trackIDText = new JTextField(Integer.toString(track.getTrackID()));
            JTextField titleText = new JTextField(track.getTitle());
            JTextField artistText = new JTextField(track.getArtist());
            JTextField durationText = new JTextField(track.getDurationStr());

            add(selectBtn);
            add(trackIDText);
            add(titleText);
            add(artistText);
            add(durationText);
        }

    }
}


