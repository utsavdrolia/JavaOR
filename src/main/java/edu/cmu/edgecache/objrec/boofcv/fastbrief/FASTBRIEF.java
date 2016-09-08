package edu.cmu.edgecache.objrec.boofcv.fastbrief;

import boofcv.abst.feature.describe.ConfigBrief;
import boofcv.abst.feature.describe.DescribeRegionPoint;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.detect.interest.GeneralFeatureDetector;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.factory.feature.describe.FactoryDescribeRegionPoint;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.detect.interest.FactoryDetectPoint;
import boofcv.factory.feature.detect.interest.FactoryInterestPoint;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.TupleDesc_B;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import edu.cmu.edgecache.objrec.boofcv.FeatureAssociator;
import edu.cmu.edgecache.objrec.boofcv.FeatureExtractor;
import edu.cmu.edgecache.objrec.boofcv.KeypointDescList;
import georegression.struct.point.Point2D_F64;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 2/5/16.
 */
public class FASTBRIEF implements FeatureExtractor<ImageUInt8, Point2D_F64, TupleDesc_B>, FeatureAssociator<Point2D_F64, TupleDesc_B>
{
    final static Class<ImageUInt8> IMAGETYPE = ImageUInt8.class;
    /**
     * Any arbitrary implementation of InterestPointDetector, OrientationImage, DescribeRegionPoint
     * can be combined into DetectDescribePoint.  The syntax is more complex, but the end result is more flexible.
     * This should only be done if there isn't a pre-made DetectDescribePoint.
     */
    public <II extends ImageSingleBand> KeypointDescList<Point2D_F64, TupleDesc_B> extract(ImageUInt8 image)
    {
        Class<II> derivType = GImageDerivativeOps.getDerivativeType(IMAGETYPE);
        // create a corner detector
        GeneralFeatureDetector<ImageUInt8, II> corner = FactoryDetectPoint.createShiTomasi(new ConfigGeneralDetector(500, null), false, derivType);
        InterestPointDetector<ImageUInt8> detector = FactoryInterestPoint.wrapPoint(corner, 1, IMAGETYPE, derivType);
//        GeneralFeatureDetector<ImageUInt8, II> corner = FactoryDetectPoint.createFast(new ConfigFast(20, 12), new ConfigGeneralDetector(500, null), IMAGETYPE);
//        InterestPointDetector<ImageUInt8> detector = FactoryInterestPoint.wrapPoint(corner, 1, IMAGETYPE, derivType);
        // describe points using BRIEF
        DescribeRegionPoint<ImageUInt8, TupleDesc_B> describe = FactoryDescribeRegionPoint.brief(new ConfigBrief(true), IMAGETYPE);
        // Combine together.
        // NOTE: orientation will not be estimated
        DetectDescribePoint<ImageUInt8, TupleDesc_B> detDesc = FactoryDetectDescribe.fuseTogether(detector, null, describe);
        // stores the location of detected interest points
        List<Point2D_F64> points = new ArrayList<Point2D_F64>();
        // stores the description of detected interest points
        FastQueue<TupleDesc_B> desc = UtilFeature.createQueue(detDesc,500);

        Long start = System.currentTimeMillis();
        detDesc.detect(image);
        Long end = System.currentTimeMillis();
        System.out.println("Found " + detDesc.getNumberOfFeatures()+ " in " + ( end - start));


        for( int i = 0; i < detDesc.getNumberOfFeatures(); i++ )
        {
            points.add( detDesc.getLocation(i).copy() );
            desc.grow().setTo(detDesc.getDescription(i));
        }

        return new KeypointDescList<Point2D_F64, TupleDesc_B>(points, desc);
    }


    public Double associate(KeypointDescList<Point2D_F64, TupleDesc_B> kp1, KeypointDescList<Point2D_F64, TupleDesc_B> kp2)
    {
        return null;
    }

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            String inputFile = args[0];
            ImageUInt8 image = UtilImageIO.loadImage(inputFile, ImageUInt8.class);
            // run each example
            FASTBRIEF fb = new FASTBRIEF();
            Long start = System.currentTimeMillis();
            fb.extract(image);
            System.out.println("Finished Easy in " + (System.currentTimeMillis() - start));
        }

    }
}
