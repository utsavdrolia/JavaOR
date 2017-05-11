package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.Recognizer;
import org.crowd.rpc.RPCServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Combines a {@link RPCServer} and {@link CachedObjRecClient} to create a cached proxy
 * Created by utsav on 6/20/16.
 */
public class ObjRecEdge extends ObjRecServiceProto.ObjRecService
{
    private RPCServer listeningrpc;
    private CachedObjRecClient objRecClient;
    private final String EDGE = Names.Edge;
    private PredictionManager<String> predictionManager = null;
    private final static Logger logger = LoggerFactory.getLogger(ObjRecEdge.class);

    /**
     * Creates Edge with defaul cache (LFU)
     * @param extractor
     * @param matcher
     * @param myaddress
     * @param serveraddress
     * @param cachesize
     * @throws IOException
     */
    public ObjRecEdge(FeatureExtractor extractor, Matcher matcher, String myaddress, String serveraddress, int cachesize) throws IOException
    {
        listeningrpc = new RPCServer(myaddress, this);
        objRecClient = new CachedObjRecClient(extractor, matcher, serveraddress, EDGE, cachesize, null);
    }

    /**
     * Creates the Edge cache with given type of cache
     * @param cache
     * @param myaddress
     */
    public ObjRecEdge(CachedObjRecClient cache, String myaddress)
    {
        listeningrpc = new RPCServer(myaddress, this);
        objRecClient = cache;
    }

    /**
     * Create ObjRecEdge with a prediction Manager that helps in prefetching
     * @param cache
     * @param myaddress
     * @param prefetchService
     */
    public ObjRecEdge(CachedObjRecClient cache, String myaddress, PredictionManager<String> prefetchService)
    {
        predictionManager = prefetchService;
        listeningrpc = new RPCServer(myaddress, this);
        objRecClient = cache;
        // If this is a warmed predictor, fetch corresponding items
        if(predictionManager.isInitialized_predictor())
        {
            Collection<String> prepopulate_items = predictionManager.warmedItems();
            if(prepopulate_items != null)
            {
                objRecClient.initializeCache(prepopulate_items);
            }
        }
    }



    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        objRecClient.recognize(request, req_rx, new CloudletObjRecCallback(done));
    }

    @Override
    public void recognizeFeatures(RpcController controller, ObjRecServiceProto.Features request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        objRecClient.recognize(request, req_rx, new CloudletObjRecCallback(done));
    }

    @Override
    public void getImage(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Image> done)
    {

    }

    /**
     * Get the features for the given annotation from the local knownItems
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void getFeatures(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Features> done)
    {
        logger.debug("getFeatures Request RECV for:" + request.getAnnotation());
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
        KeypointDescList kp = objRecClient.getFeatures(request.getAnnotation());
        if(kp != null)
        {
            logger.debug("Fetched KPList for:" + request.getAnnotation() + " Size:" + kp.points.size());
            int num_features = kp.points.size();
            if(request.hasNumFeatures())
                num_features = request.getNumFeatures();
            // Choose requested number of features
            KeypointDescList sub_kp = new KeypointDescList(kp.points.subList(0, num_features), kp.descriptions.rowRange(0, num_features));
            logger.debug("Requested #featuers:" + num_features + "--> Shortened KPList to:" + sub_kp.points.size());
            ObjRecServiceProto.Features.Builder kplist_ser = Utils.serialize(kp);
            ObjRecServiceProto.Features.Builder features = ObjRecServiceProto.Features.newBuilder();
            // Return
            done.run(features
                             .setAnnotation(request.getAnnotation())
                             .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                                   .setName(EDGE)
                                                   .setComputation((int) (System.currentTimeMillis() - start))
                                                   .setInQueue((int) (start - req_rx)))
                             .setReqId(request.getReqId())
                             .setDescs(kplist_ser.getDescs())
                             .addAllKeypoints(kplist_ser.getKeypointsList())
                             .build());
        }
        else
        {
            done.run(ObjRecServiceProto.Features.newBuilder().setReqId(request.getReqId()).build());
        }
        logger.debug("getFeatures Request SENT");
    }


    @Override
    public void getNextPDF(RpcController controller,
                           ObjRecServiceProto.Annotation annotation,
                           RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        try
        {
            Map<String, Double> pdf = predictionManager.getNextPDF(annotation.getReqId().getName(),
                                                                   annotation.getAnnotation());
            pdf.keySet().retainAll(objRecClient.getKnownItems());
            done.run(ObjRecServiceProto.Annotation.newBuilder(annotation).setPdf(ObjRecServiceProto.PDF.newBuilder().addAllPdf(
                    Utils.serialize(pdf))).build());
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
    }

    private class CloudletObjRecCallback extends ObjRecCallback
    {
        RpcCallback<ObjRecServiceProto.Annotation> done;

        public CloudletObjRecCallback(RpcCallback<ObjRecServiceProto.Annotation> done)
        {
            this.done = done;
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            ObjRecServiceProto.Annotation.Builder builder = ObjRecServiceProto.Annotation.newBuilder(
                    annotation);
            if (predictionManager != null) // If this supports prefetching
            {
                if (Recognizer.isValid(annotation.getAnnotation()))
                {
                    logger.debug("Fetching nextPDF");

                    Map<String, Double> pdf = predictionManager.getNextPDF(annotation.getReqId().getName(),
                                                                           annotation.getAnnotation());
                    pdf.keySet().retainAll(objRecClient.getKnownItems());
                    logger.debug("Added PDF to Callback");
                    builder.setPdf(ObjRecServiceProto.PDF.newBuilder().addAllPdf(Utils.serialize(pdf)));
                }
            }
            logger.debug("Return Annotation");
            done.run(builder.build());
        }
    }
}
