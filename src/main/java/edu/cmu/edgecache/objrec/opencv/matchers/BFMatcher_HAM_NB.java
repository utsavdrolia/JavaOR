package edu.cmu.edgecache.objrec.opencv.matchers;

import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import edu.cmu.edgecache.objrec.opencv.extractors.ORB;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

/**
 * Created by utsav on 2/6/16.
 * Uses Hamming distance, Brute Force Matcher, Lowe's Distance ratio test, and Homography verification
 * This matcher stores all the descriptors from all the images in the DB and runs the match against all of them at once
 * thus giving the closest match in any DB image
 */
public class BFMatcher_HAM_NB extends AbstractNBMatcher
{
    /**
     *
     * @param match_thresh Min. Number of matches
     * @param score_thresh Min. score
     */
    public BFMatcher_HAM_NB(int match_thresh, Double score_thresh)
    {
        this();
        NUM_MATCHES_THRESH = match_thresh;
        SCORE_THRESH = score_thresh;
    }

    public BFMatcher_HAM_NB()
    {
        super();
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        NUM_MATCHES_THRESH = 3;
        SCORE_THRESH = 0.8;
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
            Double matches = new BFMatcher_HAM_NB().match(qpoints, tpoints);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Score:" + matches);
        }
        System.exit(1);
    }
}
