package edu.cmu.edgecache.objrec.rpc;

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
    protected ObjRecServiceProto.ObjRecService.Stub ObjRecServiceStub;
    protected ByteBuffer buffer = ByteBuffer.allocate(80 * 1024);
    protected RPCClient rpc;
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
        recognize(buffer, cb);
    }

    public final void recognize(ByteBuffer image, ObjRecCallback cb) throws IOException
    {
        ObjRecServiceStub.recognize(rpc,
                ObjRecServiceProto.Image.newBuilder().setImage(ByteString.copyFrom(image)).build(),
                cb);
    }
}