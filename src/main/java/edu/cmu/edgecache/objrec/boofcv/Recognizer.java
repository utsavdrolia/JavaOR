package edu.cmu.edgecache.objrec.boofcv;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import edu.cmu.edgecache.objrec.boofcv.surf.SURFExtractor;
import edu.cmu.edgecache.objrec.boofcv.surf.SurfBFAssociator;
import georegression.struct.point.Point2D_F64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/5/16.
 */
public class Recognizer<II extends ImageSingleBand, K extends Point2D_F64, D extends TupleDesc>
{
    private final HashMap<String, KeypointDescList<K ,D>> DB;
    private final FeatureExtractor<II, K, D> extractor;
    private final ExecutorService executorService;
    private final FeatureAssociator<K, D> associator;

    /**
     * Loads the DB in memory so that it can be queried repeatedly using the recognize function
     * @param extractor Which {@link FeatureExtractor to use}
     * @param associator Which {@link FeatureAssociator to use}
     * @param dblistpath Where is the DB located
     * @param imageType Type of {@link ImageSingleBand}
     */
    public Recognizer(FeatureExtractor<II, K, D> extractor, FeatureAssociator<K, D> associator, String dblistpath, Class<II> imageType) throws IOException
    {
        this.extractor = extractor;
        this.associator = associator;
        DBLoader<II, K, D> dbLoader = new DBLoader<II, K, D>(imageType, extractor);
        this.DB = dbLoader.processDB(dblistpath);
        this.executorService = Executors.newFixedThreadPool(24);
    }

    /**
     * Extract features from image, match against all of the images in the DB.
     * @param image Input image
     * @return The matched image name or null if no match
     */
    public String recognize(II image)
    {
        String ret = null;
        Double score = Double.MIN_VALUE;
        HashMap<String, Future<Double>> matches = new HashMap<String, Future<Double>>();

        //-- Extract input image KP and Desc --
        final KeypointDescList<K, D> inputKDlist = this.extractor.extract(image);
        //--

        //-- Match against all DB --
        for(final Map.Entry<String, KeypointDescList<K, D>> entry : DB.entrySet())
        {
            matches.put(entry.getKey(), executorService.submit(new Callable<Double>()
            {
                public Double call() throws Exception
                {
                    return associator.associate(entry.getValue(), inputKDlist);
                }
            }));
        }

        for(Map.Entry<String, Future<Double>> future:matches.entrySet())
        {
            try
            {
                Double matchscore = future.getValue().get();
                if (matchscore > score)
                {
                    score = matchscore;
                    ret = future.getKey();
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
        if (args.length == 2)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            SURFExtractor surfExtractor = new SURFExtractor();
            SurfBFAssociator surfBFAssociator = new SurfBFAssociator();
            Recognizer<ImageUInt8, ScalePoint, BrightFeature> recognizer = new Recognizer<ImageUInt8, ScalePoint, BrightFeature>(surfExtractor, surfBFAssociator, DBdirpath, ImageUInt8.class);


            Long start = System.currentTimeMillis();
            String result = recognizer.recognize(UtilImageIO.loadImage(queryList, ImageUInt8.class));
            if(result == null)
                result = "None";
            System.out.println("Input:" + queryList + " Matched:" + result);
            System.out.println("Time:" + (System.currentTimeMillis() - start));
        }
        System.exit(1);
    }
}
