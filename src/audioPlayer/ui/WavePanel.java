package audioPlayer.ui;

import audioPlayer.Track;

import javax.swing.*;
import java.awt.*;

public class WavePanel extends JPanel {
    int[][] data;

    public void getWaveData(Track track) {
        //Creates 2D byte array to split data into 2 channels for waveform
        data = new int[track.getNumChannels()][track.getNumSamples()];
        int sampleIndex = 0;

        for (int i = 0; i < (track.getNumChannels() * track.getNumSamples()); ) {
            for (int channel = 0; channel < track.getNumChannels(); channel++) {
                int low = track.getData()[i];
                i++;
                int high = track.getData()[i];
                i++;
                int sample = (high << 8) + (low & 255); //left bitwise shift of 8, bitwise AND of 255
                data[channel][sampleIndex] = sample;
            }
            sampleIndex++;
        }
    }

    public void generateWaveform(Track track, Dimension d, Graphics2D g2) {
        setBackground(Color.LIGHT_GRAY);

        System.out.println(d.getWidth());
        double framesPerPixel = (float) track.getNumFrames() / d.getWidth(); //scales how many samples to take based on panel width

        for (int i = 0; i < 1; i++) {  //i <1 instead of 2 only loads left of stereo track in (loads faster w/ minimal loss of accuracy
            int xIndex = 0;

            //Iterates through frames in chunks of x frames per pixel
            for (int j = 0; j < data[i].length; j += framesPerPixel) {
                double maxAmplitude = 0;
                double minAmplitude = 0;
                double totalAmplitudeSqrd = 0;

                //Iterates through each chunk getting max amp, min amp and total amp^2
                for (int k = 0; k < framesPerPixel/2; k++) {
                    if ((j + k) < (data[i].length - framesPerPixel)) { //Prevents index out of range when looking for amplitude values
                        int currentAmplitude = data[i][j + k];

                        totalAmplitudeSqrd = totalAmplitudeSqrd + Math.pow(currentAmplitude, 2);

                        if (currentAmplitude > maxAmplitude) {
                            maxAmplitude = currentAmplitude;
                        } else if (currentAmplitude < minAmplitude) {
                            minAmplitude = currentAmplitude;
                        }
                    }
                }
                double rmsAmplitude = Math.sqrt(totalAmplitudeSqrd / framesPerPixel); //root mean squared of amplitude
                double yScaleFactor = Math.pow(2, 16) / (d.getHeight()); // 32760/50 (max value of 16 bit audio scales it down to-100 to +100
                maxAmplitude = maxAmplitude / yScaleFactor;
                minAmplitude = minAmplitude / yScaleFactor;
                rmsAmplitude = rmsAmplitude / yScaleFactor; //Sample scaled into correct Y values and mean taken

                int y, yPos, yNeg;
                int yMiddle = (int) (d.getHeight() / 2);

                g2.setColor(new Color(52, 106, 255));
                y = yMiddle + (int) maxAmplitude;
                g2.drawLine(xIndex, yMiddle, xIndex, y);

                y = yMiddle + (int) minAmplitude;
                g2.drawLine(xIndex, yMiddle, xIndex, y);

                g2.setColor(new Color(0, 225, 255));
                yPos = yMiddle + (int) (Math.abs(rmsAmplitude));
                yNeg = yMiddle - (int) (Math.abs(rmsAmplitude));
                g2.drawLine(xIndex, yMiddle, xIndex, yPos);
                g2.drawLine(xIndex, yMiddle, xIndex, yNeg);

                xIndex++;

            }
        }
    }
}
