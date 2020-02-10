import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVNativeLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void drawString(PDDocument file,int x,int y,int w,int h) throws IOException {
        PDDocument pdDocument = file;
        float width=0;
        float height=0;
        if (pdDocument.getNumberOfPages() > 2) {
            PDPage pdPage = pdDocument.getPage(1);
            width=pdPage.getMediaBox().getWidth();
            height=pdPage.getMediaBox().getHeight();
            pdDocument.close();
            System.out.println(width+"      "+height);
        }
        PDFRenderer renderer = new PDFRenderer(pdDocument);

        for(int i=0;i<pdDocument.getNumberOfPages();i++){
            BufferedImage image = renderer.renderImage(i, 2.5f);
            int imageWidth=image.getWidth();
            int imageHeight=image.getHeight();
            Graphics g = image.getGraphics();
            int ratio= (int) (width/imageWidth);
            g.setColor(Color.RED);//画笔颜色
            g.drawRect(x/ratio, y/ratio, w/ratio, h/ratio);
            FileOutputStream out = new FileOutputStream("d:\\test"+i+"png");//输出图片的地址
            ImageIO.write(image, "png", out);

        }
    }
    public static void main(String[] args) throws IOException {
        System.out.println("ssssssssssssssssss");
        System.out.println(System.getProperty("java.library.path"));
//        讲pdf转成图片
        File file = new File("D:\\LDA\\qwe");
        File[] fs = file.listFiles();
        for (File f : fs) {
            System.out.println(f.getName());
            pdf2Image(f.getAbsolutePath(), "D:\\LDA\\LDAdata\\temp\\png", 250);

//        显示裁切的框
            int index = f.getName().indexOf(".");
            String str = f.getName().substring(0, index);
            Map map = showTextRegion("D:\\LDA\\LDAdata\\temp\\png\\", str + "_1.png");
            PDDocument pdDocument=PDDocument.load(f);
            PDPage pdPage = pdDocument.getPage(0);
            float width=pdPage.getMediaBox().getWidth();
            float height=pdPage.getMediaBox().getHeight();
            float ratio= (int)map.get("width")/width;
//            drawString(pdDocument,0,((int)map.get("top")/ratio),1000,((int)map.get("bottom")-(int)map.get("top"))/ratio);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            for(int i=0;i<pdDocument.getNumberOfPages();i++){
                BufferedImage image = renderer.renderImage(i, 3f);
                int imageWidth=image.getWidth();
                int imageHeight=image.getHeight();
                Graphics g = image.getGraphics();
                float r= width/imageWidth;
                g.setColor(Color.RED);//画笔颜色
                g.drawRect(0, (int)((int)map.get("top")/ratio/r), imageWidth, (int)(((int)map.get("bottom")-(int)map.get("top"))/ratio/r));
                FileOutputStream out = new FileOutputStream("d:\\test"+i+".png");//输出图片的地址
                ImageIO.write(image, "png", out);

            }
//            System.out.println(map.toString());
        }
        //裁切
//        clipSaveText("D:\\LDA\\LDAdata\\temp\\pdf\\","8.png");
    }

    public static void clearNoiseContours(List<MatOfPoint> contours, int area) {
        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()) {
            MatOfPoint matOfPoint = iterator.next();
            MatOfPoint2f mat2f = new MatOfPoint2f();
            matOfPoint.convertTo(mat2f, CvType.CV_32FC1);
            RotatedRect rect = Imgproc.minAreaRect(mat2f);
            if (rect.boundingRect().area() < area) {
                iterator.remove();
            }
            ;
        }
    }

    public static void showImg(String path) {
        Mat image = Imgcodecs.imread(path);
        showImg(image);
    }

    public static void showImg(Mat mat) {
        ImageViewer imageViewer = new ImageViewer(mat, "hello");
        imageViewer.imshow();
    }

