import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.rpc.CachedObjRecClient;
import org.crowdcache.objrec.rpc.ObjRecCallback;
import org.crowdcache.objrec.rpc.ObjRecServer;
import org.crowdcache.objrec.rpc.ObjRecServiceProto;
import org.opencv.core.Core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
            Integer matchercache_size = Integer.valueOf(args[6]);
            String serverAdd = args[7];

            FeatureExtractor extractor = Util.createExtractor(featuretype, pars);
            Matcher clientmatcher = Util.createMatcher(matchertype_cache, matcherpars_cache, 6, 0.8);
            CachedObjRecClient objRecClient = new CachedObjRecClient(extractor, clientmatcher, serverAdd, "Cloudlet");

            Util.evaluate(objRecClient, queryList, resultspath);
        }
        else
        {
            System.out.println("8 Args required, only provided " + args.length);
        }
        System.exit(0);
    }
}
