package org.crowdcache.objrec.opencv;

import org.opencv.core.Mat;

/**
 * Interface for Extracting features from given image and returning a datastructure that is used by the Associator
 * Created by utsav on 2/5/16.
 */
public interface FeatureExtractor
{
    /**
     *
     * @param image
     * @return The list of keypoints and respective descriptors
     */
    KeypointDescList extract(Mat image);
}
