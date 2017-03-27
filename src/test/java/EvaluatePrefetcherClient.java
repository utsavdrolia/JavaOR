import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.objrec.rpc.PrefetchedObjRecClient;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluatePrefetcherClient
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 9)
        {
            String queryList = args[0];
            String resultspath = args[1];
            String pars = args[2];
            Integer featuretype = Integer.valueOf(args[3]);
            Integer matchertype_cache = Integer.valueOf(args[4]);
            String matcherpars_cache = args[5];
            String f_k_coeffs_path = args[6];
            String recall_k_coeffs_path = args[7];
            String serverAdd = args[8];

            int num_prefetch_features = 800;

            PrefetchedObjRecClient objRecClient = Util.createPrefetchedObjRecClient(featuretype,
                                                                                    pars,
                                                                                    matchertype_cache,
                                                                                    matcherpars_cache,
                                                                                    3,
                                                                                    0.5,
                                                                                    serverAdd,
                                                                                    Names.Edge,
                                                                                    f_k_coeffs_path,
                                                                                    recall_k_coeffs_path,
                                                                                    num_prefetch_features);

            Util.evaluate(objRecClient, queryList, resultspath, null);
        } else
        {
            System.err.println("10 Args required, provided " + args.length);
        }
        System.exit(0);
    }

}
