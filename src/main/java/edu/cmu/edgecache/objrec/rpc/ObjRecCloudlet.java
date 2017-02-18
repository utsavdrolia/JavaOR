package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.recog.CacheType;
import org.crowd.rpc.RPCServer;
import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.KeypointDescList;

import java.io.IOException;

/**
 * Combines a {@link ObjRecServer} and {@link CachedObjRecClient} to create a cached proxy
 * Created by utsav on 6/20/16.
 */
public class ObjRecCloudlet extends ObjRecServiceProto.ObjRecService
{
    private RPCServer listeningrpc;
    private CachedObjRecClient objRecClient;
    private final String EDGE = Names.Edge;


    public ObjRecCloudlet(FeatureExtractor extractor, Matcher matcher, String myaddress, String serveraddress, int cachesize, CacheType cachetype) throws IOException
    {
        listeningrpc = new RPCServer(myaddress, this);
        objRecClient = new CachedObjRecClient(extractor, matcher, serveraddress, EDGE, cachesize);
    }


    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        objRecClient.recognize(request.getImage().toByteArray(), req_rx, new CloudletObjRecCallback(done));
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
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
        KeypointDescList kp = objRecClient.getFeatures(request.getAnnotation());
        ObjRecServiceProto.Features.Builder features = Utils.serialize(kp);
        // Return
        done.run(features
                .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                      .setName(EDGE)
                                      .setComputation((int) (System.currentTimeMillis() - start))
                                      .setInQueue((int) (start - req_rx)))
                .build());
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
            done.run(annotation);
        }
    }
}
