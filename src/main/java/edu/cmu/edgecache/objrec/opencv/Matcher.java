package edu.cmu.edgecache.objrec.opencv;

import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by utsav on 2/7/16.
 */
public abstract class Matcher
{
    public static final String INVALID = "None";
    protected Map<String, KeypointDescList> DB;
    private int max_size = Integer.MAX_VALUE;
    private boolean isFixed = false;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Matcher readable_copy = null;
    final static Logger logger = LoggerFactory.getLogger(Matcher.class);

    /**
     * Matcher constructor
     */
    public Matcher()
    {
        this.DB = new ConcurrentHashMap<>();
    }

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
    protected abstract String _matchAll(KeypointDescList sceneImage);

    public String matchAll(KeypointDescList sceneImage)
    {
        String ret;
        try
        {
            readWriteLock.readLock().lock();
            if(this.readable_copy == null)
                return INVALID;
            ret = readable_copy._matchAll(sceneImage);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }

        return ret;
    }

    public abstract Matcher newMatcher();


    /**
     * Store the Image -> Features association
     * @param dataset Image -> Features association
     */
    public final synchronized void train(Map<String, KeypointDescList> dataset) throws IllegalArgumentException
    {
        logger.debug("In Training");
        boolean flag = false;

        if(dataset.size() <= max_size)
        {
            // Check if DB is a subset of dataset
            Set<String> dataset_keys = new HashSet<>(dataset.keySet());
            Set<String> DB_keys = DB.keySet();
            if(dataset_keys.containsAll(DB_keys))
            {
                // Is a subset
                // Subtract common elements from the two key sets
                dataset_keys.removeAll(DB_keys);
                // Insert the uncommon elements
                for (String key:dataset_keys)
                {
                    DB.put(key, dataset.get(key));
//                    _insert(key, dataset.get(key));
                    flag = true;
                }
            }
            else
            {
                // Is not a subset
                // Re-train the entire thing
                this.DB.clear();
                this.DB.putAll(dataset);
//                _train();
                flag = true;
            }

            if(flag)
            {
                // If new items were added, switch the readable copy so that it points to the new matcher
                Matcher write_copy = newMatcher();
                write_copy._trainAll(this.DB);
                readWriteLock.writeLock().lock();
                {
                    readable_copy = write_copy;
                }
                readWriteLock.writeLock().unlock();
            }
        }
        else
            throw new IllegalArgumentException("Size of dataset bigger than set size");
        logger.debug("Done Training");
    }


    private void _trainAll(Map<String, KeypointDescList> db)
    {
        this.DB = db;
        _train();
    }

    /**
     * Insert one image into Matcher
     * @param name
     * @param kplist
     */
    private void insert(String name, KeypointDescList kplist)
    {
        readWriteLock.writeLock().lock();
        {
            if (!DB.containsKey(name))
            {
                DB.put(name, kplist);
                _insert(name, kplist);
            }
        }
        readWriteLock.writeLock().unlock();
    }

    /**
     * Remove a specific image from matcher
     * @param name
     */
    private synchronized void remove(String name)
    {
        readWriteLock.writeLock().lock();
        {
            if (DB.remove(name) != null)
                _train();
        }
        readWriteLock.writeLock().unlock();
    }

    /**
     * Do we know about this image?
     * @param annotation
     * @return
     */
    public boolean contains(String annotation)
    {
        return this.DB.containsKey(annotation);
    }

    /**
     *
     * @param annotation Image name
     * @return Extracted {@link KeypointDescList} for the image
     */
    public KeypointDescList get(String annotation)
    {
        return this.DB.get(annotation);
    }

    /**
     * Insert one image into matcher
     */
    protected abstract void _insert(String name, KeypointDescList kplist);

    /**
     * Internally train matcher
     */
    protected abstract void _train();

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
            if(arr.length == 2)
            {
                DMatch m = arr[0];
                DMatch n = arr[1];
                // Release of the MatOfDMatch
                dmatch.release();
                if(m.distance < 0.7*n.distance)
                    good_matches.add(m);
            }
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
