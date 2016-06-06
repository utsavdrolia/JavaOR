package org.crowdcache.objrec.opencv.matchers;

import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by utsav on 2/6/16.
 * Uses Hamming distance, Brute Force Matcher, Lowe's Distance ratio test, and Homography verification
 */
public class BFMatcher_HAM extends Matcher
{
    private DescriptorMatcher matcher;
    private int NUM_MATCHES_THRESH = 10;

    public BFMatcher_HAM()
    {
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    public BFMatcher_HAM(int thresh)
    {
        NUM_MATCHES_THRESH = thresh;
    }

    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage)
    {
        List<MatOfDMatch> matches = new ArrayList<>();
        List<DMatch> good_matches;
        Mat inliers = new Mat();
        Double ret = 0.0;
//        MatOfDMatch matches = new MatOfDMatch();
//        List<DMatch> good_matches;

//        matcher.match(dbImage.descriptions, sceneImage.descriptions, matches);
//        good_matches = matches.toList();
        matcher.knnMatch(dbImage.descriptions, sceneImage.descriptions, matches, 2);
        // Ratio test
        good_matches = ratioTest(matches);

//        Collections.sort(good_matches, new Comparator<DMatch>()
//        {
//            public int compare(DMatch o1, DMatch o2)
//            {
//                return (int) (o1.distance - o2.distance);
//            }
//        });

        // Minimum number of good matches and homography verification
        if(good_matches.size() > NUM_MATCHES_THRESH)
        {
            ret = Verify.homography(good_matches, dbImage, sceneImage);
        }
        matches.clear(); matches = null;
        good_matches.clear(); good_matches = null;
        inliers.release(); inliers = null;
        return ret;
    }

    @Override
    public Matcher newMatcher()
    {
        return new BFMatcher_HAM();
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
            KeypointDescList qpoints = new ORB().extract(qimage);

            Long start = System.currentTimeMillis();
            KeypointDescList tpoints = new ORB().extract(timage);
            Double matches = new BFMatcher_HAM().match(qpoints, tpoints);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Score:" + matches);
        }
        System.exit(1);
    }
}
