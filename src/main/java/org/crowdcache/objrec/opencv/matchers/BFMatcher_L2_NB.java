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
public class BFMatcher_L2_NB extends AbstractNBMatcher
{

    /**
     *
     * @param path For matcher params
     */
    public BFMatcher_L2_NB(String path)
    {
        this();
        matcher.read(path);
    }

    public BFMatcher_L2_NB()
    {
        super();
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
        NUM_MATCHES_THRESH = 5;
        SCORE_THRESH = 0.6;
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
