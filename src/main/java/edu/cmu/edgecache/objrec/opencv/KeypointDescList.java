package edu.cmu.edgecache.objrec.opencv;

/**
 * Created by utsav on 2/3/16.
 */

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;

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

    public KeypointDescList(ArrayList<Point> kp, Mat descriptions)
    {
        points = kp;
        this.descriptions = descriptions;
    }
}