//    public static void clipSaveText(String path,String filename) {
//        Mat image = Imgcodecs.imread(path+filename);
//        Mat visualImage = image.clone();
//        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
//        //二值
//        double[] data = image.get(0, 0);
//        int thres = (int)(data[0] - 5);
//        Imgproc.threshold(image, image, thres, 255, Imgproc.THRESH_BINARY);
//        //反色
//        Core.bitwise_not(image, image);
//        image = dilate(image,30);
//        //showImg(image);
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//        System.out.println(contours.size());
//        clearNoiseContours(contours,50000);
//        for (int i = 0; i < contours.size(); i++) {
//            Rect rect = Imgproc.boundingRect(contours.get(i));
//            Mat mat = visualImage.submat(rect);
//            Imgcodecs.imwrite(path+filename+"_clip"+i+".png", mat);
//        }
//    }


    //For test
    public static Map<String, Integer> showTextRegion(String path, String filename) {
        Mat image = Imgcodecs.imread(path + filename);
        Map<String, Integer> map = new HashMap<>();
        map.put("width", image.width());
        map.put("height", image.height());
        System.out.println(image.width() + "  " + image.height());
        Mat visualImage = image.clone();
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        //二值
        double[] data = image.get(0, 0);
        int thres = (int) (data[0] - 5);
        Imgproc.threshold(image, image, thres, 255, Imgproc.THRESH_BINARY);
        //反色
        Core.bitwise_not(image, image);
        image = dilate(image, 30);
        //showImg(image);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println(contours.size());
        clearNoiseContours(contours, 50000);
        int top = image.height();
        int bottom = 0;
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if (rect.height > 160) {
                Imgproc.rectangle(visualImage, rect.tl(), rect.br(), new Scalar(0, 0, 255), 3);
                System.out.println("x:" + rect.x + "y:" + rect.y + "width:" + rect.width + "height" + rect.height);
                if (rect.y + rect.height > bottom)
                    bottom = rect.y + rect.height;
                if (rect.y < top)
                    top = rect.y;
            }
        }
        map.put("top", top);
        map.put("bottom", bottom);
//        Mat testImage = new Mat(visualImage, Imgproc.boundingRect(contours.get(1)));
        showImg(visualImage);
        return map;
    }

    /**
     * 模糊处理
     *
     * @param mat
     * @return
     */
    public static Mat blur(Mat mat) {
        Mat blur = new Mat();
        Imgproc.blur(mat, blur, new Size(5, 5));
        return blur;
    }

    /**
     * 膨胀
     *
     * @param mat
     * @return
     */
    public static Mat dilate(Mat mat, int size) {
        Mat dilate = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size, size));
        //膨胀
        Imgproc.dilate(mat, dilate, element, new Point(-1, -1), 1);
        return dilate;
    }

    /**
     * 腐蚀
     *
     * @param mat
     * @return
     */
    public static Mat erode(Mat mat, int size) {
        Mat erode = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size, size));
        //腐蚀
        Imgproc.erode(mat, erode, element, new Point(-1, -1), 1);
        return erode;
    }


    public static String pdf2Image(String PdfFilePath, String dstImgFolder, int dpi) {
        File file = new File(PdfFilePath);
        PDDocument pdDocument;
        try {
            String imgPDFPath = file.getParent();
            int dot = file.getName().lastIndexOf('.');
            String imagePDFName = file.getName().substring(0, dot); // 获取图片文件名
            String imgFolderPath = dstImgFolder;

            pdDocument = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            /* dpi越大转换后越清晰，相对转换速度越慢 */
            StringBuffer imgFilePath = null;
//            for (int i = 0; i < pdDocument.getNumberOfPages(); i++) {
            if (pdDocument.getNumberOfPages() > 2) {
                Integer i = 1;
                System.out.println("正在转换第" + i + "页");
                String imgFilePathPrefix = imgFolderPath + File.separator + imagePDFName;
                imgFilePath = new StringBuffer();
                imgFilePath.append(imgFilePathPrefix);
                imgFilePath.append("_");
                imgFilePath.append(i);
                imgFilePath.append(".png");
                File dstFile = new File(imgFilePath.toString());
                BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                ImageIO.write(image, "png", dstFile);
                System.out.println("第" + i + "页转换完成");
                System.out.println("PDF文档转PNG图片成功！");
                return imgFilePath.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


}
