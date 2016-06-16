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
import java.util.List;
import java.util.Map;

/**
 * Created by utsav on 2/6/16.
 * Implements the Naive Bayes matching technique. Matchers can extend it to define the kind of matching they want to use
 */
public abstract class AbstractNBMatcher extends Matcher
{
    private DescriptorMatcher matcher;
    private static int NUM_MATCHES_THRESH = 3;
    private List<String> objects;
    private static final Double SCORE_THRESH = 0.6;
    private final Boolean trainingLock = true;



    /**
     * @param size Size of Matcher. -1 for {@link Integer#MAX_VALUE}
     */
    protected AbstractNBMatcher(int size)
    {
        super(size);
    }

    /**
     * Store a list of images' descriptors in the matcher
     */
    public void _insert(String name, KeypointDescList kplist)
    {

        objects.add(name);
        List<Mat> m = new ArrayList<>();

        synchronized (trainingLock)
        {
            m.add(kplist.descriptions);
            matcher.add(m);
        }
    }

    /**
     * Clear and train the matcher
     */
    public void _train()
    {
        synchronized (trainingLock)
        {
            objects = new ArrayList<>();
            this.matcher.clear();
            for (String object : this.DB.keySet())
            {
                _insert(object, this.DB.get(object));
            }
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

        synchronized (trainingLock)
        {
            matcher.knnMatch(sceneImage.descriptions, matches, 2);
        }

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
