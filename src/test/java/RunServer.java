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
public class RunServer
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 7)
        {
            String DBdirpath = args[0];
            String pars = args[1];
            String pars_db = args[2];
            Integer featuretype = Integer.valueOf(args[3]);
            Integer matchertype_db = Integer.valueOf(args[4]);
            String matcherpars_db = args[5];
            String serverAdd = args[6];


            FeatureExtractor dbextractor = Util.createExtractor(featuretype, pars_db);
            FeatureExtractor extractor = Util.createExtractor(featuretype, pars);
            Matcher servermatcher = Util.createMatcher(matchertype_db, matcherpars_db, 3, 0.6);
            ObjRecServer objRecServer = new ObjRecServer(dbextractor, extractor, servermatcher, DBdirpath, serverAdd);

            while(System.in.available() == 0);
        }
        else
        {
            System.out.println("7 Args required, only provided " + args.length);
        }
        System.out.println("Exiting");
        System.exit(0);
    }
}
