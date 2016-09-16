import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.rpc.CachedObjRecClient;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.objrec.rpc.ObjRecServer;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateClientServer
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length >= 3)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];
            String pars = args[3];
            String pars_db = args[4];
            Integer featuretype = Integer.valueOf(args[5]);
            Integer matchertype_db = Integer.valueOf(args[6]);
            String matcherpars_db = args[7];
            Integer matchertype_cache = Integer.valueOf(args[8]);
            String matcherpars_cache = args[9];
            Integer cache_size = Integer.valueOf(args[10]);
            String serverAdd = args[11];


            FeatureExtractor dbextractor = Util.createExtractor(featuretype, pars_db);
            FeatureExtractor extractor = Util.createExtractor(featuretype, pars);
            Matcher servermatcher = Util.createMatcher(matchertype_db, matcherpars_db, 3, 0.6);
            ObjRecServer objRecServer = new ObjRecServer(dbextractor, extractor, servermatcher, DBdirpath, serverAdd);
//            Matcher cloudletmatcher = Util.createMatcher(matchertype_db, matcherpars_db, matchercache_size*5, 6, 0.7);
//            ObjRecCloudlet objRecCloudlet = new ObjRecCloudlet(extractor, cloudletmatcher, serverAdd, "192.168.25.145:10101");

            Matcher clientmatcher = Util.createMatcher(matchertype_cache, matcherpars_cache, 5, 0.8);
            CachedObjRecClient objRecClient = new CachedObjRecClient(extractor, clientmatcher, serverAdd,
                                                                     Names.Edge, cache_size);

//            ObjRecClient objRecClient = new ObjRecClient(serverAdd);

            Util.evaluate(objRecClient, queryList, resultspath);

        }
        else
        {
            System.out.println("NOT ENOUGH ARGS");
        }
        System.exit(0);
    }

}
