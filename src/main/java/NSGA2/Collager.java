package NSGA2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Collager {
    private int origPicWidth = 1000;
    private int origPicHeight = 800;
    private int collageWidth = (int) (origPicWidth * 2.4);
    private int collageHeight = (int) (origPicHeight * 2.4);
    private final String PIC_DIR = "D:/COMSOL Research/33) 32 parameters, edited evaluate() method/plots/";
    private int numPicsPerRow = 3;
    private int picWidth;
    private int picHeight;
    private int numPics;

    public static void main(String[] args) throws IOException {
        int x = 0;
        int y = 0;
        int longestLength = 0;
        Collager c = new Collager();
        File dir = new File(c.PIC_DIR);

        File collageFile = new File(c.PIC_DIR + "collage.png");
        if (collageFile.exists()) {
            collageFile.delete();
        }

        File[] directoryListing = dir.listFiles();

        for (File file : directoryListing) {
            String filename = file.getName();
            int length = filename.length();
            if (longestLength < length) {
                longestLength = length;
            }
            if (length < longestLength) {
                String leadingZeros = "";
                while (length < longestLength) {
                    leadingZeros += "0";
                    length++;
                }
                File newFile = new File(c.PIC_DIR + leadingZeros + filename);
                file.renameTo(newFile);
            }
        }

        directoryListing = dir.listFiles();
        Arrays.sort(directoryListing);
        c.numPics = directoryListing.length;
        while ((c.numPicsPerRow * c.numPicsPerRow) < c.numPics) {
            c.numPicsPerRow++;
        }
//        System.out.println(c.numPicsPerRow);
        c.picWidth = c.collageWidth/c.numPicsPerRow;
        c.picHeight = c.collageHeight/c.numPicsPerRow;
//        System.out.println("Unit Picture width: " + c.picWidth);
//        System.out.println("Unit Picture height: " + c.picHeight);

        BufferedImage result = new BufferedImage(c.collageWidth, c.collageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = result.getGraphics();

        for (File item : directoryListing) {
            BufferedImage pic = c.resizeImage(ImageIO.read(item),c.picWidth, c.picHeight);
            int[] pixels = ((DataBufferInt) pic.getRaster().getDataBuffer()).getData();
//            System.out.println(item.getName());
//            System.out.println(x);
//            System.out.println(y);
            g.drawImage(pic, x, y, null);
            x += c.picWidth;
            if(x > result.getWidth() - c.picWidth){
                x = 0;
                y += c.picHeight;
            }
        }

        ImageIO.write(result,"png",collageFile);
    }
    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
