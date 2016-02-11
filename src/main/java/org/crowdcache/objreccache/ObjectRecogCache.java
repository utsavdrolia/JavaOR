package org.crowdcache.objreccache;

import org.crowdcache.approxcache.LRUCache;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;

import java.util.Map;


/**
 * Created by utsav on 2/9/16.
 */
public class ObjectRecogCache implements org.crowdcache.approxcache.Cache<KeypointDescList, String>
{
    private LRUCache<String, KeypointDescList> cache;
    private Matcher matcher;

    public ObjectRecogCache(Integer size, Matcher matcher)
    {
        this.matcher = matcher;
        cache = new LRUCache<String, KeypointDescList>(size);
    }
    /**
     * Store the keypoints and descriptors along with the annotation
     * @param key
     * @param value
     */
    public void put(KeypointDescList key, String value)
    {
        cache.put(value, key);
    }

    /**
     * Get the best value for this key along with a confidence metric
     * @param key
     * @return
     */
    public Result<String> get(KeypointDescList key)
    {
        Recognizer rec = new Recognizer(null, matcher, cache);
        Map<String, Double> matchResults = rec.parallelMatch(key);
        Double score = Double.MIN_VALUE;
        String ret = "None";
        for(Map.Entry<String, Double> future:matchResults.entrySet())
        {
            Double matchscore = future.getValue();
                if (matchscore > score)
                {
                    score = matchscore;
                    ret = future.getKey();
                }
        }
        return new Result<String>(score, ret);
    }
}
