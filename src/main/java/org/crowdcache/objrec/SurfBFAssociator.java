package org.crowdcache.objrec;

import boofcv.abst.feature.associate.Associate;
import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.associate.WrapAssociateSurfBasic;
import boofcv.alg.feature.associate.AssociateSurfBasic;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
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
     * Detect and associate point features in the two images.  Display the results.
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

    public void associateWithDB(T image)
    {
        HashMap<String, Future<ArrayList<Integer>>> matches = new HashMap<String, Future<ArrayList<Integer>>>();
        Double min_score = Double.MAX_VALUE;
        HashMap<String, String> results = new HashMap<String, String>();

        // Extract input image KP and Desc
        final KeypointDescList<ScalePoint, BrightFeature> inputKDlist = this.extractor.harder(image);

        // Match against all DB
        for(final Map.Entry<String, KeypointDescList<ScalePoint, BrightFeature>> entry : DB.entrySet())
        {
            matches.put(entry.getKey(), executorService.submit(new Callable<ArrayList<Integer>>()
            {
                public ArrayList<Integer> call() throws Exception
                {
                    ArrayList<Integer> ret = new ArrayList<Integer>();
                    FastQueue<AssociatedIndex> match = associate(entry.getValue().descriptions, inputKDlist.descriptions);
                    Integer h_matches = 0;
                    if(match.size() > 20)
                        h_matches = computeHomography(match, entry.getValue().points, inputKDlist.points);
                    ret.add(match.size());
                    ret.add(h_matches);
                    return ret;
                }
            }));
        }


        for(Map.Entry<String, Future<ArrayList<Integer>>> future:matches.entrySet())
        {
            try
            {
                ArrayList<Integer> match = future.getValue().get();
                System.out.println("Image:" + future.getKey() + " Num. Matches:" + match.get(0) + " Good Points:" + match.get(1));
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
    }


    /**
     * Returns the number of valid points after applying the homography
     * @param matches
     * @param pointsA
     * @param pointsB
     * @return
     */
    private Integer computeHomography(FastQueue<AssociatedIndex> matches, List<ScalePoint> pointsA, List<ScalePoint> pointsB)
    {
        List<AssociatedPair> pairs = new ArrayList<AssociatedPair>();

        for( int i = 0; i < matches.size(); i++ )
        {
            AssociatedIndex match = matches.get(i);
            if(match.fitScore < 0.2)
            {
                ScalePoint a = pointsA.get(match.src);
                ScalePoint b = pointsB.get(match.dst);
                pairs.add(new AssociatedPair(a,b,false));
            }
        }
        // fit the images using a homography.  This works well for rotations and distant objects.
        ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher =
                FactoryMultiViewRobust.homographyRansac(null, new ConfigRansac(100, (int) (0.8*pairs.size())));

        if(modelMatcher.process(pairs))
            return modelMatcher.getMatchSet().size();
        else return -1;
    }

    public static void main(String args[])
    {
        if (args.length == 2)
        {
            String inputFile = args[0];
            String dirpath = args[1];
            SurfBFAssociator<ImageUInt8> surfAssociator = new SurfBFAssociator<ImageUInt8>(ImageUInt8.class, dirpath);

            Long start = System.currentTimeMillis();
            surfAssociator.associateWithDB(UtilImageIO.loadImage(inputFile, ImageUInt8.class));
            System.out.println("Time:" + (System.currentTimeMillis() - start));
        }
    }
}
