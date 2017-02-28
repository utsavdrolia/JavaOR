package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.Recognizer;
import org.crowd.rpc.RPCServer;

import java.io.IOException;

/**
 * Created by utsav on 6/14/16.
 */
public class ObjRecServer extends ObjRecServiceProto.ObjRecService
{
    private Recognizer recognizer;
    private RPCServer rpc;
    private final String NAME = Names.Cloud;
    public ObjRecServer(FeatureExtractor dbextractor, FeatureExtractor extractor, Matcher matcher, String dblistpath, String myaddress) throws IOException
    {
        recognizer = new Recognizer(dbextractor, extractor, matcher, dblistpath);
        rpc = new RPCServer(myaddress, this, 8);
    }

    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = rpc.getRequestRxTime(request.hashCode());
        byte[] img = request.getImage().toByteArray();
        Long start = System.currentTimeMillis();
        String ret = recognizer.recognize(img);
        long stop = System.currentTimeMillis() - start;
        done.run(ObjRecServiceProto.Annotation.newBuilder()
                .setAnnotation(ret)
                .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                  .setName(NAME)
                                  .setComputation((int) (stop))
                                  .setInQueue((int) (start - req_rx)))
                .build());
    }

    @Override
    public void recognizeFeatures(RpcController controller, ObjRecServiceProto.Features request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = rpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
//        try
//        {
//            sleep(200);
//        } catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
        KeypointDescList kplist = Utils.deserialize(request);
        String ret = recognizer.recognize(kplist);
        done.run(ObjRecServiceProto.Annotation.newBuilder()
                .setAnnotation(ret)
                .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                      .setName(NAME)
                                      .setComputation((int) (System.currentTimeMillis() - start))
                                      .setInQueue((int) (start - req_rx)))
                .build());
    }

    @Override
    public void getImage(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Image> done)
    {

    }

    @Override
    public void getFeatures(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Features> done)
    {
        Long req_rx = rpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
        KeypointDescList kp = recognizer.matcher.get(request.getAnnotation());
        ObjRecServiceProto.Features.Builder features = Utils.serialize(kp);
        // Return
        done.run(features
                .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                      .setName(NAME)
                                      .setComputation((int) (System.currentTimeMillis() - start))
                                      .setInQueue((int) (start - req_rx)))
                .build());
    }
}
