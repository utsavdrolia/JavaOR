import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;
import org.crowdcache.objrec.opencv.extractors.BRISK;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM2;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM_NB;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.*;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateOpenCV
{
    private static final int NN = 1;
    private static final int NB = 2;
    public static void main(String args[]) throws IOException, InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 6)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];
            String orb_pars = args[3];
            String orb_pars_db = args[4];
            Integer matchertype = Integer.valueOf(args[5]);

            FeatureExtractor extractor = new ORB(orb_pars);
            FeatureExtractor dbextractor = new ORB(orb_pars_db);
            Matcher matcher;

            switch (matchertype)
            {
                case NN:
                    matcher = new BFMatcher_HAM();
                    break;
                case NB:
                    matcher = new BFMatcher_HAM_NB();
                    break;
                default:
                    matcher = new BFMatcher_HAM();
                    break;
            }

            Recognizer recognizer = new Recognizer(dbextractor, extractor, matcher, DBdirpath);

            BufferedReader dir = new BufferedReader(new FileReader(queryList));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

            Integer count = 0;
            String line = dir.readLine();
            Long procstart = System.currentTimeMillis();
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];

                Long start = System.currentTimeMillis();
                String result = recognizer.recognize(imgpath);
                Long end = System.currentTimeMillis();
                if(result == null)
                    result = "None";
                resultsfile.write(img + "," + result + "," + Long.toString(end - start) + "\n");
                System.out.println("Input:" + imgpath + " Matched:" + result);
                System.out.println("Time:" + (end - start));
                line = dir.readLine();
                count++;
                System.out.println(count);
            }while ((line != null));
            Long procend = System.currentTimeMillis() - procstart;
            System.out.println("Time:" + procend + " Count:" + count);
            resultsfile.flush();
            resultsfile.close();
        }
        else
        {
            System.out.println("NOT ENOUGH ARGS");
        }
        System.exit(0);
    }
}
