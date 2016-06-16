package org.crowdcache.objrec.opencv.matchers;

import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

import java.util.*;
import java.util.concurrent.*;

import static com.sun.tools.javac.jvm.ByteCodes.ret;

/**
 * Created by utsav on 2/6/16.
 * Uses Hamming distance, Brute Force Matcher, Lowe's Distance ratio test, and Homography verification
 */
public abstract class BFMatcher_HAM extends Matcher
{
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);;
    private int NUM_MATCHES_THRESH = 10;
    private final int THREADS=16;
    private static final Double SCORE_THRESH = 0.6;


    public BFMatcher_HAM()
    {
        this(-1);
    }

    public BFMatcher_HAM(int size)
    {
        super(size);
    }

    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage)
    {
        List<MatOfDMatch> matches = new ArrayList<>();
        List<DMatch> good_matches;
        Mat inliers = new Mat();
        Double ret = 0.0;

        matcher.knnMatch(dbImage.descriptions, sceneImage.descriptions, matches, 2);
        // Ratio test
        good_matches = ratioTest(matches);

        // Minimum number of good matches and homography verification
        if(good_matches.size() > NUM_MATCHES_THRESH)
        {
            ret = Verify.homography(good_matches, sceneImage, dbImage);
        }
        matches.clear(); matches = null;
        good_matches.clear(); good_matches = null;
        inliers.release(); inliers = null;
        return ret;
    }

    @Override
    public String matchAll(KeypointDescList sceneImage)
    {
        String ret = "None";
        Double score = Double.MIN_VALUE;

        // Check if enough features present
        if(sceneImage.descriptions.rows() < 5)
            return ret;

        Map<String, Double> matchResults = parallelMatch(sceneImage);


        for(Map.Entry<String, Double> result:matchResults.entrySet())
        {
            Double matchscore = result.getValue();
            //System.out.println(result.getKey() + ":" + matchscore);

            if(matchscore > SCORE_THRESH)
            {
                if (matchscore > score)
                {
                    score = matchscore;
                    ret = result.getKey();
                }
            }
        }

        return ret;
    }

    /**
     * Match input list to all known lists
     * @param inputKDlist
     * @return
     */
    private Map<String, Double> parallelMatch(KeypointDescList inputKDlist)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        Map<String, Future<Double>> matches = new HashMap<>();
        Map<String, Double> result = new HashMap<>();

        //-- Match against all DB --
        for(Map.Entry<String, KeypointDescList> entry : DB.entrySet())
        {
            matches.put(entry.getKey(), executorService.submit(new CallableMatcher(entry.getValue(), inputKDlist, newMatcher())));
        }

        for(Map.Entry<String, Future<Double>> future:matches.entrySet())
        {
            try
            {
                Double matchscore = future.getValue().get();
                result.put(future.getKey(), matchscore);

            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                System.out.println("Error in " + future.getKey());
            }
        }
        executorService.shutdown();
        executorService = null;
        return result;
    }


    private static class CallableMatcher implements Callable<Double>
    {

        private final KeypointDescList dbKDlist;
        private final KeypointDescList inputKDlist;
        private final Matcher newmatcher;

        CallableMatcher(KeypointDescList dbKDlist, KeypointDescList inputKDlist, Matcher newmatcher)
        {
            this.dbKDlist = dbKDlist;
            this.inputKDlist = inputKDlist;
            this.newmatcher = newmatcher;
        }
        @Override
        public Double call() throws Exception
        {
            return newmatcher.match(dbKDlist, inputKDlist);
        }
    }
}
