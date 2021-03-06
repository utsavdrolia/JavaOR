package edu.cmu.edgecache.objrec.opencv.matchers;

import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.extractors.HSVHist;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by utsav on 2/22/16.
 */
public class HistMatcher extends Matcher
{

    public HistMatcher()
    {
        super();
    }

    /**
     * Match the 2 images and return a match score
     *
     * @param dbImage
     * @param sceneImage
     * @return
     */
    @Override
    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage)
    {
        Mat db, scene;

        if(dbImage.descriptions.depth() != CvType.CV_32F)
        {
            db = new Mat();
            dbImage.descriptions.convertTo(db, CvType.CV_32F);
        }
        else
            db = dbImage.descriptions;
        if(sceneImage.descriptions.depth() != CvType.CV_32F)
        {
            scene = new Mat();
            sceneImage.descriptions.convertTo(scene, CvType.CV_32F);
        }
        else
            scene = sceneImage.descriptions;

        return Imgproc.compareHist(db, scene, Imgproc.CV_COMP_BHATTACHARYYA);
    }

    @Override
    public String _matchAll(KeypointDescList sceneImage)
    {
        return null;
    }

    @Override
    public Matcher newMatcher() {
        return new HistMatcher();
    }

    @Override
    protected void _insert(String name, KeypointDescList kplist)
    {

    }

    @Override
    protected void _train()
    {

    }

    public static void main(String args[])
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length > 0)
        {
            String queryFile = args[0];
            String trainFile = args[1];
            // run each example
            KeypointDescList qpoints = new HSVHist().extract(queryFile);
            KeypointDescList tpoints = new HSVHist().extract(trainFile);
            Long start = System.currentTimeMillis();
            Double matches = new HistMatcher().match(qpoints, tpoints);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Score:" + matches);
        }

    }
}
