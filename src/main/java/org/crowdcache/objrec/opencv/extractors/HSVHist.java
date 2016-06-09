package org.crowdcache.objrec.opencv.extractors;

import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 2/22/16.
 */
public class HSVHist extends FeatureExtractor
{
    /**
     * Extract features from an image
     *
     * @param image A {@link Mat} representing the image
     * @return {@link KeypointDescList} containing the keypoints and descriptors
     */
    @Override
    public KeypointDescList extract(Mat image)
    {
        Long start = System.currentTimeMillis();
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
        List<Mat> imagel = new ArrayList<>();
        imagel.add(hsv);

        MatOfInt channels = new MatOfInt(0, 1);
        MatOfInt size = new MatOfInt(30, 32);
        MatOfFloat ranges = new MatOfFloat( 0f,180f,0f,256f );

        Mat hist = new Mat();

        Imgproc.calcHist(imagel, channels, new Mat(), hist, size, ranges);
        Mat histbyte = new MatOfByte();
        Core.normalize(hist, histbyte, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
        histbyte = histbyte.reshape(1, 1);
        System.out.println("Hist time:" + (System.currentTimeMillis() - start));
        return new KeypointDescList(new MatOfKeyPoint(), histbyte);
    }
}
