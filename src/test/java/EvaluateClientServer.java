import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
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
            Integer matchertype_db = Integer.valueOf(args[5]);
            Integer featuretype = Integer.valueOf(args[6]);
            String matcherpars_db = args[7];
            String serverAdd = "localhost:12345";

            FeatureExtractor dbextractor = Util.createExtractor(featuretype, pars_db);
            FeatureExtractor extractor = Util.createExtractor(featuretype, pars_db);
            Matcher servermatcher = Util.createMatcher(matchertype_db, matcherpars_db, 3, 0.5);
            ObjRecServer objRecServer = new ObjRecServer(dbextractor, extractor, servermatcher, DBdirpath, serverAdd);

            ObjRecClient objRecClient = new ObjRecClient(serverAdd);

            Util.evaluate(objRecClient, queryList, resultspath);

        }
        else
        {
            System.err.println("NOT ENOUGH ARGS");
        }
        System.exit(0);
    }

}
