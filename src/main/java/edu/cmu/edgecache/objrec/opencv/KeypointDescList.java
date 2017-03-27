package edu.cmu.edgecache.objrec.opencv;

/**
 * Created by utsav on 2/3/16.
 */

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for Image Keypoints and Descriptors
 */
public class KeypointDescList
{
    public final ArrayList<Point> points;
    public final Mat descriptions;

    public KeypointDescList(MatOfKeyPoint kpoints, Mat descriptions)
    {
        this.points = new ArrayList<>(kpoints.rows());
        for(KeyPoint kp : kpoints.toList())
        {
            points.add(kp.pt);
        }
        kpoints.release();
        this.descriptions = descriptions;
    }

    public KeypointDescList(List<Point> kp, Mat descriptions)
    {
        points = new ArrayList<>(kp);
        this.descriptions = descriptions;
    }

    /**
     * Append a {@link KeypointDescList} to this one
     * @param list
     */
    public void append(KeypointDescList list)
    {
        this.points.addAll(list.points);
        this.descriptions.push_back(list.descriptions);
    }
}