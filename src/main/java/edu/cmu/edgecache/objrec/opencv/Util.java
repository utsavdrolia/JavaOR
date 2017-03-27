package edu.cmu.edgecache.objrec.opencv;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmu.edgecache.objrec.opencv.extractors.ORB;
import edu.cmu.edgecache.objrec.opencv.extractors.SIFTFeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.matchers.BFMatcher_HAM_NB;
import edu.cmu.edgecache.objrec.opencv.matchers.BFMatcher_L2_NB;
import edu.cmu.edgecache.objrec.opencv.matchers.LSHMatcher_HAM;
import edu.cmu.edgecache.objrec.rpc.*;
import edu.cmu.edgecache.recog.AbstractRecogCache;
import edu.cmu.edgecache.recog.LFURecogCache;
import edu.cmu.edgecache.recog.OptRecogCache;
import edu.cmu.edgecache.recog.PrefetchedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

/**
 * Created by utsav on 6/17/16.
 */
public class Util
{
    private static final String REQUEST = "request";
    private static final String STARTTIME = "start";
    private static final String ENDTIME = "end";
    private static final String RESPONSE = "response";
    private static final String LATENCIES = "latencies";
    private static final String REQUEST_ID = "ID";
    public static final int BIN_NN = 1;
    public static final int BIN_NB = 2;
    public static final int FLOAT_NB = 3;
    public static final int LSH = 4;

    public static final int ORB = 1;
    public static final int SIFT = 2;

    public static final String LFU_cache = "LFU";
    public static final String Opt_cache = "OPT";
    final static Logger logger = LoggerFactory.getLogger(Util.class);


    public static FeatureExtractor createExtractor(int featuretype, String pars)
    {

        FeatureExtractor extractor;
        switch (featuretype)
        {
            case ORB:
                extractor = new ORB(pars);
                logger.debug("Using ORB");
                break;
            case SIFT:
                extractor = new SIFTFeatureExtractor(pars);
                logger.debug("Using SIFT");
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
                logger.debug("Using NN");
                break;
            case BIN_NB:
                matcher = new BFMatcher_HAM_NB(match_t, score_t);
                logger.debug("Using HAM NB");
                break;
            case FLOAT_NB:
                matcher = new BFMatcher_L2_NB();
                logger.debug("Using L2 NB");
                break;
            case LSH:
                matcher = new LSHMatcher_HAM(pars, match_t, score_t);
                logger.debug("Using LSH");
                break;
            default:
                matcher = new BFMatcher_HAM_NB();
                logger.warn("Using Default HAM NB");
                break;
        }

        return matcher;
    }

    public static double[] getCoefs(String path) throws IOException
    {
        BufferedReader dir = new BufferedReader(new FileReader(path));
        String line = dir.readLine();
        String[] chunks = line.split(",");
        double[] coeffs = new double[chunks.length];
        for (int i = 0; i < chunks.length; i++)
        {
            coeffs[i] = Double.parseDouble(chunks[i]);
        }
        return coeffs;
    }

    /**
     * Create a Opt Cache based recognition client
     * @param featureType
     * @param featurePars
     * @param matcherType
     * @param matcherPars
     * @param match_thresh
     * @param score_thresh
     * @param nextLevelAddress
     * @param name
     * @param F_K_PARS
     * @param RECALL_PARS
     * @param all_objects_path
     * @return
     * @throws IOException
     */
    public static CachedObjRecClient createOptCacheObjRecClient(int featureType,
                                                                String featurePars,
                                                                int matcherType,
                                                                String matcherPars,
                                                                int match_thresh,
                                                                double score_thresh,
                                                                String nextLevelAddress,
                                                                String name,
                                                                String F_K_PARS,
                                                                String RECALL_PARS,
                                                                String all_objects_path) throws IOException
    {

        FeatureExtractor extractor = Util.createExtractor(featureType, featurePars);
        Matcher clientmatcher = Util.createMatcher(matcherType, matcherPars, match_thresh, score_thresh);
        Recognizer recognizer = new Recognizer(extractor, clientmatcher);

        AbstractRecogCache<String, KeypointDescList> recogCache = new OptRecogCache<>(new ImageRecognizerInterface(recognizer),
                                                                                      getCoefs(F_K_PARS),
                                                                                      getCoefs(RECALL_PARS),
                                                                                      get_all_objects(all_objects_path));
        return new CachedObjRecClient(recognizer, recogCache, nextLevelAddress, null, name, true);
    }


