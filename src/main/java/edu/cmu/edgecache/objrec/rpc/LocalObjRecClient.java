package edu.cmu.edgecache.objrec.rpc;

import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.Recognizer;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * For only local recognition - no servers
 * Created by utsav on 2/16/17.
 */
public class LocalObjRecClient extends ObjRecClient
{
    private Recognizer recognizer;
    private String name;
    final static Logger logger = LoggerFactory.getLogger(LocalObjRecClient.class);


    /**
     *
     * @param extractor {@link FeatureExtractor} To use in the Recognizer
     * @param matcher {@link Matcher} To use in the Recognizer
     */
    public LocalObjRecClient(FeatureExtractor dbextractor, FeatureExtractor extractor, Matcher matcher, String dblistpath, String name) throws IOException
    {
        // No server, only local recognition
        super(null);
        recognizer = new Recognizer(dbextractor, extractor, matcher, dblistpath);
        this.name = name;
    }

    @Override
    public void recognize(String imagePath, ObjRecCallback cb) throws IOException
    {
        Mat image = Highgui.imread(imagePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Long start = System.currentTimeMillis();
        // Recognize from local db
        String ret = recognizer.recognize(image);
        long dur = System.currentTimeMillis() - start;
        checkAndSend(dur, ret, cb);
    }

    private void checkAndSend(long dur, String res, ObjRecCallback cb)
    {
        // Calculate comp latency
        ObjRecServiceProto.Latency.Builder complatency = ObjRecServiceProto.Latency.newBuilder().
                setComputation((int) dur).
                setName(name);
        // Check if Hit
        ObjRecServiceProto.Annotation annotation = ObjRecServiceProto.Annotation.newBuilder().
                    setAnnotation(res).
                    addLatencies(complatency).
                    build();
        cb.run(annotation);
        if (Recognizer.isValid(res))
        {
            logger.debug("*!!!Cache Hit!!!*");
        }
    }
}
