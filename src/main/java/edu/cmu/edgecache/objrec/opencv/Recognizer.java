package edu.cmu.edgecache.objrec.opencv;

import edu.cmu.edgecache.objrec.opencv.extractors.ORB;
import edu.cmu.edgecache.objrec.opencv.matchers.LSHMatcher_HAM;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by utsav on 2/5/16.
 */
public class Recognizer
{

    public static final String INVALID = Matcher.INVALID;
    public FeatureExtractor extractor;
    public Matcher matcher;
    private final AtomicLong recv_counter = new AtomicLong(0L);
    private final AtomicLong send_counter = new AtomicLong(0L);
    final static Logger logger = LoggerFactory.getLogger(Recognizer.class);

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
        this.matcher.train(DBLoader.processDB(dblistpath, dbextractor));
    }

    public Recognizer(FeatureExtractor extractor, Matcher matcher)
    {
        this.extractor = extractor;
        this.matcher = matcher;
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
        //System.out.println("KPs in Input:" + inputKDlist.points.size());
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
        Mat dst = new Mat();
        CVUtil.resize(image, dst);
        return recognize(dst);
    }

    /**
     * Read image from path. Extract features from image, match against all of the images in the DB.
     * @param imagePath Path if image
     */
    public String recognize(String imagePath)
    {
        Mat image = Highgui.imread(imagePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat dst = new Mat();
        CVUtil.resize(image, dst);
        String ret = recognize(dst);
        image.release();
        dst.release();
        return ret;
    }


    /**
     * match features against all of the images in the DB.
     * @param inputKDlist
     * @return
     */
    public String recognize(KeypointDescList inputKDlist)
    {
        logger.debug("Recognizer: Received Request Number:" + recv_counter.incrementAndGet());
        String ret = matcher.matchAll(inputKDlist);
        logger.debug("Recognizer: Sending Request Number:" + send_counter.incrementAndGet());
        return ret;
    }

    /**
     * Is the result valid or unknown?
     * @param result
     * @return
     */
    public static boolean isValid(String result)
    {
        return !result.equals(Recognizer.INVALID);
    }

    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 2)
        {
            String query = args[0];
            String DBdirpath = args[1];
            FeatureExtractor extractor = new ORB("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/orb_pars.txt");
            FeatureExtractor dbextractor = new ORB("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/orb_pars_db.txt");
            Matcher matcher = new LSHMatcher_HAM("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/lsh_pars.txt");
            Recognizer recognizer = new Recognizer(dbextractor, extractor, matcher, DBdirpath);

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
