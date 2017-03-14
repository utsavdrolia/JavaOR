package edu.cmu.edgecache.objrec.rpc;

import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import edu.cmu.edgecache.objrec.opencv.Recognizer;
import edu.cmu.edgecache.recog.PrefetchedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Created by utsav on 3/11/17.
 */
public class PrefetchedObjRecClient extends CachedObjRecClient
{
    private PrefetchedCache<String, KeypointDescList> prefetchedCache;

    final static Logger logger = LoggerFactory.getLogger(PrefetchedObjRecClient.class);


    public PrefetchedObjRecClient(Recognizer recognizer,
                                  PrefetchedCache<String, KeypointDescList> recogCache,
                                  String serverAdd, String name, boolean enableCache)
    {
        super(recognizer, recogCache, serverAdd, name, enableCache);
        this.prefetchedCache = recogCache;
    }

    /**
     * Calls the client callback with successful recognition result. Calls server for next PDF
     * @param features
     * @param res
     * @param complatency
     * @param client_cb
     */
    @Override
    protected void onHit(ObjRecServiceProto.Features features,
                         String res,
                         ObjRecServiceProto.Latency.Builder complatency,
                         ObjRecCallback client_cb)
    {
        // Calls the client callback with successful recognition result
        super.onHit(features, res, complatency, client_cb);

        logger.debug("HIT: Calling NextPDF");
        // Calls server for next PDF. There is no client call_back for this. The callback processes the PDF and issues prefetch requests accordingly.
        super.ObjRecServiceStub.getNextPDF(super.rpc, ObjRecServiceProto.Annotation.newBuilder().setAnnotation(res).build(), new PrefetcherObjRecCallback(null, null) );
    }

    /**
     * Calls server with features for recognition. The callback will look for PDF in the response and prefetch accordingly
     * @param features
     * @param client_cb
     * @param complatency
     */
    protected void onMiss(ObjRecServiceProto.Features features, ObjRecServiceProto.Latency.Builder complatency, ObjRecCallback client_cb)
    {
        logger.debug("Miss: Forwarding");
        ObjRecServiceStub.recognizeFeatures(rpc, features, new PrefetcherObjRecCallback(client_cb, complatency));
    }



    protected class PrefetcherObjRecCallback extends CachedObjRecClient.CachedObjRecCallback
    {

        PrefetcherObjRecCallback(ObjRecCallback cb, ObjRecServiceProto.Latency.Builder complatency)
        {
            super(cb, complatency);
        }

        @Override
        public void run(ObjRecServiceProto.Annotation annotation)
        {
            if(this.app_cb != null)
            {
                // Run app's cb, fetch this objects features if cache does not have it
                logger.debug("Received #Result# from Edge");
                super.run(annotation);
            }
            //Check PDF
            if(annotation.hasPdf())
            {
                logger.debug("Received *PDF* from Edge");
                Map<String, Double> pdf = Utils.deserialize(annotation.getPdf().getPdfList());
                // Update pdf and get which items to prefetch
                Collection<String> toPrefetch = prefetchedCache.updatePDF(pdf);
                // Send requests to prefetch these items
                for (String item: toPrefetch)
                {
                    logger.debug("Requesting Features:" + item);
                    ObjRecServiceStub.getFeatures(rpc,
                                                  ObjRecServiceProto.Annotation.newBuilder()
                                                  .setAnnotation(item) // Name of item to prefetch
                                                  .setPdf(ObjRecServiceProto.PDF.newBuilder()
                                                          .addPdf(ObjRecServiceProto.ObjectProbability.newBuilder()
                                                                  .setName(item) // Name of item to prefetch
                                                                  .setProbability(pdf.get(item)))).build(), // P(item) - will use for scheduling
                                                  new FeaturesRecvCallback(item));
                }
            }
        }
    }

}
