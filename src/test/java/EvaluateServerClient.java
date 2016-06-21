import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.extractors.SIFTFeatureExtractor;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM_NB;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_L2_NB;
import org.crowdcache.objrec.opencv.matchers.LSHMatcher_HAM;
import org.crowdcache.objrec.rpc.*;
import org.opencv.core.Core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateServerClient
{
    private static ConcurrentHashMap<String, Result> resultMap = new ConcurrentHashMap<>();

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
            Integer matchercache_size = Integer.valueOf(args[10]);
            String serverAdd = args[11];


            FeatureExtractor dbextractor = Util.createExtractor(featuretype, pars_db);
            FeatureExtractor extractor = Util.createExtractor(featuretype, pars);
            Matcher servermatcher = Util.createMatcher(matchertype_db, matcherpars_db, -1);
            ObjRecServer objRecServer = new ObjRecServer(dbextractor, extractor, servermatcher, DBdirpath, "192.168.25.145:10101");

            Matcher cloudletmatcher = Util.createMatcher(matchertype_db, matcherpars_db, matchercache_size*5);
            ObjRecCloudlet objRecCloudlet = new ObjRecCloudlet(extractor, cloudletmatcher, serverAdd, "192.168.25.145:10101");

            Matcher clientmatcher = Util.createMatcher(matchertype_cache, matcherpars_cache, matchercache_size);
            CachedObjRecClient objRecClient = new CachedObjRecClient(extractor, clientmatcher, serverAdd, "Client");

//            ObjRecClient objRecClient = new ObjRecClient(serverAdd);
            BufferedReader dir = new BufferedReader(new FileReader(queryList));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

            Integer count = 0;
            String line = dir.readLine();
            Long procstart = System.currentTimeMillis();
            ArrayList<String> querylist = new ArrayList<>();
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0] + "_" + count.toString();
                String imgpath = chunks[1];
                querylist.add(img);
                objRecClient.recognize(imgpath, new EvaluateCallback(System.currentTimeMillis(), img));
                Thread.sleep(500);
                line = dir.readLine();
                count++;
                //System.out.println(count);
            }while ((line != null));
//            resultsfile.write(img + "," + result + "," + Long.toString(end - start) + "\n");
//            System.out.println(img + "," + result + "," + Long.toString(end - start));
            for(String key: querylist)
            {
                while(!resultMap.containsKey(key));
            }
            System.out.println("Results:\n" + resultMap.toString());
            Long procend = System.currentTimeMillis() - procstart;
            //System.out.println("Time:" + procend + " Count:" + count);
            resultsfile.flush();
            resultsfile.close();
        }
        else
        {
            System.out.println("NOT ENOUGH ARGS");
        }
        System.exit(0);
    }


    private static class EvaluateCallback extends ObjRecCallback
    {
        long startime;
        long endtime;
        String query;
        public EvaluateCallback(long millis, String query)
        {
            super();
            startime = millis;
            this.query = query;
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            endtime = System.currentTimeMillis();
            Result res = new Result();
            res.result = annotation.getAnnotation();
            res.time = annotation.getLatenciesList();
            System.out.println(query + "," + annotation + "," + Long.toString(endtime - startime));
            resultMap.put(query, res);
        }
    }

    private static class Result
    {
        public String result;
        public List<ObjRecServiceProto.Latency> time;

        public String toString()
        {
            return result + ":" + String.valueOf(time);
        }
    }
}
