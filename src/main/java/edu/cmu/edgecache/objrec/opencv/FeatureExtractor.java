package edu.cmu.edgecache.objrec.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Interface for Extracting features from given image and returning a datastructure that is used by the Associator
 * Created by utsav on 2/5/16.
 */
public abstract class FeatureExtractor
{
    protected FeatureDetector detector;
    protected DescriptorExtractor extractor;

    public static final String NUM_FEATURES_KEY= "nFeatures";

    private final static Logger logger = LoggerFactory.getLogger(FeatureExtractor.class);

    /**
     * Extract features from an image
     * @param image A {@link Mat} representing the image
     * @return {@link KeypointDescList} containing the keypoints and descriptors
     */
    public KeypointDescList extract(Mat image)
    {
        //Keypoints
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        Long start = System.currentTimeMillis();
        if(detector != null)
            detector.detect(image, keypoints);
        Long detect = System.currentTimeMillis();
        extractor.compute(image, keypoints, descriptors);
        Long extract = System.currentTimeMillis();
        logger.debug("Detection:"+(detect-start));
        logger.debug("Extraction:"+(extract-detect));
        return new KeypointDescList(keypoints, descriptors);
    }

    /**
     * Extract feature from the image
     * @param inputFile path to the image
     * @return
     */
    public KeypointDescList extract(String inputFile)
    {
        Long start = System.currentTimeMillis();
        Mat m = Highgui.imread(inputFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Long stop = System.currentTimeMillis();
        logger.debug("ImageFileLoad:"+(stop-start));

        KeypointDescList kd = extract(m);
        m.release();
        return kd;
    }

    /**
     * Extract feature from the image
     * @param data the image
     * @return
     */
    public KeypointDescList extract(byte[] data)
    {
        Long start = System.currentTimeMillis();
        Mat m = Highgui.imdecode(new MatOfByte(data), Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Long stop = System.currentTimeMillis();
        logger.debug("ImageByteLoad:"+(stop-start));

        KeypointDescList kd = extract(m);
        m.release();
        return kd;
    }

    public abstract void updateNumDescriptorsExtracted(int num_descriptors) throws IOException;
}
