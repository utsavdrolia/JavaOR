import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.CachedObjRecClient;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.objrec.rpc.ObjRecEdge;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class RunOPTEdgeCache
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 7)
        {
            // For all caches
            String pars = args[0];
            Integer featuretype = Integer.valueOf(args[1]);
            Integer matchertype_cache = Integer.valueOf(args[2]);
            String matcherpars_cache = args[3];
            String myaddress = args[4];
            String serverAdd= args[5];

            // For OPT cache
            String f_k_coeffs_path = args[6];
            String recall_k_coeffs_path = args[7];
            String all_objects_path = args[8];

            CachedObjRecClient optCacheObjRecClient = Util.createOptCacheObjRecClient(featuretype,
                                                                              pars,
                                                                              matchertype_cache,
                                                                              matcherpars_cache,
                                                                              3,
                                                                              0.5,
                                                                              serverAdd,
                                                                              Names.Edge,
                                                                              f_k_coeffs_path,
                                                                              recall_k_coeffs_path,
                                                                              all_objects_path);
            new ObjRecEdge(optCacheObjRecClient, myaddress);

            while(System.in.available() == 0)
            {
                Thread.sleep(1000);
            }
        }
        else
        {
            System.out.println("7 Args required, only provided " + args.length);
        }
        System.out.println("Exiting");
        System.exit(0);
    }
}
