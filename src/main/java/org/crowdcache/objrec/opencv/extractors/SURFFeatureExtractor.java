package org.crowdcache.objrec.opencv.extractors;

import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

/**
 * Created by utsav on 2/6/16.
 */
public class SURFFeatureExtractor implements FeatureExtractor
{
    public KeypointDescList extract(Mat image)
    {
        //Keypoints
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        //Init detector
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
        // Read the settings file for detector
        detector.read(this.getClass().getClassLoader().getResource("surf_pars").getPath());
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        detector.read(this.getClass().getClassLoader().getResource("surf_pars").getPath());
        detector.detect(image, keypoints);
        extractor.compute(image, keypoints, descriptors);

        return new KeypointDescList(keypoints, descriptors);
    }

    public static void main(String args[])
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length > 0)
        {
            String inputFile = args[0];
            Mat image = Highgui.imread(inputFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            // run each example
            Long start = System.currentTimeMillis();
            KeypointDescList points = new SURFFeatureExtractor().extract(image);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Found:" + points.points.size());
        }

    }
}
