package edu.cmu.edgecache.objrec.opencv;

import edu.cmu.edgecache.objrec.opencv.extractors.ORB;
import edu.cmu.edgecache.objrec.opencv.extractors.SIFTFeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.matchers.BFMatcher_HAM_NB;
import edu.cmu.edgecache.objrec.opencv.matchers.BFMatcher_L2_NB;
import edu.cmu.edgecache.objrec.opencv.matchers.LSHMatcher_HAM;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.objrec.rpc.ObjRecCallback;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
import edu.cmu.edgecache.objrec.rpc.ObjRecServiceProto;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

/**
 * Created by utsav on 6/17/16.
 */
public class Util
{
    public static final int BIN_NN = 1;
    public static final int BIN_NB = 2;
    public static final int FLOAT_NB = 3;
    public static final int LSH = 4;

    public static final int ORB = 1;
    public static final int SIFT = 2;

    private static final String LFU_cache = "LFU";
    private static final String Opt_cache = "OPT";

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
        ConcurrentLinkedQueue<EvaluateCallback> evaluateCallbacks = new ConcurrentLinkedQueue<>();
        BufferedReader dir = new BufferedReader(new FileReader(queryList));
        BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

        Integer count = 0;
        String line = dir.readLine();
        Long procstart = System.currentTimeMillis();
        do
        {
            String[] chunks = line.split(",");
            String img = chunks[0];
            String imgpath = chunks[1];
            EvaluateCallback cb = new EvaluateCallback(System.currentTimeMillis(), img);
            objRecClient.recognize(imgpath, cb);
            evaluateCallbacks.add(cb);
            while(!cb.isDone())
            {
                sleep(100);
            }
            sleep(200);
            Result result = cb.getResult();
            System.out.println(img + "," +
                               result.getAnnotation() + "," +
                               (1 - (result.getTime().size() - 1)) + "," +
                               result.getEdgeLatency() + "," +
                               String.valueOf(result.getCacheSize()));
//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).annotation + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
            line = dir.readLine();
            count++;
        } while ((line != null));

        for(EvaluateCallback callback: evaluateCallbacks)
        {
            // Ensure callback is processed
            while(callback.isDone());
            Result result = callback.getResult();
            resultsfile.write(
                    callback.getQuery() + "," +
                            result.getAnnotation() + "," +
                            (1 - (result.getTime().size() - 1)) + "," +
                            String.valueOf(result.getEdgeLatency())+ "," +
                            String.valueOf(result.getCacheSize()) + "\n");
        }
        Long procend = System.currentTimeMillis() - procstart;
        System.out.println("Time:" + procend + " Count:" + count);
        resultsfile.flush();
        resultsfile.close();
    }

    public static void evaluateAsync(ObjRecClient objRecClient, String queryList, String resultspath) throws IOException, InterruptedException
    {
        ConcurrentLinkedQueue<EvaluateCallback> evaluateCallbacks = new ConcurrentLinkedQueue<>();
        BufferedReader dir = new BufferedReader(new FileReader(queryList));
        BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

        Integer count = 0;
        String line = dir.readLine();
        Long procstart = System.currentTimeMillis();
        ArrayList<String> querylist = new ArrayList<>();
        do
        {
            String[] chunks = line.split(",");
            String img = chunks[0];
            String imgpath = chunks[1];
            querylist.add(img);
            EvaluateCallback cb = new EvaluateCallback(System.currentTimeMillis(), img);
            objRecClient.recognize(imgpath, cb);
            evaluateCallbacks.add(cb);
            sleep(100);
//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).annotation + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
            line = dir.readLine();
            count++;
        } while ((line != null));

        for(EvaluateCallback callback: evaluateCallbacks)
        {
            // Ensure callback is processed
            while(callback.isDone());
            Result result = callback.getResult();
            resultsfile.write(
                    callback.getQuery() + "," +
                            result.getAnnotation() + "," +
                            (1 - (result.getTime().size() - 1)) + "," +
                            String.valueOf(result.getEdgeLatency())+ "," +
                            String.valueOf(result.getCacheSize()) + "\n");
        }
//        System.out.println("Results:\n" + resultMap.toString());
        Long procend = System.currentTimeMillis() - procstart;
        System.out.println("Time:" + procend + " Count:" + count);
        resultsfile.flush();
        resultsfile.close();
    }

    public static class EvaluateCallback extends ObjRecCallback
    {
        private long startime;
        private long endtime;
        private String query;
        private Result result;
        private boolean isDone = false;

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
            result = new Result(annotation.getAnnotation(), annotation.getLatenciesList());
            isDone = true;
        }

        public long getStartime()
        {
            return startime;
        }

        public boolean isDone()
        {
            return isDone;
        }

        public long getEndtime()
        {
            return endtime;
        }

        public String getQuery()
        {
            return query;
        }

        public Result getResult()
        {
            return result;
        }
    }


    public static class Result
    {
        private String annotation;
        private List<ObjRecServiceProto.Latency> time;

        public Result(String annotation,
                      List<ObjRecServiceProto.Latency> latenciesList)
        {
            this.annotation = annotation;
            time = latenciesList;
        }

        public String getAnnotation()
        {
            return annotation;
        }

        public List<ObjRecServiceProto.Latency> getTime()
        {
            return time;
        }

        public String toString()
        {
            return annotation + ":" + String.valueOf(time);
        }

        public long getEdgeLatency()
        {
            long latency = 0;
            for (ObjRecServiceProto.Latency l: time)
            {
                if(l.getName().equals(Names.Edge))
                {
                    latency += l.getComputation();
                    if(l.hasNetwork())
                        latency += l.getNetwork();
                }
            }
            return latency;
        }

        public long getCacheSize()
        {
            for (ObjRecServiceProto.Latency l: time)
            {
                if(l.getName().equals(Names.Edge))
                {
                    return l.getSize();
                }
            }
            return  -1;
        }
    }
}
