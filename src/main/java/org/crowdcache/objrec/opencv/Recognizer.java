package org.crowdcache.objrec.opencv;

import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.extractors.SIFTFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2_NB;
import org.crowdcache.objrec.opencv.matchers.LSHMatcher_HAM;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Map;

import static org.opencv.imgproc.Imgproc.resize;

/**
 * Created by utsav on 2/5/16.
 */
public class Recognizer
{

    private final Map<String, KeypointDescList> DB;
    private final FeatureExtractor extractor;
    private final Matcher matcher;

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
        this.matcher.train(DB);
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
        return recognize(image);
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
        return matcher.matchAll(inputKDlist);
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
            Matcher matcher = new LSHMatcher_HAM("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/lsh_pars.txt", -1);
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
