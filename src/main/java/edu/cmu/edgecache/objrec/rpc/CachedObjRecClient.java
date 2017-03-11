package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import edu.cmu.edgecache.objrec.opencv.*;
import edu.cmu.edgecache.recog.AbstractRecogCache;
import edu.cmu.edgecache.recog.LFURecogCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by utsav on 6/16/16.
 * Has an inbuilt knownItems to first check before sending requests to Server
 */
public class CachedObjRecClient extends ObjRecClient
{
    // Cache
    private AbstractRecogCache<String, KeypointDescList> recogCache;
    // Recognizer
    private Recognizer recognizer;
    private String name;
    private boolean isCacheEnabled = false;

    final static Logger logger = LoggerFactory.getLogger(CachedObjRecClient.class);


    /**
     *
     * @param extractor {@link FeatureExtractor} To use in the Recognizer
     * @param matcher {@link Matcher} To use in the Recognizer
     * @param server {@link ObjRecServer} to connect to
     */
    public CachedObjRecClient(FeatureExtractor extractor, Matcher matcher, String server, String name, Integer cache_size)
    {
        super(server);
        recognizer = new Recognizer(extractor, matcher);
        recogCache = new LFURecogCache<>(new ImageRecognizerInterface(recognizer), cache_size);
        this.name = name;
        if(cache_size > 0)
            isCacheEnabled = true;
        logger.debug("Cache enabled:" + isCacheEnabled);
    }

    public CachedObjRecClient(Recognizer recognizer, AbstractRecogCache<String, KeypointDescList> recogCache, String serverAdd, String name, boolean enableCache)
    {
        super(serverAdd);
        this.recognizer = recognizer;
        this.recogCache = recogCache;
        this.name = name;
        isCacheEnabled = enableCache;
        logger.debug("Cache enabled:" + isCacheEnabled);

    }

    /**
     * Check in local knownItems if we have image and if not send request to cloud. Only called when in device
     * @param imagePath
     * @param cb
     * @throws IOException
     */
    @Override
    public void recognize(String imagePath, ObjRecCallback cb) throws IOException
    {
        Long start = System.currentTimeMillis();
        // Extract Keypoints
        KeypointDescList kplist = recognizer.extractor.extract(imagePath);
        // Recognize from local cache
        String res = recogCache.invalid();
        if(isCacheEnabled)
            res = recogCache.get(kplist);
        long dur = System.currentTimeMillis() - start;
        ObjRecServiceProto.Features features = Utils.serialize(kplist)
                .setReqId(ObjRecServiceProto.RequestID.newBuilder()
                        .setName(this.client_name)
                        .setReqId(this.req_counter.incrementAndGet()))
                .build();
        postRecognition(dur, 0l, res, features, cb);
    }

    /**
     * Check in local knownItems if we have image and if not send request to cloud. Only called when in edge
     * @param request
     * @param cb
     */
    public void recognize(ObjRecServiceProto.Image request, Long req_recv_ts, ObjRecCallback cb)
    {
        Long start = System.currentTimeMillis();
        // Extract Keypoints
        KeypointDescList kplist = recognizer.extractor.extract(request.toByteArray());
        String res = recogCache.invalid();
        if(isCacheEnabled)
            // Recognize from local cache
            res = recogCache.get(kplist);
        long dur = System.currentTimeMillis() - start;
        ObjRecServiceProto.Features features = Utils.serialize(kplist)
                .setReqId(request.getReqId())
                .build();
        postRecognition(dur, start - req_recv_ts, res, features, cb);
    }

    /**
     * Check in local knownItems if we have image and if not send request to cloud. Only called when in edge
     * @param features
     * @param client_cb
     */
    public void recognize(ObjRecServiceProto.Features features, Long req_recv_ts, ObjRecCallback client_cb)
    {
        Long start = System.currentTimeMillis();
        KeypointDescList kplist = Utils.deserialize(features);
        String res = recogCache.invalid();
        if(isCacheEnabled)
            res = recogCache.get(kplist);
        long dur = System.currentTimeMillis() - start;
        postRecognition(dur, start - req_recv_ts, res, features, client_cb);
    }

