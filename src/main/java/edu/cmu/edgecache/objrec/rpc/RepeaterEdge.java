package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.crowd.rpc.RPCClient;
import org.crowd.rpc.RPCServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepeaterEdge simply forwards requests and replies, but helps in measuring latencies in the different parts of the network
 * Created by utsav on 6/20/16.
 */
public class RepeaterEdge extends ObjRecServiceProto.ObjRecService
{
    private final RPCClient rpc;
    private final Stub ObjRecServiceStub;
    private RPCServer listeningrpc;
    private final String EDGE = Names.Edge;
    final static Logger logger = LoggerFactory.getLogger(RepeaterEdge.class);


    /**
     * Creates Edge with defaul cache (LFU)
     * @param myaddress
     * @param serveraddress
     */
    public RepeaterEdge(String myaddress, String serveraddress)
    {
        listeningrpc = new RPCServer(myaddress, this);
        rpc = new RPCClient(serveraddress);
        ObjRecServiceStub = ObjRecServiceProto.ObjRecService.Stub.newStub(rpc);
    }

    @Override
    public void recognize(RpcController controller, ObjRecServiceProto.Image request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
        this.ObjRecServiceStub.recognize(rpc, request, new RepeaterCallback(done, start - req_rx, start));
    }

    @Override
    public void recognizeFeatures(RpcController controller, ObjRecServiceProto.Features request, RpcCallback<ObjRecServiceProto.Annotation> done)
    {
        Long req_rx = listeningrpc.getRequestRxTime(request.hashCode());
        Long start = System.currentTimeMillis();
        this.ObjRecServiceStub.recognizeFeatures(rpc, request, new RepeaterCallback(done, start - req_rx, start));
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

    }

    @Override
    public void getNextPDF(RpcController controller,
                           ObjRecServiceProto.Annotation request,
                           RpcCallback<ObjRecServiceProto.PDF> done)
    {
    }

    private class RepeaterCallback extends ObjRecCallback
    {
        RpcCallback<ObjRecServiceProto.Annotation> done;
        long q_time;
        long start_time;
        public RepeaterCallback(RpcCallback<ObjRecServiceProto.Annotation> done, long in_q, long start)
        {
            this.done = done;
            this.q_time = in_q;
            this.start_time = start;
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            logger.debug("Received Reply from Cloud");
            int latency = (int) (System.currentTimeMillis() - this.start_time);
            ObjRecServiceProto.Latency.Builder edge_latencies = ObjRecServiceProto.Latency.newBuilder().
                    setInQueue((int) this.q_time).
                    setNextLevel(latency).
                    setName(Names.Edge);
            ObjRecServiceProto.Annotation.Builder edge_annotation = ObjRecServiceProto.Annotation.newBuilder(annotation);
            edge_annotation.addLatencies(edge_latencies);
            done.run(edge_annotation.build());
            logger.debug("Sent message back to device:" + edge_annotation.build().toString());
        }
    }
}
