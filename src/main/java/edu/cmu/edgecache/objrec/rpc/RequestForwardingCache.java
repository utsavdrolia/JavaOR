package edu.cmu.edgecache.objrec.rpc;

import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import edu.cmu.edgecache.objrec.opencv.Recognizer;
import edu.cmu.edgecache.recog.AbstractRecogCache;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by utsav on 4/2/17.
 */
public class RequestForwardingCache extends CachedObjRecClient
{
    // Inter-arrival time tracker
    private SynchronizedDescriptiveStatistics interArrivalTime = new SynchronizedDescriptiveStatistics(100);

    // Last incoming request
    private Long lastRequestTime = null;

    // Recognition Executor
    private ExecutorService recognitionExecutor = Executors.newSingleThreadExecutor();

    private AtomicLong in_queue = new AtomicLong(0);
    private Random choose = new Random();

    final static Logger logger = LoggerFactory.getLogger(RequestForwardingCache.class);

    public RequestForwardingCache(Recognizer recognizer,
                                  AbstractRecogCache<String, KeypointDescList> recogCache,
                                  String serverAdd, Integer num_cache_features, String name, boolean enableCache)
    {
        super(recognizer, recogCache, serverAdd, num_cache_features, name, enableCache);
    }

    /**
     * Check if inter-arrival time is low enough to compute, else send to cloud directly
     * @param request
     * @param cb
     */
    public void recognize(ObjRecServiceProto.Image request, Long req_recv_ts, ObjRecCallback cb)
    {
        long time_in_queue = System.currentTimeMillis() - req_recv_ts;

        if (lastRequestTime == null)
        {
            lastRequestTime = req_recv_ts - 200;
            computeTime.addValue(50);
        }
        // Insert new inter-arrival time
        interArrivalTime.addValue(req_recv_ts - lastRequestTime);
        // Assign new previous request time
        lastRequestTime = req_recv_ts;

        logger.debug("InterArrivalTime:" + interArrivalTime.getMean());
        logger.debug("ComputeTime:" + computeTime.getMean());

        // Compute rate/arrival rate = fraction that can be precessed
        // Assign each request to be processed at edge with this probability

        Double processRate = interArrivalTime.getMean()/computeTime.getMean();

        logger.debug("ProcessRate:" + processRate);

//        if(interArrivalTime.getMean() >= computeTime.getMean())
        if(choose.nextDouble() <= 0.9*processRate) // Slightly bias towards offloading to cloud
        {
            // Enough time to compute
            logger.debug("Computing on Edge");
            long in_q = in_queue.incrementAndGet();
            logger.debug("InQ:" + in_q);
            recognitionExecutor.execute(new ImageRecognitionRunner(request, req_recv_ts, cb));
//            super.recognize(request, req_recv_ts, cb);
        }
        else
        {
            // Not enough time to compute, forward to cloud
            logger.debug("Forwarding to Cloud");
            // Insert comp latency
//            computeTime.addValue(0);
            ObjRecServiceProto.Latency.Builder complatency = ObjRecServiceProto.Latency.newBuilder().
                    setInQueue((int) time_in_queue).
                    setName(name).
                    setSize(recogCache.getSize());
            super.onMiss(request, complatency, cb, null);
        }
    }

    /**
     * Check in local knownItems if we have image and if not send request to cloud. Only called when in edge
     * @param features
     * @param cb
     */
    public void recognize(ObjRecServiceProto.Features features, Long req_recv_ts, ObjRecCallback cb)
    {
        long time_in_queue = System.currentTimeMillis() - req_recv_ts;

        if (lastRequestTime == null)
        {
            lastRequestTime = req_recv_ts - 200;
            computeTime.addValue(50);
        }
        // Insert new inter-arrival time
        interArrivalTime.addValue(req_recv_ts - lastRequestTime);
        // Assign new previous request time
        lastRequestTime = req_recv_ts;

        logger.debug("InterArrivalTime:" + interArrivalTime.getMean());
        logger.debug("ComputeTime:" + computeTime.getMean());

        Double processRate = interArrivalTime.getMean()/computeTime.getMean();
        logger.debug("ProcessRate:" + processRate);

//        if(interArrivalTime.getMean() >= computeTime.getMean())
        if(choose.nextDouble() <= 0.9*processRate) // Slightly bias towards offloading to cloud
        {
            // Enough time to compute
            logger.debug("Computing on Edge");
            long in_q = in_queue.incrementAndGet();
            logger.debug("InQ:" + in_q);
            recognitionExecutor.execute(new FeatureRecognitionRunner(features, req_recv_ts, cb));
//            super.recognize(features, req_recv_ts, cb);
        }
        else
        {
            // Not enough time to compute, forward to cloud
            logger.debug("Forwarding to Cloud");
            // Insert comp latency
//            computeTime.addValue(0);
            ObjRecServiceProto.Latency.Builder complatency = ObjRecServiceProto.Latency.newBuilder().
                    setInQueue((int) time_in_queue).
                    setName(name).
                    setSize(recogCache.getSize());
            super.onMiss(features, complatency, cb, null);
        }
    }

    private class ImageRecognitionRunner implements Runnable
    {

        private ObjRecServiceProto.Image request;
        private Long req_recv_ts;
        private ObjRecCallback cb;

        public ImageRecognitionRunner(ObjRecServiceProto.Image request,
                                      Long req_recv_ts,
                                      ObjRecCallback cb)
        {
            this.request = request;
            this.req_recv_ts = req_recv_ts;
            this.cb = cb;
        }

        @Override
        public void run()
        {
            try
            {
                _recognize(request, req_recv_ts, cb);
                in_queue.decrementAndGet();
            }
            catch (Exception e)
            {
                logger.error("Error in Recognition Thread", e);
            }
        }
    }

    private class FeatureRecognitionRunner implements Runnable
    {

        private ObjRecServiceProto.Features request;
        private Long req_recv_ts;
        private ObjRecCallback cb;

        public FeatureRecognitionRunner(ObjRecServiceProto.Features request,
                                        Long req_recv_ts,
                                        ObjRecCallback cb)
        {
            this.request = request;
            this.req_recv_ts = req_recv_ts;
            this.cb = cb;
        }

        @Override
        public void run()
        {
            try
            {
                _recognizeFeatures(request, req_recv_ts, cb);
                in_queue.decrementAndGet();
            }
            catch (Exception e)
            {
                logger.error("Error in Recognition Thread", e);
            }
        }
    }

    private void _recognize(ObjRecServiceProto.Image request, Long req_recv_ts, ObjRecCallback cb)
    {
        super.recognize(request, req_recv_ts, cb);
    }

    private void _recognizeFeatures(ObjRecServiceProto.Features request, Long req_recv_ts, ObjRecCallback cb)
    {
        super.recognize(request, req_recv_ts, cb);
    }

}
