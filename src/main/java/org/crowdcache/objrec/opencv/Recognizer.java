package org.crowdcache.objrec.opencv;

import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
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

    private static final Double SCORE_THRESH = 0.6;
    private final Map<String, KeypointDescList> DB;
    private final FeatureExtractor extractor;
    private final Matcher matcher;

    private final int THREADS=2;

    // Data structures used multiple times
    private HashMap<String, Future<Double>> matches;
    private HashMap<String, Double> result;
    /**
     * Loads the DB in memory so that it can be queried repeatedly using the recognize function
     * @param extractor Which {@link FeatureExtractor to use}
     * @param matcher Which {@link Matcher to use}
     * @param dblistpath Where is the DB located
     */
    public Recognizer(FeatureExtractor dbextractor, FeatureExtractor extractor, Matcher matcher, String dblistpath) throws IOException
    {
        this.extractor = extractor;
        this.matcher = matcher;
        this.DB = DBLoader.processDB(dblistpath, dbextractor);
        matches = new HashMap<>(DB.size(), 1.0f);
        result = new HashMap<>(DB.size(), 1.0f);
    }

    public Recognizer(FeatureExtractor dbextractor, FeatureExtractor extractor, Matcher matcher, Map<String, KeypointDescList> db)
    {
        this.extractor = extractor;
        this.matcher = matcher;
        this.DB = db;
    }

    /**
     * Extract features from image, match against all of the images in the DB.
     * @param image Input image
     * @return The matched image name or null if no match
     */
    public String recognize(Mat image)
    {
        //-- Extract input image KP and Desc --
        KeypointDescList inputKDlist = this.extractor.extract(image);
        //--
        String ret = recognize(inputKDlist);
        inputKDlist.descriptions.release();
        return ret;
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
        // Check if enough features present
        if(inputKDlist.descriptions.rows() < 5)
            return ret;

        Double score = Double.MIN_VALUE;
        Map<String, Double> matchResults = parallelMatch(inputKDlist);

        for(Map.Entry<String, Double> result:matchResults.entrySet())
        {
            Double matchscore = result.getValue();
          //System.out.println("DB Image:" + result.getKey() + " Score:" + matchscore);
            if(matchscore > SCORE_THRESH)
            {
                if (matchscore > score)
                {
                    score = matchscore;
                    ret = result.getKey();
//                  System.out.println("DB Image:" + future.getKey() + " Score:" + matchscore);
                }
            }
        }
        System.out.println("Result size:" + result.size());
        //--
//        matches.clear();
//        result.clear();
        inputKDlist = null;
        return ret;
    }

    /**
     * Match input list to all known lists
     * @param inputKDlist
     * @return
     */
    private Map<String, Double> parallelMatch(KeypointDescList inputKDlist)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        //-- Match against all DB --
        for(Map.Entry<String, KeypointDescList> entry : DB.entrySet())
        {
            matches.put(entry.getKey(), executorService.submit(new CallableMatcher(entry.getValue(), inputKDlist, matcher.newMatcher())));
//            result.put(entry.getKey(), matcher.match(entry.getValue(), inputKDlist));
        }

        for(Map.Entry<String, Future<Double>> future:matches.entrySet())
        {
            try
            {
                Double matchscore = future.getValue().get();
                result.put(future.getKey(), matchscore);

            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                System.out.println("Error in " + future.getKey());
            }
        }
        executorService.shutdown();
        executorService = null;
        return result;
    }

    private static class CallableMatcher implements Callable<Double>
    {

        private final KeypointDescList dbKDlist;
        private final KeypointDescList inputKDlist;
        private final Matcher newmatcher;

        CallableMatcher(KeypointDescList dbKDlist, KeypointDescList inputKDlist, Matcher newmatcher)
        {
            this.dbKDlist = dbKDlist;
            this.inputKDlist = inputKDlist;
            this.newmatcher = newmatcher;
        }
        @Override
        public Double call() throws Exception
        {
            Double ret = newmatcher.match(dbKDlist, inputKDlist);
            return ret;
        }
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
            Recognizer recognizer = new Recognizer(extractor, extractor, matcher, DBdirpath);

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
