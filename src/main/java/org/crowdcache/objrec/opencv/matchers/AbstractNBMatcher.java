package org.crowdcache.objrec.opencv.matchers;

import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by utsav on 2/6/16.
 * Implements the Naive Bayes matching technique. Matchers can extend it to define the kind of matching they want to use
 */
public abstract class AbstractNBMatcher extends Matcher
{
    protected DescriptorMatcher matcher;
    protected int NUM_MATCHES_THRESH = 3;
    protected List<String> objects;
    protected Double SCORE_THRESH = 0.6;

    protected AbstractNBMatcher()
    {
        super();
        objects = Collections.synchronizedList(new ArrayList<String>());
    }

    /**
     * Store a list of images' descriptors in the matcher
     */
    public void _insert(String name, KeypointDescList kplist)
    {
        List<Mat> m = new ArrayList<>();

        objects.add(name);
        m.add(kplist.descriptions);
        matcher.add(m);
        matcher.train();
//        System.out.println("Image:" + name);
//        System.out.println("Train:" + matcher.getTrainDescriptors().size() + " DB:" + DB.size() + " Objects:"+objects.size());
    }

    /**
     * Clear and train the matcher
     */
    public void _train()
    {
        objects.clear();
        matcher.clear();
        List<String> keys = new ArrayList<>(this.DB.keySet());
        for (String object : keys)
        {
            _insert(object, this.DB.get(object));
        }
    }


    @Override
    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage) {
        return null;
    }

    @Override
    protected String _matchAll(KeypointDescList sceneImage)
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
        for (Integer img : image2match.keySet())
        {
            List<DMatch> dmatches = image2match.get(img);
            if (dmatches.size() > NUM_MATCHES_THRESH)
            {
                Double matchscore = Verify.homography(dmatches, DB.get(objects.get(img)), sceneImage);
                //System.out.println(objects.get(img) + ":" + matchscore);
                if (matchscore > SCORE_THRESH)
                {
                    if (matchscore > score)
                    {
                        score = matchscore;
                        ret = objects.get(img);
                    }
                }
            }

        }


        matches.clear();
        matches = null;
        good_matches.clear();
        good_matches = null;
        inliers.release();
        inliers = null;
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
    public Matcher newMatcher()
    {
        return new BFMatcher_HAM_NB();
    }
}
