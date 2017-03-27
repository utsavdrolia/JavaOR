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
import java.util.concurrent.atomic.AtomicLong;
/**
 * Created by utsav on 6/14/16.
 */
public class ObjRecServer extends ObjRecServiceProto.ObjRecService
{
    private Recognizer recognizer;
    private RPCServer rpc;
    private final String NAME = Names.Cloud;

    private final AtomicLong recv_counter = new AtomicLong(0L);
    private final AtomicLong send_counter = new AtomicLong(0L);
    private final AtomicLong proc_counter = new AtomicLong(0L);
    private final static Logger logger = LoggerFactory.getLogger(ObjRecServer.class);

    public ObjRecServer(FeatureExtractor dbextractor, FeatureExtractor extractor, Matcher matcher, String dblistpath, String myaddress) throws IOException
    {
        recognizer = new Recognizer(dbextractor, extractor, matcher, dblistpath);
        rpc = new RPCServer(myaddress, this, 8);
    }

    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        logger.debug("ObjRecServer: Received Request: Client="  + request.getReqId().getName()
                                                                + " ReqID=" + request.getReqId().getReqId()
                                                                + " RecvCounter=" + recv_counter.incrementAndGet());
        int hash = request.hashCode();
        logger.debug("ObjRecServer: Received Hash:" + hash);
        Long req_rx = rpc.getRequestRxTime(hash);
        byte[] img = request.getImage().toByteArray();
        Long start = System.currentTimeMillis();
        String ret = recognizer.recognize(img);
        long stop = System.currentTimeMillis() - start;
        logger.debug("In Queue " + Long.toString(start - req_rx));
        logger.debug("Processed in " + Long.toString(stop));

        logger.debug("Processed Request: Client=" + request.getReqId().getName() + " ReqID=" + request.getReqId().getReqId());

        done.run(ObjRecServiceProto.Annotation.newBuilder()
                         .setAnnotation(ret)
                         .setReqId(request.getReqId())
                         .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                               .setName(NAME)
                                               .setComputation((int) (stop))
                                               .setInQueue((int) (start - req_rx)))
                         .build());
        logger.debug("Replied to Request: Client="    + request.getReqId().getName()
                                                                    + " ReqID=" + request.getReqId().getReqId()
                                                                    + " SendCounter=" + send_counter.incrementAndGet()
                                                                    + " Annotation=" + ret);

    }

    @Override
    public void recognizeFeatures(RpcController controller, ObjRecServiceProto.Features request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        logger.debug("Received Request: Client="  + request.getReqId().getName()
                             + " ReqID=" + request.getReqId().getReqId()
                             + " RecvCounter=" + recv_counter.incrementAndGet());
        int hash = request.hashCode();
        logger.debug("Received Hash:" + hash);
        Long req_rx = rpc.getRequestRxTime(hash);
        Long start = System.currentTimeMillis();
//        try
//        {
//            sleep(200);
//        } catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }

        KeypointDescList kplist = Utils.deserialize(request);
        logger.debug("Received Request List size:" + kplist.points.size());
        String ret = recognizer.recognize(kplist);
        long stop = System.currentTimeMillis() - start;
        logger.debug("In Queue " + Long.toString(start - req_rx));
        logger.debug("Processed in " + Long.toString(stop));

        logger.debug("Processed Request: Client=" +  request.getReqId().getName() + " ReqID=" + request.getReqId().getReqId());

        done.run(ObjRecServiceProto.Annotation.newBuilder()
                         .setAnnotation(ret)
                         .setReqId(request.getReqId())
                         .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                      .setName(NAME)
                                      .setComputation((int) (stop))
                                      .setInQueue((int) (start - req_rx)))
                .build());
        logger.debug("Replied to Request: Client="    + request.getReqId().getName()
                             + " ReqID=" + request.getReqId().getReqId()
                             + " SendCounter=" + send_counter.incrementAndGet()
                             + " Annotation=" + ret);
    }

    @Override
    public void getImage(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Image> done)
    {

    }

    @Override
    public void getFeatures(RpcController controller, ObjRecServiceProto.Annotation request, RpcCallback<ObjRecServiceProto.Features> done)
    {
        logger.debug("getFeatures Request RECV for:" + request.getAnnotation());
        Long req_rx = rpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
        KeypointDescList kp = recognizer.matcher.get(request.getAnnotation());
        logger.debug("Fetched KPList for:" + request.getAnnotation() + " Size:" + kp.points.size());
        ObjRecServiceProto.Features.Builder features = Utils.serialize(kp);
        // Return
        done.run(features
                .addLatencies(ObjRecServiceProto.Latency.newBuilder()
                                      .setName(NAME)
                                      .setComputation((int) (System.currentTimeMillis() - start))
                                      .setInQueue((int) (start - req_rx)))
                .setReqId(request.getReqId())
                .build());
        logger.debug("getFeatures Request SENT");
    }

    @Override
    public void getNextPDF(RpcController controller,
                           ObjRecServiceProto.Annotation request,
                           RpcCallback<ObjRecServiceProto.Annotation> done)
    {

    }
}
