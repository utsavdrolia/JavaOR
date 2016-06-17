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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateServerClient
{
    private static final int BIN_NN = 1;
    private static final int BIN_NB = 2;
    private static final int FLOAT_NB = 3;
    private static final int LSH = 4;

    private static final int ORB = 1;
    private static final int SIFT = 2;

    private static ConcurrentHashMap<String, Result> resultMap = new ConcurrentHashMap<>();

    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length >= 3)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];
//            String pars = args[3];
//            String pars_db = args[4];
//            Integer matchertype = Integer.valueOf(args[5]);
//            Integer featuretype = Integer.valueOf(args[6]);
//            String lshpars = "";
//            if(args.length == 8)
//                lshpars = args[7];

//            FeatureExtractor extractor;
//            FeatureExtractor dbextractor;
//            Matcher matcher;

//            switch (featuretype)
//            {
//                case ORB:
//                    extractor = new ORB(pars);
//                    dbextractor = new ORB(pars_db);
//                    System.out.println("Using ORB");
//                    break;
//                case SIFT:
//                    extractor = new SIFTFeatureExtractor(pars);
//                    dbextractor = new SIFTFeatureExtractor(pars_db);
//                    System.out.println("Using SIFT");
//                    break;
//                default:
//                    extractor = new ORB(pars);
//                    dbextractor = new ORB(pars_db);
//                    break;
//            }
//            switch (matchertype)
//            {
//                case BIN_NN:
//                    matcher = new BFMatcher_HAM();
//                    System.out.println("Using NN");
//                    break;
//                case BIN_NB:
//                    matcher = new BFMatcher_HAM_NB();
//                    System.out.println("Using NB");
//                    break;
//                case FLOAT_NB:
//                    matcher = new BFMatcher_L2_NB();
//                    System.out.println("Using NB");
//                    break;
//                case LSH:
//                    matcher = new LSHMatcher_HAM(lshpars);
//                    System.out.println("Using LSH");
//                    break;
//                default:
//                    matcher = new BFMatcher_HAM_NB();
//                    break;
//            }

            FeatureExtractor extractor = new ORB("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/orb_pars.txt");
            FeatureExtractor dbextractor = new ORB("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/orb_pars_db.txt");
            Matcher servermatcher = new LSHMatcher_HAM("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/lsh_pars.txt", -1);
            ObjRecServer objRecServer = new ObjRecServer(dbextractor, extractor, servermatcher, DBdirpath, "192.168.25.145:12345");

            Matcher clientmatcher = new LSHMatcher_HAM("/Users/utsav/Documents/DATA/Research/HYRAX/Code/JavaOR/lsh_pars.txt", 5);
//            Matcher clientmatcher = new BFMatcher_HAM_NB(5);
            CachedObjRecClient objRecClient = new CachedObjRecClient(extractor, clientmatcher, "192.168.25.145:12345");
//            ObjRecClient objRecClient = new ObjRecClient("192.168.25.145:12345");
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
                //Thread.sleep(1000);
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
        public void run(String annotation)
        {
            endtime = System.currentTimeMillis();
            Result res = new Result();
            res.result = annotation;
            res.time = endtime - startime;
            resultMap.put(query, res);
        }
    }

    private static class Result
    {
        public String result;
        public long time;

        public String toString()
        {
            return result + ":" + String.valueOf(time);
        }
    }
}
