package org.crowdcache.objrec.opencv.matchers;

import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.calib3d.Calib3d;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 2/6/16.
 */
public class BFMatcher
{
    private static final int NUM_MATCHES_THRESH = 10;

    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage)
    {
        ArrayList<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        ArrayList<DMatch> good_matches = new ArrayList<DMatch>();
        List<Point> good_dbkp = new ArrayList<Point>();
        List<Point> good_scenekp = new ArrayList<Point>();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        matcher.knnMatch(dbImage.descriptions, sceneImage.descriptions, matches, 2);
        Mat inliers = new Mat();

        for(MatOfDMatch dmatch: matches)
        {
            DMatch[] arr = dmatch.toArray();
            DMatch m = arr[0];
            DMatch n = arr[1];
            if(m.distance < 0.7*n.distance)
                good_matches.add(m);
        }

        if(good_matches.size() > NUM_MATCHES_THRESH)
        {
            for(DMatch match:good_matches)
            {
                good_dbkp.add(dbImage.points.get(match.queryIdx).pt);
                good_scenekp.add(sceneImage.points.get(match.trainIdx).pt);
            }

            MatOfPoint2f good_dbpoints = new MatOfPoint2f();
            good_dbpoints.fromList(good_dbkp);

            MatOfPoint2f good_scenepoints = new MatOfPoint2f();
            good_scenepoints.fromList(good_scenekp);

            Calib3d.findHomography(good_dbpoints, good_scenepoints, Calib3d.RANSAC, 5.0, inliers);
            return Core.sumElems(inliers).val[0]/good_matches.size();
        }

        return 0.0;

    }

    public static void main(String args[])
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length > 0)
        {
            String queryFile = args[0];
            String trainFile = args[1];
            Mat qimage = Highgui.imread(queryFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            Mat timage = Highgui.imread(trainFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            // run each example
            KeypointDescList qpoints = new SURFFeatureExtractor().extract(qimage);
            KeypointDescList tpoints = new SURFFeatureExtractor().extract(timage);
            Long start = System.currentTimeMillis();
            Double matches = new BFMatcher().match(qpoints, tpoints);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Score:" + matches);
        }

    }
}
