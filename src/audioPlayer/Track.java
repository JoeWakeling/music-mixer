package audioPlayer;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Track {
    private byte[] data;
    private String filePath, artLink, fileType, title, artist, durationStr, album, albumArtist, genre, year;
    private int trackID, fileSize, numChannels, sampleRate, bitRate, sampleSize, numFrames, numSamples, bpm, duration;


    public Track(String filePath, int trackID) throws IOException {
        this.filePath = filePath;
        this.trackID = trackID; // for instantiating new track
        parseData(this);
    }

    private void parseData(Track track) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(track.getFilePath()));
        byte[] data = new byte[dis.available()];
        dis.readFully(data);

        track.setData(data);

        int chunkStart;    // byte[] pos of chunk start
        int targetDistance;    //difference in byte[] pos between chunk start and target field start
        byte[] newIntByteArr = new byte[4];
        ByteBuffer newIntByteBuffer;

        //SUB-CHUNK: FORMAT
        chunkStart = search(data, "fmt ");

        targetDistance = 10;    // NUMCHANNELS short (distance from chunkStart)
        int numChannels = data[chunkStart + targetDistance];
        track.setNumChannels(numChannels);

        targetDistance = 12;    // SAMPLERATE int (distance from chunkStart)
        for (byte i = 0; i < 4; i++) {
            newIntByteArr[i] = data[chunkStart + targetDistance + i];
        }
        newIntByteBuffer = ByteBuffer.wrap(newIntByteArr);    //Combines 4 bytes into int (little endian like in wav)
        newIntByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int sampleRate = newIntByteBuffer.getInt();
        track.setSampleRate(sampleRate);

        targetDistance = 16;    // BYTERATE int (distance from chunkStart)
        for (byte i = 0; i < 4; i++) {
            newIntByteArr[i] = data[chunkStart + targetDistance + i];
        }
        newIntByteBuffer = ByteBuffer.wrap(newIntByteArr);    //Combines 4 bytes into int (little endian like in wav)
        newIntByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int byteRate = newIntByteBuffer.getInt();
        int bitRate = byteRate * 8; //byterate -> bitrate
        track.setBitRate(bitRate);

        //SUB-CHUNK: DATA
        chunkStart = search(data, "data");

        targetDistance = 4;    // Distance from start of data header to start of CHUNK SIZE (file size)
        for (byte i = 0; i < 4; i++) {
            newIntByteArr[i] = data[chunkStart + targetDistance + i];
        }
        newIntByteBuffer = ByteBuffer.wrap(newIntByteArr).order(ByteOrder.LITTLE_ENDIAN);    //Combines 4 bytes into int (little endian like in wav)
        newIntByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int fileSize = newIntByteBuffer.getInt();
        track.setFileSize(fileSize);

        //Calculates SAMPLESIZE from SAMPLERATE, BITRATE, NUMCHANNELS
        int sampleSize = bitRate / (sampleRate * numChannels);
        track.setSampleSize(sampleSize);

        //Calculates DURATION in seconds from FILESIZE and BYTERATE
        int duration = (fileSize / byteRate);
        track.setDuration(duration);

        int mins = duration / 60;
        mins = (int) Math.floor(mins);
        int secs = duration % 60;
        String middle;
        if (Integer.toString(secs).length() == 1) {
            middle = ":0";
        } else {
            middle = ":";
        }
        String durationStr = mins + middle + secs;
        track.setDurationStr(durationStr);

        int numFrames = duration * sampleRate;
        track.setNumFrames(numFrames);

        int numSamples = numFrames * numChannels;
        track.setNumSamples(numSamples);

        //Sets song title, artist
        String fileName = new File(track.getFilePath()).getName();

        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == ' ' && fileName.charAt(i + 1) == '-') {
                String artist = fileName.substring(0, i);
                track.setArtist(artist);

                for (int j = i; j < fileName.length(); j++) {
                    if (fileName.charAt(j) == '.') {
                        String title = fileName.substring(i+3, j);
                        track.setTitle(title);
                    }
                }
            }
        }

        String albumArtUrl;
        albumArtUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getinfo&api_key="
                + "b5202b17de9633edcdad45faa8f6bffb" //API KEY GOES HERE
                + "&artist=" + track.getArtist().replaceAll("&", "%26")
                + "&track=" + track.getTitle().replaceAll("&", "%26");
        albumArtUrl = albumArtUrl.replaceAll(" ", "%20");

        System.out.println(albumArtUrl);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(albumArtUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            BufferedReader bufReader = new BufferedReader(new StringReader(response.body()));
            String line;
            while ( (line=bufReader.readLine()) != null ) {
                String startTag = "<image size=\"extralarge\">";
                String endTag = "</image>";
                if (line.contains(startTag)) {
                    String artLink = line.substring(startTag.length(), line.length()-endTag.length());
                    track.setArtLink(artLink);
                }
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    int search(byte[] src, String pattern) {
        byte[] patternChars = new byte[pattern.length()];
        for (int i=0; i < pattern.length(); i++) {
            patternChars[i] = (byte) pattern.charAt(i);
        }
        int c = src.length - patternChars.length + 1;
        int k;
        for (int j = 0; j < c; j++)
        {
            if (src[j] != patternChars[0]) continue;
            for (k = patternChars.length - 1; k >= 1 && src[j + k] == patternChars[k]; k--) {
            }
            if (k == 0) return j;
        }

        return -1;
    }

    public void setData(byte[] data) { this.data = data; }

    public void setArtLink(String artLink) {
        this.artLink = artLink;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDurationStr(String durationStr) {
        this.durationStr = durationStr;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setNumChannels(int numChannels) {
        this.numChannels = numChannels;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public void setNumFrames(int numFrames) {
        this.numFrames = numFrames;
    }

    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public byte[] getData() {
        return data;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getArtLink() {
        return artLink;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDurationStr() {
        return durationStr;
    }

    public int getTrackID() {
        return trackID;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public int getDuration() {
        return duration;
    }
}
