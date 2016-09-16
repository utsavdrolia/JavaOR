import edu.cmu.edgecache.objrec.opencv.*;
import edu.cmu.edgecache.objrec.rpc.CachedObjRecClient;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.recog.AbstractRecogCache;
import edu.cmu.edgecache.recog.OptRecogCache;
import org.opencv.core.Core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateOPTCachedClient
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 10)
        {
            String queryList = args[0];
            String resultspath = args[1];
            String pars = args[2];
            Integer featuretype = Integer.valueOf(args[3]);
            Integer matchertype_cache = Integer.valueOf(args[4]);
            String matcherpars_cache = args[5];
            String f_k_coeffs_path = args[6];
            String recall_k_coeffs_path = args[7];
            String all_objects_path = args[8];
            String serverAdd = args[9];

            BufferedReader all_objects_file = new BufferedReader(new FileReader(all_objects_path));

            String line = all_objects_file.readLine();
            ArrayList<String> all_objects = new ArrayList<>();
            do
            {
                all_objects.add(line);
                line = all_objects_file.readLine();
            } while ((line != null));

            FeatureExtractor extractor = Util.createExtractor(featuretype, pars);
            Matcher clientmatcher = Util.createMatcher(matchertype_cache, matcherpars_cache, 6, 0.8);
            Recognizer recognizer = new Recognizer(extractor, clientmatcher);
            AbstractRecogCache<String, KeypointDescList> recogCache = new OptRecogCache<>(new ImageRecognizerInterface(recognizer),
                                                                                          getCoefs(f_k_coeffs_path),
                                                                                          getCoefs(recall_k_coeffs_path),
                                                                                          all_objects);
            CachedObjRecClient objRecClient = new CachedObjRecClient(recognizer, recogCache, serverAdd, Names.Edge, true);

            Util.evaluate(objRecClient, queryList, resultspath);
        } else
        {
            System.out.println("10 Args required, provided " + args.length);
        }
        System.exit(0);
    }

    private static double[] getCoefs(String path) throws IOException
    {
        BufferedReader dir = new BufferedReader(new FileReader(path));
        String line = dir.readLine();
        String[] chunks = line.split(",");
        double[] coeffs = new double[chunks.length];
        for (int i = 0; i < chunks.length; i++)
        {
            coeffs[i] = Double.parseDouble(chunks[i]);
        }
        return coeffs;
    }


}
