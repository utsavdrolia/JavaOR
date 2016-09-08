package edu.cmu.edgecache.objrec.boofcv;

import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageSingleBand;
import georegression.struct.point.Point2D_F64;

/**
 * Interface for Extracting features from given image and returning a datastructure that is used by the Associator
 * Created by utsav on 2/5/16.
 */
public interface FeatureExtractor<I extends ImageSingleBand, K extends Point2D_F64, D extends TupleDesc>
{
    /**
     *
     * @param image
     * @return The list of keypoints and respective descriptors
     */
     <II extends ImageSingleBand> KeypointDescList<K, D> extract(I image);
}
