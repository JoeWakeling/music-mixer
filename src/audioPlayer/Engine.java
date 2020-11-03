package audioPlayer;

import javax.sound.sampled.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Engine {
    SourceDataLine line;
    public Status status;
    FloatControl gainCtrl;
    FloatControl panCtrl;
    BooleanControl muteCtrl;
    float crossfade;
    int bufferSize;
    int fileLength;
    Track track;

    public Engine(Track track) {
        this.track = track;
        bufferSize = 4096;
        newTrack(track);
    }

    public void newTrack(Track track)  {
        AudioFormat format = new AudioFormat(track.getSampleRate(),
                track.getSampleSize(),
                track.getNumChannels(),
                true,
                false);

        SourceDataLine.Info info = new SourceDataLine.Info(SourceDataLine.class, format); // format is an AudioFormat object
        System.out.println(info);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line unsupported");
        }
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, bufferSize);
        } catch (LineUnavailableException e) {
            System.out.println("Line unavailable");
        }

        //Instantiates Float/BooleanControl objects
        gainCtrl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        panCtrl = (FloatControl) line.getControl(FloatControl.Type.PAN);
        muteCtrl = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
        //Sets default vars for Control objects
        gainCtrl.setValue((float) 0.5);
        panCtrl.setValue(0);
        muteCtrl.setValue(false);
        //More default vars
        crossfade = 1;
        status = Status.WAITING;

        audioToLine(0);
    }

    public void audioToLine(int startPos) {
        Thread audioOutThread = new Thread(() -> {
            FileInputStream rawFile = null;
            final int finalStartPos = 4*(Math.round((float)startPos/4));
            try {
                rawFile = new FileInputStream(track.getFilePath());
                fileLength = rawFile.available();
                long skippedBytes = rawFile.skip(finalStartPos);
                if (skippedBytes < finalStartPos) {
                    System.out.println("Skip length exceeds song length");
                }
                System.out.println(finalStartPos);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] data = new byte[bufferSize];

            Timer timer = new Timer();
            int delay = 0;
            int period = 10;

            FileInputStream finalRawFile = rawFile;
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        if (status == Status.STOPPED) {
                            line.drain();
                            line.stop();
                            timer.cancel();
                            timer.purge();
                        } else if (status == Status.PLAYING) {
                            if ((finalRawFile.read(data, 0, bufferSize)) != -1) {
                                line.write(processData(data), 0, bufferSize);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, delay, period);
        });
        audioOutThread.start();
    }

    public byte[] processData(byte[] dataBytes){
        //Todo: data processing
        return dataBytes;
    }

    public void playPause() {
        if (status == Status.PLAYING) {
            line.stop();
            setStatus(Status.PAUSED);
        } else {
            line.start();
            setStatus(Status.PLAYING);
        }
    }

    public void stop() throws InterruptedException {
        if (status == Status.PLAYING || status == Status.PAUSED ) {
            setStatus(Status.STOPPED);
            Thread.sleep(40);
            setStatus(Status.WAITING);
            audioToLine(0);
        }
    }

    public void changePos(float progress) throws InterruptedException {
        int startPos = Math.round(progress*fileLength);
        Status tempStatus = status;
        setStatus(Status.STOPPED);
        Thread.sleep(100); //40ms causes sound only to play after pausing and unpausing
        setStatus(tempStatus);
        line.start();
        audioToLine(startPos);
    }

    public void setVol(float vol, float crossfade) {
        //Todo: options for different crossfade curves
        vol = vol / 100; //changes vol values from 0 to 100 -> 0 to 1
        this.crossfade = crossfade / 100; //changes crossfade values from 0 to 100 -> 0 to 1
        vol = vol * this.crossfade; //Adds crossfade factor to volume
        float gain = (float) (Math.log(vol) / Math.log(10.0) * 20.0); //Converts volume to gain
        gainCtrl.setValue(gain);
    }

    public void setPan(float pan) {
        panCtrl.setValue((pan / 50) - 1); //changes pan values from 0 to 100 -> -1 to 1
    }

    public void toggleMute() {
        muteCtrl.setValue(!muteCtrl.getValue());
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
