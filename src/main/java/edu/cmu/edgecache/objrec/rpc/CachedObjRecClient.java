package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import edu.cmu.edgecache.objrec.opencv.*;
import edu.cmu.edgecache.recog.AbstractRecogCache;
import edu.cmu.edgecache.recog.LFURecogCache;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by utsav on 6/16/16.
 * Has an inbuilt knownItems to first check before sending requests to Server
 */
public class CachedObjRecClient extends ObjRecClient
{
    // Cache
    protected AbstractRecogCache<String, KeypointDescList> recogCache;
    protected Integer num_cache_features;
    // Recognizer
    protected Recognizer recognizer = null;
    protected String name;
    private boolean isCacheEnabled = false;

    // Lookback queue
    protected Set<String> feature_request_lookback;

    // Compute time tracker
    protected SynchronizedDescriptiveStatistics computeTime = new SynchronizedDescriptiveStatistics(100);

    final static Logger logger = LoggerFactory.getLogger(CachedObjRecClient.class);

    /**
     *  @param extractor {@link FeatureExtractor} To use in the Recognizer
     * @param matcher {@link Matcher} To use in the Recognizer
     * @param server {@link ObjRecServer} to connect to
     * @param num_cache_features
     */
    public CachedObjRecClient(FeatureExtractor extractor,
                              Matcher matcher,
                              String server,
                              String name,
                              Integer cache_size,
                              Integer num_cache_features)
    {
        super(server);
        this.num_cache_features = num_cache_features;
        feature_request_lookback = Collections.synchronizedSet(new LinkedHashSet<String>());
        recognizer = new Recognizer(extractor, matcher);
        recogCache = new LFURecogCache<>(new ImageRecognizerInterface(recognizer), cache_size);
        this.name = name;
        if(cache_size > 0)
            isCacheEnabled = true;
        logger.debug("Cache enabled:" + isCacheEnabled);
    }

    public CachedObjRecClient(Recognizer recognizer,
                              AbstractRecogCache<String, KeypointDescList> recogCache,
                              String serverAdd,
                              Integer num_cache_features,
                              String name,
                              boolean enableCache)
    {
        super(serverAdd);
        this.recognizer = recognizer;
        this.recogCache = recogCache;
        this.num_cache_features = num_cache_features;
        feature_request_lookback = Collections.synchronizedSet(new LinkedHashSet<String>());
        this.name = name;
        isCacheEnabled = enableCache;
        logger.debug("Cache enabled:" + isCacheEnabled);
    }

