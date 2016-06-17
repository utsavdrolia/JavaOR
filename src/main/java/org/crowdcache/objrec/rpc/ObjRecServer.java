package org.crowdcache.objrec.rpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.crowd.rpc.RPCServer;
import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by utsav on 6/14/16.
 */
public class ObjRecServer extends ObjRecServiceProto.ObjRecService
{
    private Recognizer recognizer;
    private RPCServer rpc;

    public ObjRecServer(FeatureExtractor dbextractor, FeatureExtractor extractor, Matcher matcher, String dblistpath, String myaddress) throws IOException
    {
        recognizer = new Recognizer(dbextractor, extractor, matcher, dblistpath);
        rpc = new RPCServer(myaddress, this);
    }

    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        //TODO: Parallelize - push request into a queue and have a set of workers process the queue
        byte[] img = request.getImage().toByteArray();
        String ret = recognizer.recognize(img);
        done.run(ObjRecServiceProto.Annotation.newBuilder().setAnnotation(ret).build());
    }

    @Override
    public void recognizeFeatures(RpcController controller, ObjRecServiceProto.Features request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        KeypointDescList kplist = Utils.deserialize(request);
        String ret = recognizer.recognize(kplist);
        done.run(ObjRecServiceProto.Annotation.newBuilder().setAnnotation(ret).build());
    }

    @Override
    public void getImage(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Image> done)
    {

    }

    @Override
    public void getFeatures(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Features> done)
    {
        KeypointDescList kp = recognizer.matcher.get(request.getAnnotation());
        ObjRecServiceProto.Features features = Utils.serialize(kp);
        // Return
        done.run(features);
    }
}
