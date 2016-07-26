package org.crowdcache.objrec.opencv.matchers;

import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

import java.util.*;

/**
 * Created by utsav on 2/6/16.
 * Uses Hamming distance, FLANN (LSH based) Matcher, Lowe's Distance ratio test, and Homography verification
 * This matcher stores all the descriptors from all the images in the DB in an LSH hashtable
 * and runs the match against all of them at once
 * thus giving the ~closest match in any DB image
 */
public class LSHMatcher_HAM extends AbstractNBMatcher
{
    /**
     * Matcher with default size
     */
    public LSHMatcher_HAM()
    {
        this(-1);
    }

    /**
     *
     * @param path For matcher params
     * @param size Size of Matcher
     */
    public LSHMatcher_HAM(String path, Integer size)
    {
        this(size);
        matcher.read(path);
    }

    /**
     *
     * @param size Size of matcher
     * @param match_thresh Min. Number of matches
     * @param score_thresh Min. score
     */
    public LSHMatcher_HAM(String path, int size, int match_thresh, Double score_thresh)
    {
        this(size);
        matcher.read(path);
        NUM_MATCHES_THRESH = match_thresh;
        SCORE_THRESH = score_thresh;
    }

    /**
     * @param size Size of Matcher. -1 for {@link Integer#MAX_VALUE}
     */
    public LSHMatcher_HAM(int size)
    {
        super(size);
        matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        NUM_MATCHES_THRESH = 5;
        SCORE_THRESH = 0.6;
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
            Double matches = new LSHMatcher_HAM().match(qpoints, tpoints);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Score:" + matches);
        }
        System.exit(1);
    }
}
