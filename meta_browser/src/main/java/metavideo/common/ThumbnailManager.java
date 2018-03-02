package metavideo.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThumbnailManager {

    public static String exePath="D:\\workspace\\metavideo\\lib\\ffmpeg.exe";

    //같은 임시파일을 덮어쓰면서 시간만 얻는다.
    public static String getRunningTime(String videoFilePath){
        String duration="";
        try {
            String[] commands = { exePath, "-ss",
                    String.format("%02d:%02d:%02d", 0, 0, 1),
                    "-i", videoFilePath, "-an", "-vframes", "1", "-y",
                    "./temp.jpg" };

            Process processor = Runtime.getRuntime().exec(commands);

            String line1 = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    processor.getErrorStream()));
            while ((line1 = br.readLine()) != null) {
                if(line1.contains("Duration")) {
                    duration = line1.substring(12, 23);
                    System.out.println("duration: " + duration);
                }
            }
            processor.waitFor();
            int exitValue = processor.exitValue();
            if (exitValue != 0) {
                throw new RuntimeException("exit code is not 0 [" + exitValue+ "]");
            }
            //return creatingImageFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return duration;
    }



    //개선본(파일경로로 바로처리하고, 재생시간을 얻는다.)
    public static String createThumbnail(String videoFilePath, String imageFilePath){
        String duration="";
        try {

            String[] commands = { exePath, "-ss",
                    String.format("%02d:%02d:%02d", 0, 0, 1),
                    "-i", videoFilePath, "-an", "-vframes", "1", "-y",
                    imageFilePath };

            Process processor = Runtime.getRuntime().exec(commands);

            String line1 = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    processor.getErrorStream()));
            while ((line1 = br.readLine()) != null) {
                if(line1.contains("Duration")) {
                    duration = line1.substring(12, 23);
                    System.out.println("duration: " + duration);
                }
            }
            processor.waitFor();
            int exitValue = processor.exitValue();
            if (exitValue != 0) {
                //throw new RuntimeException("exit code is not 0 [" + exitValue+ "]");
                ;//return duration;
            }
            //return creatingImageFile;
        } catch (IOException e) {
            //throw new RuntimeException(e);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
        return duration;
    }



    public static void createThumbnailbk(String videoPath, String thumbnailPath){
        extractImage(new File(videoPath), 1, new File(thumbnailPath));
    }




    //원본소스
    public static File extractImage(File videoFile, int position,
                             File creatingImageFile) {
        try {
            int seconds = position % 60;
            int minutes = (position - seconds) / 60;
            int hours = (position - minutes * 60 - seconds) / 60 / 60;

            String videoFilePath = videoFile.getAbsolutePath();
            String imageFilePath = creatingImageFile.getAbsolutePath();

            String[] commands = { exePath, "-ss",
                    String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    "-i", videoFilePath, "-an", "-vframes", "1", "-y",
                    imageFilePath };

            Process processor = Runtime.getRuntime().exec(commands);

            String line1 = null;
            BufferedReader error = new BufferedReader(new InputStreamReader(
                    processor.getErrorStream()));
            while ((line1 = error.readLine()) != null) {
                System.out.println("line1: "+line1);
            }
            processor.waitFor();
            int exitValue = processor.exitValue();
            if (exitValue != 0) {
                throw new RuntimeException("exit code is not 0 [" + exitValue+ "]");
            }
            return creatingImageFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
