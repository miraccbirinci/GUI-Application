package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    int imageWidth = 0;
    int imageHeight = 0;
    int pressed = 0;

    //Sobel Edge Detector arrays
    int[][] x = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    int[][] y = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};

    public static void main(String[] args) {

        Main main = new Main();
        main.apFinal();

        String[] filenames = {"circle2.jpeg", "hexagon.jpeg", "triangle.jpeg"};
        int[] cellSizes = {400, 8, 16, 32};

        for (int cellSize: cellSizes){
            System.out.println("------------CELL SIZE:"+cellSize+"-----------");
            double[] distances = new double[filenames.length];
            int index = 0;
            for (String filename: filenames){
                String filename1 = main.extractTypeFile("circle1.jpeg");
                int[][] image1 = main.readTypeFile(filename1);
                int[][] gx1 = main.getGDirection(main.imageWidth, main.imageHeight, image1, main.x);
                int[][] gy1 = main.getGDirection(main.imageWidth, main.imageHeight, image1, main.y);
                //int[][] g1 = main.getG(main.imageWidth, main.imageHeight, gx1, gy1);
                int[][] histogram1 = main.getHistogram(gx1, gy1, cellSize);

                String filename2 = main.extractTypeFile(filename);
                int[][] image2 = main.readTypeFile(filename2);
                int[][] gx2 = main.getGDirection(main.imageWidth, main.imageHeight, image2, main.x);
                int[][] gy2 = main.getGDirection(main.imageWidth, main.imageHeight, image2, main.y);
                //int[][] g2 = main.getG(main.imageWidth, main.imageHeight, gx2, gy2);
                int[][] histogram2 = main.getHistogram(gx2, gy2, cellSize);

                double distance = main.compareHistograms(histogram1, histogram2);
                System.out.println("Distance between "+filename+": "+distance);
                distances[index] = distance;
                index++;

            }

            double closest = -1;
            int closestIndex = -1;
            for (int i=0; i<distances.length; i++){
                if (closest >= 0){
                    if (closest > distances[i]){
                        closest = distances[i];
                        closestIndex = i;
                    }
                }else {
                    closest = distances[i];
                    closestIndex = i;
                }
            }

            System.out.println("The closest image with the distance: "+closest+", "+filenames[closestIndex]);
        }

    }



    public void apFinal(){


        String filename = extractTypeFile("circle.jpg");
        int[][] image = readTypeFile(filename);
        int[][] gx = getGDirection(imageWidth, imageHeight, image, x);
        int[][] gy = getGDirection(imageWidth, imageHeight, image, y);
        int[][] g = getG(imageWidth, imageHeight, gx, gy);
        int[][] histogram = getHistogram(gx, gy, 8);

        
        Point point1 = new Point(0,0);
        Point point2 = new Point(0,0);

        JTabbedPane pane = new JTabbedPane();

        Panel p0 = new Panel(image);
        pane.add("Original Image", p0);

        Panel p1 = new Panel(gx);
        pane.add("X Direction Edge", p1);

        Panel p2 = new Panel(gy);
        pane.add("Y Direction Edge", p2);

        Panel p3 = new Panel(g);
        pane.add("Edge Image", p3);

        HistogramPanel p4 = new HistogramPanel(histogram);
        pane.add("Histogram", p4);

        Panel p5 = resizeImage(point1, point2, image);
        pane.add("Resized", p5);

        JFrame frame = new JFrame();                        //JFrame oluşturuyoruz.
        frame.add(pane);                                    //JTabbedPane i ekliyoruz görsel adını vererek.

        frame.setVisible(true);
        frame.setSize(700,700);                       //frame in boyutlarını tanımlayıp görünür yapıyoruz.
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        p0.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pressed % 2 == 0){
                    point1.setLocation(e.getX(), e.getY());
                }else {
                    point2.setLocation(e.getX(), e.getY());
                    pane.removeTabAt(5);
                    pane.add("Resized",resizeImage(point1, point2, image));
                }
                pressed++;

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

    }


    public String extractTypeFile(String path) {
        BufferedImage img;
        String extractedFileName = "";
        try {
            img = ImageIO.read(new File(path));  //Verilen dosyayı okuyoruz. En basit yöntem bu
            this.imageWidth = img.getWidth();    //resmin genişliğini ve yüksekliğini alıyoruz.
            this.imageHeight = img.getHeight();
            String[] givenFileName = path.split("\\."); //Dosya adını '.' a göre ayırıyoruz ki dosyanın uzantısız
            //adını alabilelim
            extractedFileName = givenFileName[givenFileName.length - 2] + ".txt";
            //
            FileWriter writer = new FileWriter(extractedFileName);
            writer.write(2 + "\n");
            writer.write(imageWidth + " " + imageHeight + "\n");
            writer.write("255\n");

            for (int i = 0; i < imageHeight; i++) {
                for (int j = 0; j < imageWidth; j++) {
                    Color color = new Color(img.getRGB(j, i));

                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();
                    int colorValue = (int) ((0.3 * r) + (0.59 * g) + (0.11 * b));
                    writer.write(colorValue + " ");

                }
            }

            writer.flush();
            return extractedFileName;

        } catch (IOException ignored) {
        }
        return extractedFileName;
    }

    public int[][] readTypeFile(String path){

        try {
            Scanner scanner = new Scanner(new File(path));
            int type = scanner.nextInt();
            int width = scanner.nextInt();        //Resmin genişliğini alıyoruz
            int height = scanner.nextInt();       //Resmin yüksekliğini alıyoruz
            if (type != 1){
                scanner.nextInt();
            }
            int[][] imageColors = new int[height][width];
            /*if (type == 3){
                for(int i=0; i<height; i++) {
                    for(int j=0; j<width; j++) {
                        int r = scanner.nextInt();     //r değerini okuyoruz.
                        int g = scanner.nextInt();   //g değerini okuyoruz.
                        int b = scanner.nextInt();   //b değerini okuyoruz.
                        imageColors[i][j] = new Color(r, g, b);
                    }
                }
            }else if(type == 2){*/
                for(int i=0;i<height;i++) {                  //Image arrayini dolaşıyoruz.
                    for(int j=0;j<width;j++) {
                        int value = scanner.nextInt();
                        imageColors[i][j] = value;
                    }
                }/*
            }else{
                for(int i=0;i<height;i++) {
                    for(int j=0;j<width;j++) {
                        int value = scanner.nextInt();  //Her pikselin değerini sırasıyla okuyup image arrayinde tutuyoruz.
                        if (value == 1){        //Eğer o pikselin değeri 0 ise renk siyah değil ise beyaz.
                            imageColors[i][j] = Color.BLACK;
                        }else{
                            imageColors[i][j] = Color.WHITE;
                        }
                    }
                }
            }*/
            return imageColors;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    public int[][] getGDirection(int width, int height, int[][] gPixels, int[][] gradient) {

        int[][] gStore = new int[height][width];
        int[][] g = new int[height][width];
        int max = 0;

        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {

                int p00 = gPixels[i - 1][j - 1];
                int p01 = gPixels[i][j - 1];
                int p02 = gPixels[i + 1][j - 1];
                int p10 = gPixels[i - 1][j];
                int p11 = gPixels[i][j];
                int p12 = gPixels[i + 1][j];
                int p20 = gPixels[i - 1][j + 1];
                int p21 = gPixels[i][j + 1];
                int p22 = gPixels[i + 1][j + 1];

                int gradientY = ((gradient[0][0] * p00) + (gradient[0][1] * p01) + (gradient[0][2] * p02))
                        + ((gradient[1][0] * p10) + (gradient[1][1] * p11) + (gradient[1][2] * p12))
                        + ((gradient[2][0] * p20) + (gradient[2][1] * p21) + (gradient[2][2] * p22));
                    if (gradientY > max) {
                        max = gradientY;
                    }
                    gStore[i][j] = gradientY;
            }
        }
        float formula = (float) max / (float) 255;
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                g[i][j] = (int) Math.abs(gStore[i][j] / formula);
            }
        }

        return g;

    }

    public int[][] getG(int width, int height, int[][] gXPixels, int[][] gYPixels) {


        int max = 0;

        int[][] gStore  = new int[height][width];
        int[][] g = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gradientValue = (int) Math.sqrt(Math.pow(gXPixels[i][j], 2) + Math.pow(gYPixels[i][j], 2));
                if (gradientValue > max) {
                    max = gradientValue;
                }
                gStore [i][j] = gradientValue;
            }
        }

        float formula=(float)max/(float)255;
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                g[i][j]=(int)Math.abs(gStore [i][j]/formula);
            }
        }

        return g;
    }

    public int[][] getHistogram(int[][] gXPixels, int[][] gYPixels, int cellSize){

        int height = gXPixels.length;
        int width = gXPixels[0].length;

        int noCells = (height/cellSize) * (width/cellSize);

        int[][] histogram = new int[noCells][9];

        for (int i=0; i<noCells; i++){
            for (int a=0; a<height/(height/cellSize); a++){
                for (int b=0; b<width/(width/cellSize); b++){

                    int y = height/(width/cellSize) * (i/(width/cellSize)) + a;
                    int x = width/(height/cellSize) * (i%(height/cellSize)) + b;

                    double gxValue ;
                    if(gXPixels[y][x]==0){
                        gxValue=0.0000001;
                    }else{
                        gxValue=gXPixels[y][x];
                    }

                    int angle = (int) Math.toDegrees(Math.atan(gYPixels[y][x] / gxValue));
                    histogram[i][angle/20]++;
                }
            }
        }


        return histogram;
    }

    public Panel resizeImage(Point point1, Point point2, int[][] img){
        System.out.println(point1);
        System.out.println(point2);
        int[][] resizedImage = new int[(point2.y - point1.y) * 2][(point2.x - point1.x) * 2];
        for (int i=0; i<resizedImage.length; i=i+2){
            for (int j=0; j<resizedImage[0].length; j=j+2){
                resizedImage[i][j] = img[point1.y + i/2][point1.x + j/2];
                resizedImage[i][j + 1] = img[point1.y + i/2][point1.x + j/2];
                resizedImage[i + 1][j] = img[point1.y + i/2][point1.x + j/2];
                resizedImage[i + 1][j + 1] = img[point1.y + i/2][point1.x + j/2];
            }
        }
        return new Panel(resizedImage);
    }


    public double compareHistograms(int[][] his1, int[][] his2){
        double distance = 0;
        for (int i=0; i<his1.length; i++){
            for (int j=0; j<his1[0].length; j++){
                distance += Math.abs(his1[i][j] - his2[i][j]);
            }
        }
        return distance;
    }
}

