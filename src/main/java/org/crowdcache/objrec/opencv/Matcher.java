package org.crowdcache.objrec.opencv;

import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/7/16.
 */
public abstract class Matcher
{
    protected Map<String, KeypointDescList> DB;

    /**
     * Match the 2 images and return a match score
     * @param dbImage
     * @param sceneImage
     * @return
     */
    public abstract Double match(KeypointDescList dbImage, KeypointDescList sceneImage);

    /**
     * Match the given sceneImage with all known Database images
     * @param sceneImage
     * @return Name of best match
     */
    public abstract String matchAll(KeypointDescList sceneImage);
    public abstract Matcher newMatcher();


    /**
     * Store the Image -> Features association
     * @param dataset Image -> Features association
     */
    public void train(Map<String, KeypointDescList> dataset)
    {
        this.DB = dataset;
    }

    /**
     * Ratio test for descriptor matches
     * @param matches
     * @return
     */
    protected List<DMatch> ratioTest(List<MatOfDMatch> matches)
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

    /**
     * Convert the list of good matches to a Map of Images and the Descriptors that occur in the images
     * @param good_matches
     * @return
     */
    protected Map<Integer, List<DMatch>> invertGoodMatches(List<DMatch> good_matches)
    {
        Map<Integer, List<DMatch>> image2match = new HashMap<>();

        for(DMatch match:good_matches)
        {
            Integer imgID = match.imgIdx;
            if(!image2match.containsKey(imgID))
                image2match.put(imgID, new ArrayList<DMatch>());
            image2match.get(imgID).add(match);
        }
        return image2match;
    }
}
