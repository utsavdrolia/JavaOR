package edu.cmu.edgecache.objrec.rpc;

import com.google.protobuf.ByteString;
import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by utsav on 6/17/16.
 */
public class Utils
{
    /**
     * Convert {@link KeypointDescList} to protobuf's {@link ObjRecServiceProto.Features}
     * @param kp {@link KeypointDescList}
     * @return {@link ObjRecServiceProto.Features}
     */
    public static ObjRecServiceProto.Features.Builder serialize(KeypointDescList kp)
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
        // Return
        return reply;
    }

    /**
     * Convert protobuf's {@link ObjRecServiceProto.Features} to {@link KeypointDescList}
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

    public static ObjRecServiceProto.ObjectProbability.Builder serialize(String object, Double prob)
    {
        ObjRecServiceProto.ObjectProbability.Builder builder = ObjRecServiceProto.ObjectProbability.newBuilder();
        builder.setName(object).setProbability(prob);
        return builder;
    }

    public static List<ObjRecServiceProto.ObjectProbability> serialize(Map<String, Double> pdf)
    {
        List<ObjRecServiceProto.ObjectProbability> probabilities = new ArrayList<>();
        for (Map.Entry<String, Double> entry :
                pdf.entrySet())
        {
            probabilities.add(serialize(entry.getKey(), entry.getValue()).build());
        }
        return probabilities;
    }


    public static Map<String, Double> deserialize(List<ObjRecServiceProto.ObjectProbability> pdfList)
    {
        Map<String, Double> pdf = new HashMap<>();
        for (ObjRecServiceProto.ObjectProbability probability:
             pdfList)
        {
            pdf.put(probability.getName(), probability.getProbability());
        }
        return pdf;
    }
}
