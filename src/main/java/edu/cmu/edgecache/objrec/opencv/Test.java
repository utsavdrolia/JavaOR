package edu.cmu.edgecache.objrec.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by utsav on 2/6/16.
 */
public class Test
{
        public static void main(String[] args){
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
            System.out.println("mat = " + mat.dump());
        }
}
