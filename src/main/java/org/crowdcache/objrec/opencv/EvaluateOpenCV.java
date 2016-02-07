package org.crowdcache.objrec.opencv;

import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2;
import org.opencv.core.Core;
import org.opencv.highgui.Highgui;

import java.io.*;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateOpenCV
{
    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 3)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];

            SURFFeatureExtractor surfExtractor = new SURFFeatureExtractor();
            BFMatcher_L2 surfBFAssociator = new BFMatcher_L2();
            Recognizer recognizer = new Recognizer(surfExtractor, surfBFAssociator, DBdirpath);

            BufferedReader dir = new BufferedReader(new FileReader(queryList));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

            Integer count = 0;
            String line = dir.readLine();
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];

                Long start = System.currentTimeMillis();
                String result = recognizer.recognize(Highgui.imread(imgpath, Highgui.CV_LOAD_IMAGE_GRAYSCALE));
                Long end = System.currentTimeMillis();
                if(result == null)
                    result = "None";
                resultsfile.write(img + "," + result + "," + Long.toString(end - start) + "\n");
                System.out.println("Input:" + imgpath + " Matched:" + result);
                System.out.println("Time:" + (System.currentTimeMillis() - start));
                line = dir.readLine();
                count++;
            }while ((line != null) && (count < 5));
            resultsfile.flush();
            resultsfile.close();
        }
        System.exit(0);
    }
}
