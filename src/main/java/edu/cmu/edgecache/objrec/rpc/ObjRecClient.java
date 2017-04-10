package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.ByteString;
import org.crowd.rpc.RPCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by utsav on 6/14/16.
 */
public class ObjRecClient
{
    protected ObjRecServiceProto.ObjRecService.Stub ObjRecServiceStub;
    protected ByteBuffer buffer = ByteBuffer.allocate(5 * 1000 * 1024);
    protected RPCClient rpc;
    protected String client_name = "ObjRecClient_" + Long.toString(new Random().nextLong());
    protected final AtomicInteger req_counter = new AtomicInteger();

    private final static Logger logger = LoggerFactory.getLogger(ObjRecClient.class);

    public ObjRecClient(String server)
    {
        if(server != null)
        {
            rpc = new RPCClient(server);
            ObjRecServiceStub = ObjRecServiceProto.ObjRecService.Stub.newStub(rpc);
        }
    }

    public void recognize(String imagePath, ObjRecCallback cb, long startTime) throws IOException
    {
        buffer.clear();
        FileInputStream fis = new FileInputStream(imagePath);
        FileChannel fc = fis.getChannel();
        fc.read(buffer);
        buffer.flip();
        fc.close();
        recognize(buffer, new ObjRecClientCallback(cb, startTime));
    }

    public final void recognize(ByteBuffer image, ObjRecCallback cb) throws IOException
    {
        logger.debug("ImageRequestSize:" + image.position());
        ObjRecServiceProto.Image.Builder request_builder = ObjRecServiceProto.Image.newBuilder()
                .setImage(ByteString.copyFrom(image))
                .setReqId(ObjRecServiceProto.RequestID.newBuilder()
                                  .setName(this.client_name)
                                  .setReqId(req_counter.incrementAndGet()));
        ObjRecServiceStub.recognize(rpc,
                                    request_builder.build(),
                                    cb);
    }

    private class ObjRecClientCallback extends ObjRecCallback
    {
        private ObjRecCallback cb;
        private long start;

        ObjRecClientCallback(ObjRecCallback cb, long startTime)
        {
            this.cb = cb;
            this.start = startTime;
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            int latency = (int) (System.currentTimeMillis() - start);
            // Add latencies to Annotation
            ObjRecServiceProto.Latency.Builder next_latency = ObjRecServiceProto.Latency.newBuilder().
                    setNextLevel(latency).
                    setName(Names.Device);
            ObjRecServiceProto.Annotation.Builder ann = ObjRecServiceProto.Annotation.newBuilder(annotation);
            ann.addLatencies(next_latency);
            // Run the client's callback
            cb.run(ann.build());
        }
    }
}
