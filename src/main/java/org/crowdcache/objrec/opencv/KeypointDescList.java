package org.crowdcache.objrec.opencv;

/**
 * Created by utsav on 2/3/16.
 */

import boofcv.struct.feature.TupleDesc;
import georegression.struct.point.Point2D_F64;
import org.ddogleg.struct.FastQueue;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for Image Keypoints and Descriptors
 */
public class KeypointDescList
{
    public final List<KeyPoint> points;
    public final Mat descriptions;

    public KeypointDescList(MatOfKeyPoint points, Mat descriptions)
    {
        this.points = points.toList();
        points.release();
        this.descriptions = descriptions;
    }
}