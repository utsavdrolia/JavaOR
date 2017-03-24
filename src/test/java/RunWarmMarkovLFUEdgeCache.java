import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.CachedObjRecClient;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.objrec.rpc.ObjRecEdge;
import edu.cmu.edgecache.objrec.rpc.PredictionManager;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class RunWarmMarkovLFUEdgeCache
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 8)
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

            // For Predictor
            String priors_path = args[7];

            CachedObjRecClient lfuCacheObjRecClient = Util.createLFUCacheObjRecClient(featuretype,
                                                                                      feature_pars,
                                                                                      matchertype_cache,
                                                                                      matcherpars_cache,
                                                                                      3,
                                                                                      0.5,
                                                                                      serverAdd,
                                                                                      Names.Edge,
                                                                                      cache_size);

            new ObjRecEdge(lfuCacheObjRecClient, myaddress, new PredictionManager<String>(priors_path));

            while(System.in.available() == 0)
            {
                Thread.sleep(1000);
            }
        }
        else
        {
            System.err.println("8 Args required, only provided " + args.length);
        }
        System.out.println("Exiting");
        System.exit(0);
    }
}
