package org.crowdcache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.crowd.rpc.RPCServer;
import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;

import java.io.IOException;

/**
 * Created by utsav on 6/20/16.
 */
public class ObjRecCloudlet extends ObjRecServiceProto.ObjRecService
{
    private RPCServer listeningrpc;
    private CachedObjRecClient objRecClient;


    public ObjRecCloudlet(FeatureExtractor extractor, Matcher matcher, String myaddress, String serveraddress) throws IOException
    {
        listeningrpc = new RPCServer(myaddress, this);
        objRecClient = new CachedObjRecClient(extractor, matcher, serveraddress, "Cloudlet");
    }


    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        objRecClient.recognize(request.getImage().toByteArray(), new CloudletObjRecCallback(done));
    }

    @Override
    public void recognizeFeatures(RpcController controller, ObjRecServiceProto.Features request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        // Check in local cache if we have image and if not send request to cloud
        objRecClient.recognize(request, new CloudletObjRecCallback(done));
    }

    @Override
    public void getImage(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Image> done)
    {

    }

    /**
     * Get the features for the given annotation from the local cache
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void getFeatures(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Features> done)
    {
        KeypointDescList kp = objRecClient.recognizer.matcher.get(request.getAnnotation());
        ObjRecServiceProto.Features features = Utils.serialize(kp);
        // Return
        done.run(features);
    }

    private class CloudletObjRecCallback extends ObjRecCallback
    {
        RpcCallback<ObjRecServiceProto.Annotation> done;

        public CloudletObjRecCallback(RpcCallback<ObjRecServiceProto.Annotation> done)
        {
            this.done = done;
        }

        @Override
        public void run(String annotation)
        {
            done.run(ObjRecServiceProto.Annotation.newBuilder().setAnnotation(annotation).build());
        }
    }
}
