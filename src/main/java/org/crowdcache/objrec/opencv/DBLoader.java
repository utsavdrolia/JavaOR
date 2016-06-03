package org.crowdcache.objrec.opencv;

import org.crowdcache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.opencv.core.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/3/16.
 */
public class DBLoader
{

    /**
     * Format of input file list
     *      imgName,imgPath
     * @param dblistpath
     * @return
     */
    public static HashMap<String, KeypointDescList> processDB(String dblistpath, final FeatureExtractor extractor) throws IOException
    {
        ExecutorService executorService = Executors.newFixedThreadPool(24);
        HashMap<String, KeypointDescList> dbMap = new HashMap<String, KeypointDescList>();
        HashMap<String, Future<KeypointDescList>> futMap = new HashMap<String, Future<KeypointDescList>>();
        BufferedReader dir = new BufferedReader(new FileReader(dblistpath));
        HashMap<String, String> paths = new HashMap<String, String>();
        Long total_KPs = 0l;
        String line = dir.readLine();
        do
        {
            String[] chunks = line.split(",");
            paths.put(chunks[0], chunks[1]);
            line = dir.readLine();
        }while (line != null);

        for (Map.Entry<String, String> image : paths.entrySet())
        {
            final String imagepath = image.getValue();
            File imagefile = new File(imagepath);
            final String imgname = image.getKey();
            if (imagefile.exists())
            {
                futMap.put(imgname, executorService.submit(new Callable<KeypointDescList>()
                {
                    public KeypointDescList call() throws Exception
                    {
                        return extractor.extract(imagepath);
                    }
                }));
                System.out.println("Loading " + imagepath);
            }
            else
                System.out.println("Could not find image");
        }

        for(Map.Entry<String, Future<KeypointDescList>> future:futMap.entrySet())
        {
            try
            {
                dbMap.put(future.getKey(), future.getValue().get());
                total_KPs += future.getValue().get().points.size();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        futMap = null;
        paths = null;
        System.out.println("Total KPs:" + total_KPs);
        return dbMap;
    }

    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length > 0)
        {
            String inputFile = args[0];
            Long start = System.currentTimeMillis();
            SURFFeatureExtractor surfextractor = new SURFFeatureExtractor();
            HashMap<String, KeypointDescList> map = DBLoader.processDB(inputFile, surfextractor);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + "ms Num:" + map.size());
        }
    }
}