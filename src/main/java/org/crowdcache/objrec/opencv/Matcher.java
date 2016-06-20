package org.crowdcache.objrec.opencv;

import org.crowdcache.LRUCache;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by utsav on 2/7/16.
 */
public abstract class Matcher
{
    protected Map<String, KeypointDescList> DB;
    private int max_size = Integer.MAX_VALUE;
    private boolean isFixed = false;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     *
     * @param max_size If not -1, limit the size of the Matcher
     */
    public Matcher(int max_size)
    {
        if(max_size != -1)
        {
            this.max_size = max_size;
            this.DB = Collections.synchronizedMap(new LRUCache<>(max_size));
            isFixed = true;
        }
        else
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
        readWriteLock.readLock().lock();
        {
            ret = _matchAll(sceneImage);
        }
        readWriteLock.readLock().unlock();
        return ret;
    }

    public abstract Matcher newMatcher();


    /**
     * Store the Image -> Features association
     * @param dataset Image -> Features association
     */
    public final void train(Map<String, KeypointDescList> dataset) throws IllegalArgumentException
    {
        if(dataset.size() <= max_size)
        {
            readWriteLock.writeLock().lock();
            {
                this.DB.putAll(dataset);
                _train();
            }
            readWriteLock.writeLock().unlock();
        }
        else
            throw new IllegalArgumentException("Size of dataset bigger than set size");
    }

    /**
     * Insert one image into Matcher
     * @param name
     * @param kplist
     */
    public final void insert(String name, KeypointDescList kplist)
    {
        readWriteLock.writeLock().lock();
        {
            if (!DB.containsKey(name))
            {
                if (DB.size() == max_size)
                {
                    DB.put(name, kplist);
                    _train();
                } else
                {
                    DB.put(name, kplist);
                    _insert(name, kplist);
                }
            }
        }
        readWriteLock.writeLock().unlock();
    }

    /**
     * Remove a specific image from matcher
     * @param name
     */
    public final synchronized void remove(String name)
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