    public static PrefetchedObjRecClient createPrefetchedObjRecClient(int featureType,
                                                                      String featurePars,
                                                                      int matcherType,
                                                                      String matcherPars,
                                                                      int match_thresh,
                                                                      double score_thresh,
                                                                      String nextLevelAddress,
                                                                      String name,
                                                                      String F_K_PARS,
                                                                      String RECALL_PARS,
                                                                      int num_prefetch_features) throws IOException
    {

        FeatureExtractor extractor = Util.createExtractor(featureType, featurePars);
        Matcher clientmatcher = Util.createMatcher(matcherType, matcherPars, match_thresh, score_thresh);
        Recognizer recognizer = new Recognizer(extractor, clientmatcher);

        PrefetchedCache<String, KeypointDescList> recogCache = new PrefetchedCache<>(new ImageRecognizerInterface(recognizer),
                                                                                      getCoefs(F_K_PARS),
                                                                                      getCoefs(RECALL_PARS));
        return new PrefetchedObjRecClient(recognizer, recogCache, nextLevelAddress, name, num_prefetch_features);
    }


    public static CachedObjRecClient createLFUCacheObjRecClient(int featureType,
                                                                String featurePars,
                                                                int matcherType,
                                                                String matcherPars,
                                                                int match_thresh,
                                                                double score_thresh,
                                                                String nextLevelAddress,
                                                                String name,
                                                                Integer cache_size) throws IOException
    {

        FeatureExtractor extractor = Util.createExtractor(featureType, featurePars);
        Matcher clientmatcher = Util.createMatcher(matcherType, matcherPars, match_thresh, score_thresh);
        Recognizer recognizer = new Recognizer(extractor, clientmatcher);

        AbstractRecogCache<String, KeypointDescList> recogCache = new LFURecogCache<>(new ImageRecognizerInterface(recognizer), cache_size);
        return new CachedObjRecClient(recognizer, recogCache, nextLevelAddress, null, name, true);
    }

    public static List<String> get_all_objects(String path) throws IOException
    {
        BufferedReader all_objects_file = new BufferedReader(new FileReader(path));

        String line = all_objects_file.readLine();
        ArrayList<String> all_objects = new ArrayList<>();
        do
        {
            all_objects.add(line.split(",")[1]);
            line = all_objects_file.readLine();
        } while ((line != null));
        return all_objects;
    }

    public static void evaluate(ObjRecClient objRecClient,
                                String queryList,
                                String resultspath,
                                AppCallBack appCallBack) throws IOException, InterruptedException
    {
        ConcurrentLinkedQueue<EvaluateCallback> evaluateCallbacks = new ConcurrentLinkedQueue<>();
        BufferedReader dir = new BufferedReader(new FileReader(queryList));

        Integer count = 0;
        String line = dir.readLine();
        Long procstart = System.currentTimeMillis();
        do
        {
            count++;
            String[] chunks = line.split(",");
            String img = chunks[0];
            String imgpath = chunks[1];
            EvaluateCallback cb = new EvaluateCallback(System.currentTimeMillis(), img, count, appCallBack);
            logger.debug("Issuing request:" + img + " @ " + imgpath);
            objRecClient.recognize(imgpath, cb);
            evaluateCallbacks.add(cb);
            while(!cb.isDone(60000))
            {
                sleep(100);
            }
            sleep(200);
            Result result = cb.getResult();
            logger.debug(img + "," +
                               result.getAnnotation());
//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).annotation + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
            line = dir.readLine();
        } while ((line != null));

        writeResultFile(evaluateCallbacks, resultspath);
        Long procend = System.currentTimeMillis() - procstart;
        logger.debug("Time:" + procend + " Count:" + count);
    }

    public static void evaluateAsync(ObjRecClient objRecClient, String queryList, String resultspath, AppCallBack app_cb) throws IOException, InterruptedException
    {
        ConcurrentLinkedQueue<Util.EvaluateCallback> evaluateCallbacks = new ConcurrentLinkedQueue<>();
        BufferedReader trace = new BufferedReader(new FileReader(queryList));

        Integer count = 0;
        String line = trace.readLine();
        Long procstart = System.currentTimeMillis();
        do
        {
            count++;
            // Parse
            String[] chunks = line.split(",");
            String img = chunks[0];
            String imgpath = chunks[1];
            Long req_time = Long.valueOf(chunks[2]);
            try
            {
                // Wait till assigned time
                sleep(req_time - (System.currentTimeMillis() - procstart));
            }
            catch (IllegalArgumentException e)
            {
                // Thrown if argument is negative. If negative, don't sleep. Drop exception like its hot.
            }
            // Create callback
            Util.EvaluateCallback cb = new Util.EvaluateCallback(System.currentTimeMillis(), img, count, app_cb);

            logger.debug("Issuing request:" + img);
            // Issue request
            objRecClient.recognize(imgpath, cb);
            evaluateCallbacks.add(cb);

//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).annotation + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
            line = trace.readLine();
        } while ((line != null));
//        System.out.println("Results:\n" + resultMap.toString());

        Long procend = System.currentTimeMillis() - procstart;
        logger.debug("Time:" + procend + " Count:" + count);

        // Write results to file
        writeResultFile(evaluateCallbacks, resultspath);
        logger.debug("Wrote results file");
    }

