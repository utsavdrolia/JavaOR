package org.crowdcache.objrec.opencv;

import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/5/16.
 */
public class Recognizer
{

    private static final Double SCORE_THRESH = 0.5;
    private final Map<String, KeypointDescList> DB;
    private final FeatureExtractor extractor;
    private final ExecutorService executorService;
    private final Matcher matcher;

    /**
     * Loads the DB in memory so that it can be queried repeatedly using the recognize function
     * @param extractor Which {@link FeatureExtractor to use}
     * @param matcher Which {@link Matcher to use}
     * @param dblistpath Where is the DB located
     */
    public Recognizer(FeatureExtractor extractor, Matcher matcher, String dblistpath) throws IOException
    {
        this.extractor = extractor;
        this.matcher = matcher;
        this.DB = DBLoader.processDB(dblistpath, extractor);
        this.executorService = Executors.newFixedThreadPool(24);
    }

    public Recognizer(FeatureExtractor extractor, Matcher matcher, Map<String, KeypointDescList> db)
    {
        this.extractor = extractor;
        this.matcher = matcher;
        this.DB = db;
        this.executorService = Executors.newFixedThreadPool(24);
    }

    /**
     * Extract features from image, match against all of the images in the DB.
     * @param image Input image
     * @return The matched image name or null if no match
     */
    public String recognize(Mat image)
    {
        //-- Extract input image KP and Desc --
        final KeypointDescList inputKDlist = this.extractor.extract(image);
        //--

        return recognize(inputKDlist);
    }

    /**
     * Convert byte[] to image. Extract features from image, match against all of the images in the DB.
     * @param data
     */
    public String recognize(byte[] data)
    {
        Mat image = Highgui.imdecode(new MatOfByte(data), Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        return recognize(image);
    }


    /**
     * match features against all of the images in the DB.
     * @param inputKDlist
     * @return
     */
    public String recognize(KeypointDescList inputKDlist)
    {
        String ret = "None";
        Double score = Double.MIN_VALUE;
        Map<String, Double> matchResults = parallelMatch(inputKDlist);

        for(Map.Entry<String, Double> future:matchResults.entrySet())
        {
            Double matchscore = future.getValue();
//          System.out.println("DB Image:" + future.getKey() + " Score:" + matchscore);
            if(matchscore > SCORE_THRESH)
            {
                if (matchscore > score)
                {
                    score = matchscore;
                    ret = future.getKey();
//                  System.out.println("DB Image:" + future.getKey() + " Score:" + matchscore);
                }
            }
        }
        //--

        return ret;
    }

    /**
     * Match input list to all known lists
     * @param inputKDlist
     * @return
     */
    public Map<String, Double> parallelMatch(final KeypointDescList inputKDlist)
    {
        HashMap<String, Future<Double>> matches = new HashMap<String, Future<Double>>(DB.size(), 1.0f);
        HashMap<String, Double> result = new HashMap<String, Double>(DB.size(), 1.0f);
        //-- Match against all DB --
        for(final Map.Entry<String, KeypointDescList> entry : DB.entrySet())
        {
            matches.put(entry.getKey(), executorService.submit(new Callable<Double>()
            {
                public Double call() throws Exception
                {
                    return matcher.match(entry.getValue(), inputKDlist);
                }
            }));
        }

        for(Map.Entry<String, Future<Double>> future:matches.entrySet())
        {
            try
            {
                Double matchscore = future.getValue().get();
                result.put(future.getKey(), matchscore);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 2)
        {
            String query = args[0];
            String DBdirpath = args[1];
            FeatureExtractor extractor = new ORB();
            Matcher matcher = new BFMatcher_HAM();
            Recognizer recognizer = new Recognizer(extractor, matcher, DBdirpath);

            Mat img = Highgui.imread(query, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            Long start = System.currentTimeMillis();
            String result = recognizer.recognize(img);
            System.out.println("Time:" + (System.currentTimeMillis() - start));
            if(result == null)
                result = "None";
            System.out.println("Input:" + query + " Matched:" + result);
        }
        System.exit(1);
    }

}
