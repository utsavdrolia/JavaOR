package org.crowdcache.objrec.rpc;

import com.google.protobuf.ByteString;
import org.crowd.rpc.RPCClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by utsav on 6/14/16.
 */
public class ObjRecClient
{
    private ObjRecServiceProto.ObjRecService.Stub ObjRecServiceStub;
    ByteBuffer buffer = ByteBuffer.allocate(80 * 1024);
    RPCClient rpc;
    public ObjRecClient(String server)
    {
        rpc = new RPCClient(server);
        ObjRecServiceStub = ObjRecServiceProto.ObjRecService.Stub.newStub(rpc);
    }

    public void recognize(String imagePath, ObjRecCallback cb) throws IOException
    {
        buffer.clear();
        FileInputStream fis = new FileInputStream(imagePath);
        FileChannel fc = fis.getChannel();
        fc.read(buffer);
        buffer.flip();
        fc.close();
        cb.setStarttime(System.currentTimeMillis());
        ObjRecServiceStub.recognize(rpc,
                ObjRecServiceProto.Image.newBuilder().setImage(ByteString.copyFrom(buffer)).build(),
                cb);
    }

    public abstract class ObjRecCallback implements com.google.protobuf.RpcCallback<ObjRecServiceProto.Annotation>
    {
        private long starttime;

        void setStarttime(long starttime)
        {
            this.starttime = starttime;
        }
    }
}
