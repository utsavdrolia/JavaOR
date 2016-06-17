import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.extractors.SIFTFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM_NB;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2_NB;
import org.crowdcache.objrec.opencv.matchers.LSHMatcher_HAM;
import org.opencv.core.Core;

import java.io.*;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateOpenCV
{
    private static final int BIN_NN = 1;
    private static final int BIN_NB = 2;
    private static final int FLOAT_NB = 3;
    private static final int LSH = 4;

    private static final int ORB = 1;
    private static final int SIFT = 2;

    public static void main(String args[]) throws IOException, InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length >= 7)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];
            String pars = args[3];
            String pars_db = args[4];
            Integer matchertype = Integer.valueOf(args[5]);
            Integer featuretype = Integer.valueOf(args[6]);
            String lshpars = "";
            if(args.length == 8)
                lshpars = args[7];

            FeatureExtractor extractor;
            FeatureExtractor dbextractor;
            Matcher matcher;

            switch (featuretype)
            {
                case ORB:
                    extractor = new ORB(pars);
                    dbextractor = new ORB(pars_db);
                    System.out.println("Using ORB");
                    break;
                case SIFT:
                    extractor = new SIFTFeatureExtractor(pars);
                    dbextractor = new SIFTFeatureExtractor(pars_db);
                    System.out.println("Using SIFT");
                    break;
                default:
                    extractor = new ORB(pars);
                    dbextractor = new ORB(pars_db);
                    break;
            }
            switch (matchertype)
            {
                case BIN_NN:
                    matcher = new BFMatcher_HAM_NB();
                    System.out.println("Using NN");
                    break;
                case BIN_NB:
                    matcher = new BFMatcher_HAM_NB();
                    System.out.println("Using NB");
                    break;
                case FLOAT_NB:
                    matcher = new BFMatcher_L2_NB();
                    System.out.println("Using NB");
                    break;
                case LSH:
                    matcher = new LSHMatcher_HAM(lshpars, -1);
                    System.out.println("Using LSH");
                    break;
                default:
                    matcher = new BFMatcher_HAM_NB();
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
                System.out.println(img + "," + result + "," + Long.toString(end - start));
                line = dir.readLine();
                count++;
               //System.out.println(count);
            }while ((line != null));
            Long procend = System.currentTimeMillis() - procstart;
            //System.out.println("Time:" + procend + " Count:" + count);
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
