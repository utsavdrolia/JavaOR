package edu.cmu.edgecache.objrec.boofcv;

import boofcv.struct.feature.TupleDesc;
import georegression.struct.point.Point2D_F64;

/**
 * Associate point features in the two descriptor lists
 * Created by utsav on 2/5/16.
 */
public interface FeatureAssociator<K extends Point2D_F64, D extends TupleDesc>
{
    /**
     * Associate the 2 images and return a association score. Greater is better
     * @param kp1
     * @param kp2
     * @return Association score
     */
    Double associate(KeypointDescList<K, D> kp1, KeypointDescList<K, D> kp2);
}
