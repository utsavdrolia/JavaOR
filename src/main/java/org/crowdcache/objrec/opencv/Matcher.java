package org.crowdcache.objrec.opencv;

import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 2/7/16.
 */
public abstract class Matcher
{
    /**
     * Match the 2 images and return a match score
     * @param dbImage
     * @param sceneImage
     * @return
     */
    public abstract Double match(KeypointDescList dbImage, KeypointDescList sceneImage);
    public abstract Matcher newMatcher();

    /**
     * Ratio test for descriptor matches
     * @param matches
     * @return
     */
    public List<DMatch> ratioTest(List<MatOfDMatch> matches)
    {
        List<DMatch> good_matches = new ArrayList<>();

        // Ratio test
        for(MatOfDMatch dmatch: matches)
        {
            DMatch[] arr = dmatch.toArray();
            DMatch m = arr[0];
            DMatch n = arr[1];
            // Release of the MatOfDMatch
            dmatch.release();
            if(m.distance < 0.7*n.distance)
                good_matches.add(m);
        }

        return good_matches;
    }
}