    /**
     * Check in local knownItems if we have image and if not send request to cloud. Only called when in device
     * @param imagePath
     * @param cb
     * @param l
     * @throws IOException
     */
    @Override
    public void recognize(String imagePath, ObjRecCallback cb, long startTime) throws IOException
    {
        logger.debug("Received image path");
        // Load image into memory
        Mat image = Highgui.imread(imagePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        logger.debug("Loaded Image");
        Long start = startTime;

        ObjRecServiceProto.Features.Builder features = ObjRecServiceProto.Features.newBuilder()
                .setReqId(ObjRecServiceProto.RequestID.newBuilder()
                                  .setName(this.client_name)
                                  .setReqId(this.req_counter.incrementAndGet()));

        // Recognize from local cache
        String res = recogCache.invalid();
        long dur = 0;
        if (isCacheEnabled && !recogCache.isEmpty())
        {       // Extract Keypoints
            KeypointDescList kplist = recognizer.extractor.extract(image);
            logger.debug("Extracted KPs from Image:" + kplist.points.size());
            res = recogCache.get(kplist);
            ObjRecServiceProto.Features.Builder kplist_ser = Utils.serialize(kplist);
            features.setDescs(kplist_ser.getDescs())
                    .addAllKeypoints(kplist_ser.getKeypointsList());
            dur = System.currentTimeMillis() - start;
        }
        computeTime.addValue(dur);
        postRecognition(dur, 0l, res, features.build(), cb, imagePath);
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
        KeypointDescList kplist = recognizer.extractor.extract(request.getImage().toByteArray());
        logger.debug("Extracted KPs from Image request:" + kplist.points.size());

        String res = recogCache.invalid();
        if(isCacheEnabled)
            // Recognize from local cache
            res = recogCache.get(kplist);
        long dur = System.currentTimeMillis() - start;
        ObjRecServiceProto.Features features = Utils.serialize(kplist)
                .setReqId(request.getReqId())
                .build();
        computeTime.addValue(dur);
        postRecognition(dur, start - req_recv_ts, res, features, cb, null);
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
        computeTime.addValue(dur);
        postRecognition(dur, start - req_recv_ts, res, features, client_cb, null);
    }

    /**
     * Check if hit, if not send to server
     * @param lookup_latency
     * @param res
     * @param features
     * @param client_cb
     * @param imagePath
     */
    protected void postRecognition(long lookup_latency,
                                   long time_in_queue,
                                   String res,
                                   ObjRecServiceProto.Features features,
                                   ObjRecCallback client_cb,
                                   String imagePath)
    {
        // Calculate comp latency
        ObjRecServiceProto.Latency.Builder complatency = ObjRecServiceProto.Latency.newBuilder().
                setInQueue((int) time_in_queue).
                setName(name).
                setSize(recogCache.getSize());
        if(lookup_latency > 0)
            complatency.setComputation((int) lookup_latency);
        // Check if Hit
        if (recogCache.isValid(res))
            onHit(features, res, complatency, client_cb);
        else
            onMiss(features, complatency, client_cb, imagePath);
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
     * @param complatency
     * @param client_cb
     * @param imagePath
     */
    protected void onMiss(ObjRecServiceProto.Features features,
                          ObjRecServiceProto.Latency.Builder complatency,
                          ObjRecCallback client_cb,
                          String imagePath)
    {
        logger.debug("Miss");
        ObjRecServiceStub.recognizeFeatures(rpc, features, new CachedObjRecCallback(client_cb, complatency));
    }

    /**
     * Calls server with image for recognition
     * @param imageRequest
     * @param complatency
     * @param client_cb
     * @param imagePath
     */
    protected void onMiss(ObjRecServiceProto.Image imageRequest,
                          ObjRecServiceProto.Latency.Builder complatency,
                          ObjRecCallback client_cb,
                          String imagePath)
    {
        logger.debug("Miss");
        ObjRecServiceStub.recognize(rpc, imageRequest, new CachedObjRecCallback(client_cb, complatency));
    }


    /**
     * Add items to cache
     * @param items
     */
    public void initializeCache(Collection<String> items)
    {
        for (String annotationstring :
                items)
        {
            if(!recogCache.contains(annotationstring))
            {
                if(!feature_request_lookback.contains(annotationstring))
                {
                    feature_request_lookback.add(annotationstring);
                    logger.debug("Added to lookback:" + annotationstring);
                    logger.debug("Requesting for init item features:" + annotationstring);
                    // If not, fetch from server
                    ObjRecServiceProto.Annotation.Builder features_req = ObjRecServiceProto.Annotation.newBuilder()
                            .setAnnotation(annotationstring)
                            .setReqId(ObjRecServiceProto.RequestID.newBuilder()
                                              .setName(client_name)
                                              .setReqId(req_counter.incrementAndGet()));
                    if (num_cache_features != null)
                        features_req.setNumFeatures(num_cache_features);

                    ObjRecServiceStub.getFeatures(rpc,
                                                  features_req.build(),
                                                  new FeaturesRecvCallback(annotationstring));
                    try
                    {
                        Thread.sleep(10);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
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

    public Collection<String> getKnownItems()
    {
        return recogCache.getAllKeys();
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
            logger.debug("Received results from next:" + annotation.getAnnotation());
            int latency = (int) (System.currentTimeMillis() - start);
            // Add latencies to Annotation
            ObjRecServiceProto.Annotation.Builder ann = ObjRecServiceProto.Annotation.newBuilder(annotation);
            ann.addLatencies(complatency.setNextLevel(latency));
            // Run the client's callback
            app_cb.run(ann.build());
            logger.debug("Ran Client callback");

            if (isCacheEnabled)
            {
                // Update cache about latency
                recogCache.updateMissLatency(latency);
                String annotationstring = annotation.getAnnotation();
                // Check if you have this image in your knownItems
                if (recogCache.isValid(annotationstring))
                    handle_missed_features(annotation);
            }
        }

        protected void handle_missed_features(ObjRecServiceProto.Annotation annotation)
        {
            String annotationstring = annotation.getAnnotation();
            if (!recogCache.contains(annotationstring))
                {
                    // Check if lookback queue contains these features
                    if(!feature_request_lookback.contains(annotationstring))
                    {
                        feature_request_lookback.add(annotationstring);
                        logger.debug("Added to lookback:" + annotationstring);
                        logger.debug("Requesting for missed item features:" + annotationstring);
                        // If not, fetch from server
                        ObjRecServiceProto.Annotation.Builder features_req = ObjRecServiceProto.Annotation.newBuilder(
                                annotation)
                                .setReqId(ObjRecServiceProto.RequestID.newBuilder()
                                                  .setName(client_name)
                                                  .setReqId(req_counter.incrementAndGet()));
                        if (num_cache_features != null)
                            features_req.setNumFeatures(num_cache_features);

                        ObjRecServiceStub.getFeatures(rpc,
                                                      features_req.build(),
                                                      new FeaturesRecvCallback(annotationstring));
                    }
                    else
                    {
                        // Already requested this and have not gotten a reply yet, so drop this request
                        logger.debug("Already requested - present in lookback queue: " + annotationstring);
                    }
                } else
                {
                    recogCache.put(annotationstring, null);
                    logger.debug("Present in Cache but not matched");
                }
        }
    }

    /**
     * Callback that is called when features are received from server.
     * Inserts the features into the cache
     */
    protected class FeaturesRecvCallback implements RpcCallback<ObjRecServiceProto.Features>
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
            logger.debug("Received **Features** from Server:" + annotation);
            logger.debug("Removed from lookback:" + annotation);
            feature_request_lookback.remove(annotation);
            if(!features.hasDescs())
            {
                logger.debug("Features empty");
                return;
            }

            KeypointDescList kdlist = Utils.deserialize(features);
            recogCache.put(annotation, kdlist);
            long compdur = System.currentTimeMillis() - compstart;
            long netdur = compstart - start;
        }
    }
}
