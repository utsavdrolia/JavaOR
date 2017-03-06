package edu.cmu.edgecache.objrec.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

/**
 * Interface for Extracting features from given image and returning a datastructure that is used by the Associator
 * Created by utsav on 2/5/16.
 */
public abstract class FeatureExtractor
{
    protected FeatureDetector detector;
    protected DescriptorExtractor extractor;
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
        detector.detect(image, keypoints);

        extractor.compute(image, keypoints, descriptors);

        return new KeypointDescList(keypoints, descriptors);
    }

    /**
     * Extract feature from the image
     * @param inputFile path to the image
     * @return
     */
    public KeypointDescList extract(String inputFile)
    {
        Mat m = Highgui.imread(inputFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
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
        Mat m = Highgui.imdecode(new MatOfByte(data), Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        KeypointDescList kd = extract(m);
        m.release();
        return kd;
    }
}
