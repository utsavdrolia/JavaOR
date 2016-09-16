package edu.cmu.edgecache.objrec.opencv;

import edu.cmu.edgecache.objrec.opencv.extractors.SURFFeatureExtractor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        HashMap<String, List<Future<KeypointDescList>>> futMap = new HashMap<>();
        BufferedReader dir = new BufferedReader(new FileReader(dblistpath));
        HashMap<String, List<String>> paths = new HashMap<>();
        Long total_KPs = 0l;
        String line = dir.readLine();
        do
        {
            String[] chunks = line.split(",");
            if(!paths.containsKey(chunks[0]))
                paths.put(chunks[0], new ArrayList<String>());
            paths.get(chunks[0]).add(chunks[1]);
            line = dir.readLine();
        }while (line != null);

        for (Map.Entry<String, List<String>> imagelist : paths.entrySet())
        {
            for (String image: imagelist.getValue())
            {
                final String imagepath = image;
                Mat imagemat = Highgui.imread(imagepath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
                final Mat dst = new Mat();
                CVUtil.resize(imagemat, dst);
                File imagefile = new File(imagepath);
                final String imgname = imagelist.getKey();
                if (imagefile.exists())
                {
                    if(!futMap.containsKey(imgname))
                        futMap.put(imgname, new ArrayList<Future<KeypointDescList>>());
                    futMap.get(imgname).add(executorService.submit(new Callable<KeypointDescList>()
                    {
                        public KeypointDescList call() throws Exception
                        {
                            return extractor.extract(dst);
                        }
                    }));
                    //System.out.println("Loading " + imagepath);
                } else
                    System.out.println("Could not find image");
            }
        }

        for(Map.Entry<String, List<Future<KeypointDescList>>> future:futMap.entrySet())
        {
            for (Future<KeypointDescList> kp: future.getValue())
            {
                try
                {
                    if (!dbMap.containsKey(future.getKey()))
                        dbMap.put(future.getKey(), kp.get());
                    else
                        dbMap.get(future.getKey()).append(kp.get());
                    total_KPs += kp.get().points.size();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
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