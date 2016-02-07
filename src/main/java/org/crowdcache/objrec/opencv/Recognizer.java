package org.crowdcache.objrec.opencv;

import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
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

    private static final Double SCORE_THRESH = 0.4;
    private final HashMap<String, KeypointDescList> DB;
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

    /**
     * Extract features from image, match against all of the images in the DB.
     * @param image Input image
     * @return The matched image name or null if no match
     */
    public String recognize(Mat image)
    {
        String ret = null;
        Double score = Double.MIN_VALUE;
        HashMap<String, Future<Double>> matches = new HashMap<String, Future<Double>>();

        //-- Extract input image KP and Desc --
        final KeypointDescList inputKDlist = this.extractor.extract(image);
        //--

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
//                System.out.println("DB Image:" + future.getKey() + " Score:" + matchscore);
                if(matchscore > SCORE_THRESH)
                    if (matchscore > score)
                    {
                        score = matchscore;
                        ret = future.getKey();
                        System.out.println("DB Image:" + future.getKey() + " Score:" + matchscore);
                    }
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
        //--

        return ret;
    }

    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 2)
        {
            String query = args[0];
            String DBdirpath = args[1];
            SURFFeatureExtractor surfExtractor = new SURFFeatureExtractor();
            BFMatcher_L2 surfBFAssociator = new BFMatcher_L2();
            Recognizer recognizer = new Recognizer(surfExtractor, surfBFAssociator, DBdirpath);


            Long start = System.currentTimeMillis();
            String result = recognizer.recognize(Highgui.imread(query, Highgui.CV_LOAD_IMAGE_GRAYSCALE));
            if(result == null)
                result = "None";
            System.out.println("Input:" + query + " Matched:" + result);
            System.out.println("Time:" + (System.currentTimeMillis() - start));
        }
        System.exit(1);
    }
}