    /**
     * Writes JSON file. Format
     * [{REQUEST:"",
     *   START:ms,
     *   END:ms,
     *   RESPONSE:"",
     *   LATENCIES:{DEVICE:{QUEUE:ms, COMPUTE:ms, NEXT:ms}, ... }},
     *   {REQUEST:"", ...}
     *  ]
     * @param evaluateCallbacks
     * @param resultspath
     * @throws IOException
     */
    public static void writeResultFile(Collection<EvaluateCallback> evaluateCallbacks, String resultspath) throws IOException
    {
        BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> output_array = new ArrayList<>();
        try
        {
            for (Util.EvaluateCallback callback : evaluateCallbacks)
            {
                Map<String, Object> json = new HashMap<>();
                json.put(REQUEST_ID, callback.getID());
                json.put(REQUEST, callback.getQuery());
                json.put(STARTTIME, callback.getStartime());
                // Ensure callback is processed
                if (callback.isDone(60000))
                {
                    Util.Result result = callback.getResult();
                    Map<String, Map<String, Integer>> map = result.getLatencies();
                    json.put(ENDTIME, callback.getEndtime());
                    json.put(RESPONSE, result.getAnnotation());
                    json.put(LATENCIES, map);
                    logger.debug("Responses Received to Req:" + callback.getID());
                } else
                {
                    json.put(ENDTIME, -1);
                    logger.warn("Responses *NOT* Received to Req:" + callback.getID());
                }
                output_array.add(json);
            }
        }
        catch (InterruptedException ignored)
        {}
        finally
        {
            mapper.writeValue(resultsfile, output_array);
            resultsfile.close();
        }
    }

    public static ObjRecClient createLocalObjRecClient(int featureType,
                                                       String featurePars,
                                                       String db_featurePars,
                                                       int matcherType,
                                                       String matcherPars,
                                                       int match_thresh, double score_thresh,
                                                       String dblistpath,
                                                       String name) throws IOException
    {
        FeatureExtractor extractor = Util.createExtractor(featureType, featurePars);
        FeatureExtractor dbextractor = Util.createExtractor(featureType, db_featurePars);
        Matcher clientmatcher = Util.createMatcher(matcherType, matcherPars, match_thresh, score_thresh);
        return new LocalObjRecClient(dbextractor, extractor, clientmatcher, dblistpath, name);
    }


    public static class EvaluateCallback extends ObjRecCallback
    {
        private long startime;
        private long endtime;
        private String query;
        private Result result;
        private boolean isDone = false;
        private AppCallBack app_cb = null;
        private Integer ID;

        public EvaluateCallback(long millis, String query, Integer id, AppCallBack cb)
        {
            super();
            ID = id;
            startime = millis;
            this.query = query;
            app_cb = cb;
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            endtime = System.currentTimeMillis();
            logger.debug("Received Response");
            result = new Result(annotation.getAnnotation(), annotation.getLatenciesList());
            isDone = true;
            if(app_cb != null)
                app_cb.call(this.query, result.getAnnotation(), endtime - startime);
        }

        public long getStartime()
        {
            return startime;
        }

        /**
         * Wait for result till timeout, return accordingly
         * @param timeout
         * @return
         */
        public boolean isDone(long timeout) throws InterruptedException
        {
            Long start = System.currentTimeMillis();
            while(!isDone)
            {
                sleep(100);
                if((System.currentTimeMillis() - start) > timeout)
                    break;
            }
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

        public Integer getID()
        {
            return ID;
        }
    }


    public static class Result
    {
        private static final String NEXT = "next";
        private static final String COMPUTE = "compute";
        private static final String QUEUE = "inqueue";

        private final String annotation;
        private final List<ObjRecServiceProto.Latency> time;

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
                    if(l.hasNextLevel())
                        latency += l.getNextLevel();
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

        /**
         * Format - {RESPONSE:"", DEVICE:{QUEUE:ms, COMPUTE:ms, NEXT:ms} ... }
         * @return
         */

        public Map<String,Map<String,Integer>> getLatencies()
        {
            Map<String, Map<String,Integer>> json = new HashMap<>();
            for(ObjRecServiceProto.Latency level : this.getTime())
            {
                Map<String, Integer> latency = new HashMap<>();
                if(level.hasNextLevel())
                    latency.put(NEXT, level.getNextLevel());
                if(level.hasComputation())
                    latency.put(COMPUTE, level.getComputation());
                if(level.hasInQueue())
                    latency.put(QUEUE, level.getInQueue());
                json.put(level.getName(), latency);
            }
            return json;
        }
    }

    public abstract static class AppCallBack
    {
        /**
         * Gets called on result
         * @param annotation
         * @param latency
         */
        public abstract void call(String req, String annotation, long latency);
    }
}
