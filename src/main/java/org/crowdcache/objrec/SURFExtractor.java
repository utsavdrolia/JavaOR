package org.crowdcache.objrec;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.extract.ConfigExtract;
import boofcv.abst.feature.detect.extract.NonMaxSuppression;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.orientation.OrientationIntegral;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.describe.DescribePointSurf;
import boofcv.alg.feature.detect.interest.FastHessianFeatureDetector;
import boofcv.alg.transform.ii.GIntegralImageOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.feature.describe.FactoryDescribePointAlgs;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.detect.extract.FactoryFeatureExtractor;
import boofcv.factory.feature.orientation.FactoryOrientationAlgs;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.BoofDefaults;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.FastQueueList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 2/3/16.
 */
public class SURFExtractor<II extends ImageSingleBand>
{

    /**
     * Use generalized interfaces for working with SURF.  This removes much of the drudgery, but also reduces flexibility
     * and slightly increases memory and computational requirements.
     *
     * @param image Input image type. DOES NOT NEED TO BE ImageFloat32, ImageUInt8 works too
     */
    public static void easy(ImageFloat32 image)
    {
        // create the detector and descriptors
        DetectDescribePoint<ImageFloat32, BrightFeature> surf = FactoryDetectDescribe.
                surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null, ImageFloat32.class);

        // specify the image to process
        surf.detect(image);

        System.out.println("Found Features: " + surf.getNumberOfFeatures());
        System.out.println("First descriptor's first value: " + surf.getDescription(0).value[0]);
    }

    final Class<II> integralType;
    final NonMaxSuppression extractor;
    final FastHessianFeatureDetector<II> detector;
    final OrientationIntegral<II> orientation;
    final DescribePointSurf<II> descriptor;

    public SURFExtractor(Class<II> clazz)
    {
        // SURF works off of integral images
        integralType = GIntegralImageOps.getIntegralType(clazz);

        // define the feature detection algorithm
        extractor = FactoryFeatureExtractor.nonmax(new ConfigExtract(2, 0, 5, true));
        detector = new FastHessianFeatureDetector<II>(extractor, 200, 2, 9, 4, 4, 6);

        // estimate orientation
        orientation = FactoryOrientationAlgs.sliding_ii(null, integralType);

        descriptor = FactoryDescribePointAlgs.<II>surfStability(null, integralType);
    }

    /**
     * Configured exactly the same as the easy example above, but require a lot more code and a more in depth
     * understanding of how SURF works and is configured.  Instead of TupleDesc_F64, SurfFeature are computed in
     * this case.  They are almost the same as TupleDesc_F64, but contain the Laplacian's sign which can be used
     * to speed up association. That is an example of how using less generalized interfaces can improve performance.
     *
     * @param image Input image type. DOES NOT NEED TO BE ImageFloat32, ImageUInt8 works too
     */
    public KeypointDescList<ScalePoint, BrightFeature> harder(II image)
    {
        // compute the integral image of 'image'
        II integral = GeneralizedImageOps.createSingleBand(integralType, image.width, image.height);
        GIntegralImageOps.transform(image, integral);

        // detect fast hessian features
        detector.detect(integral);
        // tell algorithms which image to process
        orientation.setImage(integral);
        descriptor.setImage(integral);

        List<ScalePoint> points = detector.getFoundPoints();

        FastQueue<BrightFeature> descriptions = new FastQueue<BrightFeature>(points.size(), BrightFeature.class, false);

        for (ScalePoint p : points)
        {
            // estimate orientation
            orientation.setObjectRadius(p.scale * BoofDefaults.SURF_SCALE_TO_RADIUS);
            double angle = orientation.compute(p.x, p.y);

            // extract the SURF description for this region
            BrightFeature desc = descriptor.createDescription();
            descriptor.describe(p.x, p.y, angle, p.scale, desc);

            // save everything for processing later on
            descriptions.add(desc);
        }

        return new KeypointDescList<ScalePoint, BrightFeature>(points, descriptions);
    }

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            String inputFile = args[0];
            ImageFloat32 image = UtilImageIO.loadImage(inputFile, ImageFloat32.class);
            // run each example
            Long start = System.currentTimeMillis();
            SURFExtractor.easy(image);
            System.out.println("Finished Easy in " + (System.currentTimeMillis() - start));
//            start = System.currentTimeMillis();
//            FeatureExtractor.harder(image);
//            System.out.println("Finished Harder in " + (System.currentTimeMillis() - start));
//            System.out.println("Done!");
        }

    }
}
