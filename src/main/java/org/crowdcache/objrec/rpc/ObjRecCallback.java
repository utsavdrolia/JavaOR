package org.crowdcache.objrec.rpc;

public abstract class ObjRecCallback implements com.google.protobuf.RpcCallback<ObjRecServiceProto.Annotation>
{
    @Override
    public void run(ObjRecServiceProto.Annotation annotation)
    {
        run(annotation.getAnnotation());
    }

    public abstract void run(String annotation);
}
