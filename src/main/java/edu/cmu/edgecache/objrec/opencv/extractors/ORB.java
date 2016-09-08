package edu.cmu.edgecache.objrec.opencv.extractors;

import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

/**
 * Created by utsav on 2/8/16.
 */
public class ORB extends FeatureExtractor
{
    public ORB()
    {
        //Init detector
        detector = FeatureDetector.create(FeatureDetector.ORB);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    }

    /**
     *
     * @param pars Path to parameters for ORB
     */
    public ORB(String pars)
    {
        //Init detector
        detector = FeatureDetector.create(FeatureDetector.ORB);
        // Read the settings file for detector
        detector.read(pars);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
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
            KeypointDescList points = new ORB().extract(image);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Found:" + points.points.size());
        }
    }
}
