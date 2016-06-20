package org.crowdcache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;

import java.io.IOException;

/**
 * Created by utsav on 6/16/16.
 * Has an inbuilt cache to first check before sending requests to Server
 */
public class CachedObjRecClient extends ObjRecClient
{
    // Cache
    protected Recognizer recognizer;
    private String name;

    /**
     *
     * @param extractor {@link FeatureExtractor} To use in the cache
     * @param matcher {@link Matcher} To use in the cache
     * @param server {@link ObjRecServer} to connect to
     */
    public CachedObjRecClient(FeatureExtractor extractor, Matcher matcher, String server, String name)
    {
        super(server);
        recognizer = new Recognizer(extractor, matcher);
        this.name = name;
    }

    @Override
    public void recognize(String imagePath, ObjRecCallback cb) throws IOException
    {
        // Extract Keypoints
        KeypointDescList kplist = recognizer.extractor.extract(imagePath);
        // Recognize from local cache
        String res = recognizer.recognize(kplist);
        // Check if Hit
        if(!res.equals("None"))
        {
            cb.run(res);
            System.out.println(name+ " : *!!!Cache Hit!!!*");
        }
        else
            // If not, send to server
            ObjRecServiceStub.recognizeFeatures(rpc, Utils.serialize(kplist), new CachedObjRecCallback(cb));
    }

    /**
     * Check in local cache if we have image and if not send request to cloud
     * @param features
     * @param cb
     */
    public void recognize(ObjRecServiceProto.Features features, ObjRecCallback cb)
    {
        KeypointDescList kplist = Utils.deserialize(features);
        String res = recognizer.recognize(kplist);
        // Check if Hit
        if(!res.equals("None"))
        {
            cb.run(res);
            System.out.println(name+ " : *!!!Cache Hit!!!*");
        }
        else
            // If not, send to server
            ObjRecServiceStub.recognizeFeatures(rpc, features, new CachedObjRecCallback(cb));
    }


    /**
     * Check in local cache if we have image and if not send request to cloud
     * @param imagePath
     * @param cb
     * @throws IOException
     */
    public void recognize(byte[] imagePath, ObjRecCallback cb)
    {
        // Extract Keypoints
        KeypointDescList kplist = recognizer.extractor.extract(imagePath);
        // Recognize from local cache
        String res = recognizer.recognize(kplist);
        // Check if Hit
        if(!res.equals("None"))
        {
            cb.run(res);
            System.out.println(name+ " : *!!!Cache Hit!!!*");
        }
        else
            // If not, send to server
            ObjRecServiceStub.recognizeFeatures(rpc, Utils.serialize(kplist), new CachedObjRecCallback(cb));
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
                    // If not, fetch from server
                    ObjRecServiceStub.getFeatures(rpc,
                            ObjRecServiceProto.Annotation.newBuilder().setAnnotation(annotation).build(),
                            new FeaturesRecvCallback(annotation));
                }
                else
                {
                    System.out.println(name+ " : Present in Cache but not matched");
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
            System.out.println(name+ " : Received Features from Server");
            KeypointDescList kdlist = Utils.deserialize(features);
            recognizer.matcher.insert(annotation, kdlist);
        }
    }
}
