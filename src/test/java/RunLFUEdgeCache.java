import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.ObjRecEdge;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class RunLFUEdgeCache
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 7)
        {
            // For all caches
            String feature_pars = args[0];
            Integer featuretype = Integer.valueOf(args[1]);
            Integer matchertype_cache = Integer.valueOf(args[2]);
            String matcherpars_cache = args[3];
            String myaddress = args[4];
            String serverAdd= args[5];

            // For LFU Cache
            Integer cache_size = Integer.valueOf(args[6]);


            FeatureExtractor extractor = Util.createExtractor(featuretype, feature_pars);
            Matcher servermatcher = Util.createMatcher(matchertype_cache, matcherpars_cache, 3, 0.4);
            new ObjRecEdge(extractor, servermatcher, myaddress, serverAdd, cache_size);

            while(System.in.available() == 0)
            {
                Thread.sleep(1000);
            }
        }
        else
        {
            System.err.println("7 Args required, only provided " + args.length);
        }
        System.out.println("Exiting");
        System.exit(0);
    }
}