    /**
     * Check if hit, if not send to server
     * @param lookup_latency
     * @param res
     * @param features
     * @param client_cb
     */
    protected void postRecognition(long lookup_latency, long time_in_queue, String res, ObjRecServiceProto.Features features, ObjRecCallback client_cb)
    {
        // Calculate comp latency
        ObjRecServiceProto.Latency.Builder complatency = ObjRecServiceProto.Latency.newBuilder().
                setComputation((int) lookup_latency).
                setInQueue((int) time_in_queue).
                setName(name).
                setSize(recogCache.getSize());
        // Check if Hit
        if (recogCache.isValid(res))
            onHit(features, res, complatency, client_cb);
        else
            onMiss(features, complatency, client_cb);
    }

    /**
     * Calls the client callback with successful recognition result
     * @param features
     * @param res
     * @param complatency
     * @param client_cb
     */
    protected void onHit(ObjRecServiceProto.Features features,
                         String res,
                         ObjRecServiceProto.Latency.Builder complatency,
                         ObjRecCallback client_cb)
    {
        ObjRecServiceProto.Annotation annotation = ObjRecServiceProto.Annotation.newBuilder().
                setAnnotation(res).
                addLatencies(complatency)
                .setReqId(features.getReqId())
                .build();
        client_cb.run(annotation);
        logger.debug("*!!!Cache Hit!!!*");
    }

    /**
     * Calls server with features for recognition
     * @param features
     * @param client_cb
     * @param complatency
     */
    protected void onMiss(ObjRecServiceProto.Features features, ObjRecServiceProto.Latency.Builder complatency, ObjRecCallback client_cb)
    {
        ObjRecServiceStub.recognizeFeatures(rpc, features, new CachedObjRecCallback(client_cb, complatency));
    }

    /**
     * Get features for given ID/key
     * @param annotation
     * @return
     */
    public KeypointDescList getFeatures(String annotation)
    {
        return recogCache.getValue(annotation);
    }

    /**
     * This callback first calls the application's callback with retrieved annotation
     * and then checks cache to see if it knows about this image. If not, it will fetch the
     * features of the given image from the server.
     */
    protected class CachedObjRecCallback extends ObjRecCallback
    {
        protected ObjRecCallback app_cb;
        protected long start;
        protected ObjRecServiceProto.Latency.Builder complatency;

        CachedObjRecCallback(ObjRecCallback cb, ObjRecServiceProto.Latency.Builder complatency)
        {
            this.app_cb = cb;
            this.complatency = complatency;
            this.start = System.currentTimeMillis();
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            int latency = (int) (System.currentTimeMillis() - start);
            // Add latencies to Annotation
            ObjRecServiceProto.Annotation.Builder ann = ObjRecServiceProto.Annotation.newBuilder(annotation);
            ann.addLatencies(complatency.setNextLevel(latency));
            // Run the client's callback
            app_cb.run(ann.build());

            if (isCacheEnabled)
            {
                String annotationstring = annotation.getAnnotation();

                // Update cache about latency
                recogCache.updateMissLatency(latency);

                // Check if you have this image in your knownItems
                if (recogCache.isValid(annotationstring))
                    if (!recogCache.contains(annotationstring))
                    {
                        // If not, fetch from server
                        ObjRecServiceStub.getFeatures(rpc,
                                                      annotation,
                                                      new FeaturesRecvCallback(annotationstring));
                    } else
                    {
                        recogCache.put(annotationstring, null);
                        logger.debug(name + " : Present in Cache but not matched");
                    }
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
        private long start;
        public FeaturesRecvCallback(String annotation)
        {
            this.annotation = annotation;
            this.start = System.currentTimeMillis();
        }

        @Override
        public void run(ObjRecServiceProto.Features features)
        {
            long compstart = System.currentTimeMillis();
            logger.debug(name+ " : Received Features from Server");
            KeypointDescList kdlist = Utils.deserialize(features);
            recogCache.put(annotation, kdlist);
            long compdur = System.currentTimeMillis() - compstart;
            long netdur = compstart - start;
        }
    }
}
