package org.crowdcache.objreccache;

import naivebayes.BayesClassifier;
import naivebayes.Classification;
import org.crowdcache.Cache;
import org.crowdcache.LRUCache;
import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.extractors.BRISK;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvNormalBayesClassifier;

import java.util.*;
import java.util.concurrent.*;


/**
 * Created by utsav on 2/9/16.
 */
public class ObjectRecogCacheNB implements Cache<KeypointDescList, String>
{
    private final FeatureExtractor extractor;
    private final LRUCache<String, KeypointDescList> cache;
    private BayesClassifier<String, String> classifier;
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    private Mat descriptors;

    public ObjectRecogCacheNB(Integer size)
    {
        this.extractor = new ORB();
        this.cache = new LRUCache<>(size);
        this.classifier = new BayesClassifier<>();
        this.classifier.setMemoryCapacity(size);
    }

    /**
     * Store the keypoints and descriptors along with the annotation
     *
     * @param key
     * @param value
     */
    public void put(KeypointDescList key, String value)
    {
        cache.put(value, key);
        updateclassifier();
    }

    /**
     * Retrain the classifier
     */
    private void updateclassifier()
    {
        synchronized (cache)
        {
            this.descriptors = new Mat();
            List<String> keys = new ArrayList<>(100);
            classifier.reset();
            for (String key : cache.keySet())
            {
                for (int i = 0; i < cache.get(key).descriptions.rows(); i++)
                    keys.add(cache.get(key).descriptions.row(i).dump());
                classifier.learn(key, keys);
                descriptors.push_back(cache.get(key).descriptions);
            }
            System.out.println(classifier.getCategories());
        }
    }

    /**
     * Get the best value for this key along with a confidence metric
     *
     * @param key
     * @return
     */
    public Result<String> get(KeypointDescList key)
    {
        return match(key);
    }


    protected byte[] reduce(String imgpath)
    {

        Mat img = Highgui.imread(imgpath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat dst = new Mat();
        Imgproc.resize(img, dst, new Size(img.width() / 2, img.height() / 2));
        MatOfByte bytemat = new MatOfByte();
        Highgui.imencode(".jpg", dst, bytemat, new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 30));
        return bytemat.toArray();
    }

    public Result<String> get(String imgpath)
    {
        byte[] newpath = reduce(imgpath);
        return this.get(this.extractor.extract(newpath));
    }

    public void put(String imgpath, String value)
    {
        byte[] newpath = reduce(imgpath);
        this.put(this.extractor.extract(newpath), value);
    }

    /**
     * Match the 2 images and return a match score
     *
     * @param sceneImage
     * @return
     */
    public Result<String> match(KeypointDescList sceneImage)
    {
        synchronized (cache)
        {
            if (descriptors != null)
            {
                MatOfDMatch matches = new MatOfDMatch();
                List<String> keys = new ArrayList<>(50);
                matcher.match(sceneImage.descriptions, descriptors, matches);
                List<DMatch> good_matches = matches.toList();
                for (DMatch m : good_matches)
                {
                    keys.add(descriptors.row(m.trainIdx).dump());
                }
                Classification<String, String> classification = classifier.classify(keys);
                System.out.println(classification);
                return new Result<>((double) classification.getProbability(), classification.getCategory());
            }
        }
        return new Result<>("None");
    }
}
