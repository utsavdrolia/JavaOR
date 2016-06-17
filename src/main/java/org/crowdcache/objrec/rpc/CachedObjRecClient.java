package org.crowdcache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 6/16/16.
 * Has an inbuilt cache to first check before sending requests to Server
 */
public class CachedObjRecClient extends ObjRecClient
{
    // Cache
    private Recognizer recognizer;

    /**
     *
     * @param extractor {@link FeatureExtractor} To use in the cache
     * @param matcher {@link Matcher} To use in the cache
     * @param server {@link ObjRecServer} to connect to
     */
    public CachedObjRecClient(FeatureExtractor extractor, Matcher matcher, String server)
    {
        super(server);
        recognizer = new Recognizer(extractor, matcher);
    }

    @Override
    public void recognize(String imagePath, ObjRecCallback cb) throws IOException
    {
        KeypointDescList kplist = recognizer.extractor.extract(imagePath);
        String res = recognizer.recognize(kplist);
        if(!res.equals("None"))
        {
            cb.run(res);
            System.out.println("*!!!Cache Hit!!!*");
        }
        else
        ObjRecServiceStub.recognizeFeatures(rpc, Utils.serialize(kplist), new CachedObjRecCallback(cb));
//            super.recognize(imagePath, new CachedObjRecCallback(cb));
    }

    /**
     * This callback first calls the application's callback with retrieved annotation
     * and then checks cache to see if it knows about this image. If not, it will fetch the
     * features of the given image from the server.
     */
    private class CachedObjRecCallback extends ObjRecCallback
    {
        private ObjRecCallback cb;

        public CachedObjRecCallback(ObjRecCallback cb)
        {
            this.cb = cb;
        }

        @Override
        public void run(String annotation)
        {
            // Run the client's callback
            cb.run(annotation);
            // Check if you have this image in your cache
            if(!annotation.equals("None"))
                if(!recognizer.matcher.contains(annotation))
                {
                    ObjRecServiceStub.getFeatures(rpc,
                            ObjRecServiceProto.Annotation.newBuilder().setAnnotation(annotation).build(),
                            new FeaturesRecvCallback(annotation));
                }
                else
                {
                    System.out.println("Present in Cache but not matched");
                }
        }
    }

    /**
     * Callback that is called when features are received from server.
     * Inserts the features into the cache
     */
    private class FeaturesRecvCallback implements RpcCallback<ObjRecServiceProto.Features>
    {
        private String annotation;

        public FeaturesRecvCallback(String annotation)
        {
            this.annotation = annotation;
        }

        @Override
        public void run(ObjRecServiceProto.Features features)
        {
            System.out.println("Received Features from Server");
            KeypointDescList kdlist = Utils.deserialize(features);
            recognizer.matcher.insert(annotation, kdlist);
        }
    }
}
