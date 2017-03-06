package edu.cmu.edgecache.objrec.opencv.matchers;

import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 6/6/16.
 */
public class Verify
{
    /**
     * Given the matched points, Find the homography and return a score. Higher score means better match
     * @param good_matches List containing the matches
     * @param dbImage Database image
     * @param sceneImage Scene image
     * @return
     */
    public static Double homography(List<DMatch> good_matches, KeypointDescList dbImage, KeypointDescList sceneImage)
    {

        List<Point> good_dbkp = new ArrayList<>();
        List<Point> good_scenekp = new ArrayList<>();
        Mat inliers = new Mat();

        for(DMatch match: good_matches)
        {
            good_dbkp.add(dbImage.points.get(match.trainIdx));
            good_scenekp.add(sceneImage.points.get(match.queryIdx));
        }

        MatOfPoint2f good_dbpoints = new MatOfPoint2f();
        good_dbpoints.fromList(good_dbkp);

        MatOfPoint2f good_scenepoints = new MatOfPoint2f();
        good_scenepoints.fromList(good_scenekp);

        Calib3d.findHomography(good_dbpoints, good_scenepoints, Calib3d.RANSAC, 5.0, inliers);
        Double ret = Core.sumElems(inliers).val[0]/ good_matches.size();

        good_dbpoints.release();
        good_scenepoints.release();

        return ret;
    }
}
