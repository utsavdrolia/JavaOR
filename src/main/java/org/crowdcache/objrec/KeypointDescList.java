package org.crowdcache.objrec;

/**
 * Created by utsav on 2/3/16.
 */

import boofcv.struct.feature.TupleDesc;
import georegression.struct.point.Point2D_F64;
import org.ddogleg.struct.FastQueue;

import java.util.List;

/**
 * Container for Image Keypoints and Descriptors
 */
public class KeypointDescList<K extends Point2D_F64, D extends TupleDesc>
{
    public KeypointDescList(List<K> points, FastQueue<D> descriptions)
    {
        this.points = points;
        this.descriptions = descriptions;
    }
    public List<K> points;
    public FastQueue<D> descriptions;
}