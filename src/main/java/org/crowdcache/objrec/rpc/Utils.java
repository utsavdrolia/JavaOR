package org.crowdcache.objrec.rpc;

import com.google.protobuf.ByteString;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 6/17/16.
 */
public class Utils
{
    /**
     * Convert {@link KeypointDescList} to protobuf's {@link org.crowdcache.objrec.rpc.ObjRecServiceProto.Features}
     * @param kp {@link KeypointDescList}
     * @return {@link org.crowdcache.objrec.rpc.ObjRecServiceProto.Features}
     */
    public static ObjRecServiceProto.Features serialize(KeypointDescList kp)
    {
        Mat descs = kp.descriptions;
        ArrayList<Point> points = kp.points;

        // Serialize mat of descriptors
        byte[] matdata = new byte[(int) (descs.total()*descs.elemSize())];
        descs.get(0, 0, matdata);
        ObjRecServiceProto.Features.Builder reply = ObjRecServiceProto.Features.newBuilder();
        reply.setDescs(ObjRecServiceProto.DescMat.newBuilder()
                .setRows(descs.rows())
                .setCols(descs.cols())
                .setType(descs.type())
                .setData(ByteString.copyFrom(matdata)));
        // Serialize keypoints
        for(Point p: points)
        {
            reply.addKeypoints(ObjRecServiceProto.KeyPoint.newBuilder().
                    setX(p.x).
                    setY(p.y).build());
        }
        ObjRecServiceProto.Features features = reply.build();
        // Return
        return features;
    }

    /**
     * Convert protobuf's {@link org.crowdcache.objrec.rpc.ObjRecServiceProto.Features} to {@link KeypointDescList}
     * @param features {@link KeypointDescList}
     * @return {@link KeypointDescList}
     */
    public static KeypointDescList deserialize(ObjRecServiceProto.Features features)
    {
        ObjRecServiceProto.DescMat descMat = features.getDescs();
        Mat desc = new Mat(descMat.getRows(), descMat.getCols(), descMat.getType());
        desc.put(0, 0, descMat.getData().toByteArray());
        List<ObjRecServiceProto.KeyPoint> keypointsList = features.getKeypointsList();
        ArrayList<Point> kplist = new ArrayList<>(keypointsList.size());
        for (ObjRecServiceProto.KeyPoint kp: keypointsList)
        {
            kplist.add(new Point(kp.getX(), kp.getY()));
        }
        return new KeypointDescList(kplist, desc);
    }

}
