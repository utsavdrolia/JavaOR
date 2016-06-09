package org.crowdcache.objrec.opencv.matchers;

import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

import java.util.*;

/**
 * Created by utsav on 2/6/16.
 */
public class BFMatcher_L2_NB extends Matcher
{
    private DescriptorMatcher matcher;
    private static int NUM_MATCHES_THRESH = 5;
    private List<String> objects;
    private static final Double SCORE_THRESH = 0.6;


    public BFMatcher_L2_NB()
    {
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
    }

    public BFMatcher_L2_NB(int thresh)
    {
        NUM_MATCHES_THRESH = thresh;
    }

    /**
     * Store a single image's descriptors in the matcher
     * @param descriptions
     */
    public void train(Mat descriptions)
    {
        List<Mat> m = new ArrayList<>();
        m.add(descriptions);
        matcher.add(m);
    }

    /**
     * Store a list of images' descriptors in the matcher
     * @param descriptions
     */
    public void train(List<Mat> descriptions)
    {
        matcher.add(descriptions);
    }

    /**
     * Store the dataset and train the BruteForce matcher
     * @param dataset Image -> Features association
     */
    public void train(Map<String, KeypointDescList> dataset)
    {
        super.train(dataset);
        objects = new ArrayList<>(this.DB.keySet());
        for(String object: objects)
        {
            train(DB.get(object).descriptions);
        }
    }


    @Override
    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage) {
        return null;
    }

    @Override
    public String matchAll(KeypointDescList sceneImage)
    {
        List<MatOfDMatch> matches = new ArrayList<>();
        List<DMatch> good_matches;
        Mat inliers = new Mat();
        String ret = "None";
        Double score = Double.MIN_VALUE;

        matcher.knnMatch(sceneImage.descriptions, matches, 2);

        // Ratio test
        good_matches = ratioTest(matches);

        // Get an inverted map (image --> descriptor)
        Map<Integer, List<DMatch>> image2match = invertGoodMatches(good_matches);

        //printInvertedMatches(image2match);

        // Minimum number of good matches and homography verification
        for (Integer img: image2match.keySet())
        {
            List<DMatch> dmatches = image2match.get(img);
            if(dmatches.size() > NUM_MATCHES_THRESH)
            {
                Double matchscore = Verify.homography(dmatches, DB.get(objects.get(img)), sceneImage);
                //System.out.println(objects.get(img) + ":" + matchscore);
                if(matchscore > SCORE_THRESH)
                {
                    if (matchscore > score)
                    {
                        score = matchscore;
                        ret = objects.get(img);
                    }
                }
            }
        }


        matches.clear(); matches = null;
        good_matches.clear(); good_matches = null;
        inliers.release(); inliers = null;
        return ret;
    }

    private void printInvertedMatches(Map<Integer, List<DMatch>> image2match)
    {
        for (Integer imgID: image2match.keySet())
        {
            System.out.println(objects.get(imgID) + ":" + image2match.get(imgID).size());
        }
    }


    @Override
    public Matcher newMatcher() {
        return new BFMatcher_L2_NB();
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
            Double matches = new BFMatcher_L2_NB().match(qpoints, tpoints);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Score:" + matches);
        }

    }
}
