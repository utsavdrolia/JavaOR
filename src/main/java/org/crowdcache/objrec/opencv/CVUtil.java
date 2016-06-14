package org.crowdcache.objrec.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.resize;

/**
 * Created by utsav on 6/14/16.
 */
public class CVUtil
{
    public static void resize(Mat image, Mat dst)
    {
        if(image.rows() > image.cols())
            Imgproc.resize(image, dst, new Size(480, 640), 0, 0, Imgproc.INTER_LINEAR);
        else if(image.rows() < image.cols())
            Imgproc.resize(image, dst, new Size(640, 480), 0, 0, Imgproc.INTER_LINEAR);
        else
            Imgproc.resize(image, dst, new Size(500, 500), 0, 0, Imgproc.INTER_LINEAR);
    }
}
