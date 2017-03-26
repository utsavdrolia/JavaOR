import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.CachedObjRecClient;
import edu.cmu.edgecache.objrec.rpc.Names;
import org.opencv.core.Core;

import java.io.IOException;

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

            CachedObjRecClient objRecClient = Util.createOptCacheObjRecClient(featuretype,
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

            Util.evaluate(objRecClient, queryList, resultspath, null);
        } else
        {
            System.err.println("10 Args required, provided " + args.length);
        }
        System.exit(0);
    }

}
