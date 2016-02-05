package org.crowdcache.objrec;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.homography.Homography2D_F64;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/3/16.
 */
public class SurfBFAssociator<T extends ImageSingleBand>
{
    private final static Double RATIO_THRESHOLD = 0.8;
    private final static Integer MATCH_THRESHOLD = 20;
    private final Class<T> imageType;
    private final ScoreAssociation<BrightFeature> scorer;
    SURFExtractor<T> extractor;
    ExecutorService executorService;
    HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> DB;

    public SurfBFAssociator(Class<T> imageType, String dirpath)
    {
        this.extractor = new SURFExtractor<T>(imageType);
        this.scorer = FactoryAssociation.scoreEuclidean(BrightFeature.class, true);
        this.imageType = imageType;
        DBLoader<T> dbLoader = new DBLoader<T>(imageType);
        this.DB = dbLoader.processDB(dirpath);
        this.executorService = Executors.newFixedThreadPool(24);
    }

    /**
     * Associate point features in the two descriptors.
     */
    public FastQueue<AssociatedIndex> associate(FastQueue<BrightFeature> descA, FastQueue<BrightFeature> descB)
    {
        // Associate features between the two images
        AssociateDescription<BrightFeature> ass = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
        ass.setSource(descA);
        ass.setDestination(descB);
        ass.associate();
        return ass.getMatches();
    }

    /**
     * Extract features from image, match against all of the images in the DB.
     * @param image
     * @return The matched image name or null if no match
     */
    public String associateWithDB(T image)
    {
        String ret = null;
        Double max_ratio = Double.MIN_VALUE;
        HashMap<String, Future<Double>> matches = new HashMap<String, Future<Double>>();
        Double min_score = Double.MAX_VALUE;
        HashMap<String, String> results = new HashMap<String, String>();

        //-- Extract input image KP and Desc --
        final KeypointDescList<ScalePoint, BrightFeature> inputKDlist = this.extractor.harder(image);
        //--

        //-- Match against all DB --
        for(final Map.Entry<String, KeypointDescList<ScalePoint, BrightFeature>> entry : DB.entrySet())
        {
            matches.put(entry.getKey(), executorService.submit(new Callable<Double>()
            {
                public Double call() throws Exception
                {
                    FastQueue<AssociatedIndex> match = associate(entry.getValue().descriptions, inputKDlist.descriptions);
                    Double ratio = Double.MIN_VALUE;
                    if(match.size() > MATCH_THRESHOLD)
                        ratio = geometricVerification(match, entry.getValue().points, inputKDlist.points);
                    return ratio;
                }
            }));
        }

        for(Map.Entry<String, Future<Double>> future:matches.entrySet())
        {
            try
            {
                Double matchratio = future.getValue().get();
                if (matchratio > max_ratio)
                {
                    max_ratio = matchratio;
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


    /**
     * Returns the ratio of interior points to matched points after applying the homography, if the ratio is above
     * {@link SurfBFAssociator<>.RATIO_THRESHOLD}
     * @param matches
     * @param pointsA
     * @param pointsB
     * @return the ratio of interior points to matched points
     */
    private Double geometricVerification(FastQueue<AssociatedIndex> matches, List<ScalePoint> pointsA, List<ScalePoint> pointsB)
    {
        List<AssociatedPair> pairs = new ArrayList<AssociatedPair>();

        for( int i = 0; i < matches.size(); i++ )
        {
            AssociatedIndex match = matches.get(i);
//            if(match.fitScore < 0.2)
//            {
                ScalePoint a = pointsA.get(match.src);
                ScalePoint b = pointsB.get(match.dst);
                pairs.add(new AssociatedPair(a,b,false));
//            }
        }
        // fit the images using a homography.  This works well for rotations and distant objects.
        ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher =
                FactoryMultiViewRobust.homographyRansac(null, new ConfigRansac(100, pairs.size()));

        if(modelMatcher.process(pairs))
        {
            Double good_matches = (double) matches.size();
            Double interior_points = (double) modelMatcher.getMatchSet().size();
            Double ratio = interior_points/good_matches;
            if(ratio > RATIO_THRESHOLD)
                return ratio;
            else
                return Double.MIN_VALUE;
        }
        else return Double.MIN_VALUE;
    }

    public static void main(String args[])
    {
        if (args.length == 2)
        {
            String inputFile = args[0];
            String dirpath = args[1];
            SurfBFAssociator<ImageUInt8> surfAssociator = new SurfBFAssociator<ImageUInt8>(ImageUInt8.class, dirpath);

            Long start = System.currentTimeMillis();
            String result = surfAssociator.associateWithDB(UtilImageIO.loadImage(inputFile, ImageUInt8.class));
            if(result == null)
                result = "None";
            System.out.println("Input:" + inputFile + " Matched:" + result);
            System.out.println("Time:" + (System.currentTimeMillis() - start));
        }
    }
}
