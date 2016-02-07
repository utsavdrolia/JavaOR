package org.crowdcache.objrec.opencv;

import org.opencv.core.Mat;

/**
 * Interface for Extracting features from given image and returning a datastructure that is used by the Associator
 * Created by utsav on 2/5/16.
 */
public interface FeatureExtractor
{
    /**
     * Extract features from an image
     * @param image A {@link Mat} representing the image
     * @return {@link KeypointDescList} containing the keypoints and descriptors
     */
    KeypointDescList extract(Mat image);

    /**
     * Extract feature from the image
     * @param inputFile path to the image
     * @return
     */
    KeypointDescList extract(String inputFile);
}
