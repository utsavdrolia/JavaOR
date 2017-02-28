import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.Recognizer;
import edu.cmu.edgecache.objrec.opencv.Util;
import org.opencv.core.Core;

import java.io.*;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateOpenCV
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
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
            String matcher_pars = args[7];

            FeatureExtractor extractor = Util.createExtractor(featuretype, pars_db);
            FeatureExtractor dbextractor = Util.createExtractor(featuretype, pars_db);
            Matcher matcher = Util.createMatcher(matchertype, matcher_pars, 3, 0.5);

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
