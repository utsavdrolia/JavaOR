package org.crowdcache.objrec.surf;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.geo.AssociatedPair;
import georegression.struct.homography.Homography2D_F64;
import org.crowdcache.objrec.FeatureAssociator;
import org.crowdcache.objrec.KeypointDescList;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/3/16.
 */
public class SurfBFAssociator implements FeatureAssociator<ScalePoint, BrightFeature>
{
    private final static Double RATIO_THRESHOLD = 0.6;
    private final static Integer MATCH_THRESHOLD = 20;
    private final ScoreAssociation<BrightFeature> scorer;
    ExecutorService executorService;

    public SurfBFAssociator()
    {
        this.scorer = FactoryAssociation.scoreEuclidean(BrightFeature.class, true);
        this.executorService = Executors.newFixedThreadPool(24);
    }

    /**
     * Associate point features in the two descriptors.
     * Then verify using geometry.
     */
    public Double associate(KeypointDescList<ScalePoint, BrightFeature> descA, KeypointDescList<ScalePoint, BrightFeature> descB)
    {
        // Associate features between the two images
        AssociateDescription<BrightFeature> ass = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
        Double ratio = Double.MIN_VALUE;

        FastQueue<AssociatedIndex> match;
        ass.setSource(descA.descriptions);
        ass.setDestination(descB.descriptions);
        ass.associate();
        match = ass.getMatches();

        // Verify using geometry
        if(match.size() > MATCH_THRESHOLD)
            ratio = geometricVerification(match, descA.points, descB.points);
        return ratio;
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

}
