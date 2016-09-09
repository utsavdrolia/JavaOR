import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.Matcher;
import edu.cmu.edgecache.objrec.opencv.extractors.ORB;
import edu.cmu.edgecache.objrec.opencv.extractors.SIFTFeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.matchers.BFMatcher_HAM_NB;
import edu.cmu.edgecache.objrec.opencv.matchers.BFMatcher_L2_NB;
import edu.cmu.edgecache.objrec.opencv.matchers.LSHMatcher_HAM;
import edu.cmu.edgecache.objrec.rpc.ObjRecCallback;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
import edu.cmu.edgecache.objrec.rpc.ObjRecServiceProto;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by utsav on 6/17/16.
 */
public class Util
{
    private static final int BIN_NN = 1;
    private static final int BIN_NB = 2;
    private static final int FLOAT_NB = 3;
    private static final int LSH = 4;

    private static final int ORB = 1;
    private static final int SIFT = 2;

    private static ConcurrentHashMap<String, Result> resultMap = new ConcurrentHashMap<>();

    public static FeatureExtractor createExtractor(int featuretype, String pars)
    {

        FeatureExtractor extractor;
        switch (featuretype)
        {
            case ORB:
                extractor = new ORB(pars);
                System.out.println("Using ORB");
                break;
            case SIFT:
                extractor = new SIFTFeatureExtractor(pars);
                System.out.println("Using SIFT");
                break;
            default:
                extractor = new ORB(pars);
                break;
        }

        return extractor;
    }

    public static Matcher createMatcher(int matchertype, String pars, int match_t, double score_t)
    {
        Matcher matcher;
        switch (matchertype)
        {
            case BIN_NN:
                matcher = new BFMatcher_HAM_NB();
                System.out.println("Using NN");
                break;
            case BIN_NB:
                matcher = new BFMatcher_HAM_NB(match_t, score_t);
                System.out.println("Using HAM NB");
                break;
            case FLOAT_NB:
                matcher = new BFMatcher_L2_NB();
                System.out.println("Using L2 NB");
                break;
            case LSH:
                matcher = new LSHMatcher_HAM(pars, match_t, score_t);
                System.out.println("Using LSH");
                break;
            default:
                matcher = new BFMatcher_HAM_NB();
                System.out.println("Using Default HAM NB");
                break;
        }

        return matcher;
    }

    public static void evaluate(ObjRecClient objRecClient, String queryList, String resultspath) throws IOException, InterruptedException
    {
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
            while(!resultMap.containsKey(img));
//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).result + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
            line = dir.readLine();
            count++;
        } while ((line != null));

        for(String key: querylist)
        {
            while(!resultMap.containsKey(key));
            resultsfile.write(key.split("_")[0] + "," + resultMap.get(key).result + "," + (1 - (resultMap.get(key).time.size() - 1)) + "," + "\n");
        }
//        System.out.println("Results:\n" + resultMap.toString());
        Long procend = System.currentTimeMillis() - procstart;
        System.out.println("Time:" + procend + " Count:" + count);
        resultsfile.flush();
        resultsfile.close();
    }

    public static void evaluateAsync(ObjRecClient objRecClient, String queryList, String resultspath) throws IOException, InterruptedException
    {
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
            Thread.sleep(100);
//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).result + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
            line = dir.readLine();
            count++;
        } while ((line != null));

        for(String key: querylist)
        {
            while(!resultMap.containsKey(key));
            resultsfile.write(key.split("_")[0] + "," + resultMap.get(key).result + "," + (1 - (resultMap.get(key).time.size() - 1)) + "," + "\n");
        }
//        System.out.println("Results:\n" + resultMap.toString());
        Long procend = System.currentTimeMillis() - procstart;
        System.out.println("Time:" + procend + " Count:" + count);
        resultsfile.flush();
        resultsfile.close();
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
            long dur = endtime - startime;
            Result res = new Result();
            res.result = annotation.getAnnotation();
            res.time = annotation.getLatenciesList();
            System.out.println(query + "," + res.result + "," + String.valueOf(dur));
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
