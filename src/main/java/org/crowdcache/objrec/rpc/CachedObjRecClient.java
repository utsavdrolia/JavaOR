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
        Long start = System.currentTimeMillis();
        // Extract Keypoints
        KeypointDescList kplist = recognizer.extractor.extract(imagePath);
        // Recognize from local cache
        String res = recognizer.recognize(kplist);
        long dur = System.currentTimeMillis() - start;
        checkAndSend(dur, res, Utils.serialize(kplist).build(), cb);
    }

    /**
     * Check in local cache if we have image and if not send request to cloud
     * @param features
     * @param cb
     */
    public void recognize(ObjRecServiceProto.Features features, ObjRecCallback cb)
    {
        Long start = System.currentTimeMillis();
        KeypointDescList kplist = Utils.deserialize(features);
        String res = recognizer.recognize(kplist);
        long dur = System.currentTimeMillis() - start;
        checkAndSend(dur, res, features, cb);
    }


    /**
     * Check in local cache if we have image and if not send request to cloud
     * @param imagedata
     * @param cb
     * @throws IOException
     */
    public void recognize(byte[] imagedata, ObjRecCallback cb)
    {
        Long start = System.currentTimeMillis();
        // Extract Keypoints
        KeypointDescList kplist = recognizer.extractor.extract(imagedata);
        // Recognize from local cache
        String res = recognizer.recognize(kplist);
        long dur = System.currentTimeMillis() - start;
        checkAndSend(dur, res, Utils.serialize(kplist).build(), cb);
    }

    /**
     * Check if hit, if not send to server
     * @param dur
     * @param res
     * @param features
     * @param cb
     */
    private void checkAndSend(long dur, String res, ObjRecServiceProto.Features features, ObjRecCallback cb)
    {
        // Calculate comp latency
        ObjRecServiceProto.Latency.Builder complatency = ObjRecServiceProto.Latency.newBuilder().
                setComputation((int) dur).
                setName(name);
        // Check if Hit
        if(!res.equals("None"))
        {
            ObjRecServiceProto.Annotation annotation = ObjRecServiceProto.Annotation.newBuilder().
                    setAnnotation(res).
                    addLatencies(complatency).
                    build();
            cb.run(annotation);
            System.out.println(name+ " : *!!!Cache Hit!!!*");
        }
        else
            // If not, send to server
            ObjRecServiceStub.recognizeFeatures(rpc, features, new CachedObjRecCallback(cb, complatency));
    }

    /**
     * This callback first calls the application's callback with retrieved annotation
     * and then checks cache to see if it knows about this image. If not, it will fetch the
     * features of the given image from the server.
     */
    private class CachedObjRecCallback extends ObjRecCallback
    {
        private ObjRecCallback cb;
        private long start;
        ObjRecServiceProto.Latency.Builder complatency;

        public CachedObjRecCallback(ObjRecCallback cb, ObjRecServiceProto.Latency.Builder complatency)
        {
            this.cb = cb;
            this.complatency = complatency;
            this.start = System.currentTimeMillis();
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            // Add latencies to Annotation
            ObjRecServiceProto.Annotation.Builder ann = ObjRecServiceProto.Annotation.newBuilder(annotation);
            ann.addLatencies(complatency.setNetwork((int) (System.currentTimeMillis() - start)));
            // Run the client's callback
            cb.run(ann.build());

            String annotationstring = annotation.getAnnotation();

            // Check if you have this image in your cache
            if(!annotationstring.equals("None"))
                if(!recognizer.matcher.contains(annotationstring))
                {
                    // If not, fetch from server
                    ObjRecServiceStub.getFeatures(rpc,
                            annotation,
                            new FeaturesRecvCallback(annotationstring));
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
