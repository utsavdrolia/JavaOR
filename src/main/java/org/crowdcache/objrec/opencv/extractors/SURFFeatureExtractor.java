package org.crowdcache.objrec.opencv.extractors;

import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by utsav on 2/6/16.
 */
public class SURFFeatureExtractor extends FeatureExtractor
{
    public SURFFeatureExtractor()
    {
        //Init detector
        detector = FeatureDetector.create(FeatureDetector.SURF);
        extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
    }

    /**
     *
     * @param pars Path to parameters for SURF
     */
    public SURFFeatureExtractor(String pars)
    {
        //Init detector
        detector = FeatureDetector.create(FeatureDetector.SURF);
        // Read the settings file for detector
        detector.read(pars);
        extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
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
