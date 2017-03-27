import edu.cmu.edgecache.objrec.opencv.*;
import edu.cmu.edgecache.objrec.rpc.CachedObjRecClient;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.recog.AbstractRecogCache;
import edu.cmu.edgecache.recog.LFURecogCache;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateCachedClient
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 8)
        {
            String queryList = args[0];
            String resultspath = args[1];
            String pars = args[2];
            Integer featuretype = Integer.valueOf(args[3]);
            Integer matchertype_cache = Integer.valueOf(args[4]);
            String matcherpars_cache = args[5];
            Integer cache_size = Integer.valueOf(args[6]);
            String serverAdd = args[7];

            FeatureExtractor extractor = Util.createExtractor(featuretype, pars);
            Matcher clientmatcher = Util.createMatcher(matchertype_cache, matcherpars_cache, 3, 0.5);
            Recognizer recognizer = new Recognizer(extractor, clientmatcher);
            AbstractRecogCache<String, KeypointDescList> recogCache = new LFURecogCache<>(new ImageRecognizerInterface(recognizer), cache_size);
            CachedObjRecClient objRecClient = new CachedObjRecClient(recognizer, recogCache, serverAdd,
                                                                     800,
                                                                     Names.Edge, cache_size>0);

            Util.evaluate(objRecClient, queryList, resultspath, null);
        }
        else
        {
            System.err.println("8 Args required, only provided " + args.length);
        }
        System.exit(0);
    }
}