class Panel extends JPanel {

    int[][] image;

    Panel(int[][] image){
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {  //JPanel e ait metot. Eklediğimiz panelin istediğimiz gibi boyamak için kullanıyoruz.
        super.paintComponent(g);
        for(int y=0;y<image.length;y++) {
            for(int x=0;x<image[0].length;x++) {
                g.setColor(new Color(image[y][x], image[y][x], image[y][x]));// Pikselin rengini alıp
                g.fillRect(x, y, 1, 1);//resimdeki yerini boyuyoruz.
            }
        }
    }

}

class HistogramPanel extends JPanel {// Java Swing in JPanel sınıfından extends ediyoruz

    int[][] histogram;

    HistogramPanel(int[][] histogram) {
        this.histogram = histogram;
    }

    @Override
    protected void paintComponent(Graphics g) {//JPanel e ait metot. Eklediğimiz panelin istediğimiz gibi boyamak için kullanıyoruz.
        super.paintComponent(g);
        for (int i=0;i<histogram.length;i++){
            for (int j=0; j<9; j++){

                int x = i * 9 + j;

                g.setColor(Color.BLACK);
                g.drawLine(x,400, x,400-(histogram[i][j]*8));

                if (j == 0){
                    g.setColor(Color.GREEN);
                    g.drawLine(x, 400, x, 400-60);
                }


            }
        }
    }

}
