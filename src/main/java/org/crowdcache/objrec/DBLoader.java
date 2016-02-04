package org.crowdcache.objrec;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/3/16.
 */
public class DBLoader<T extends ImageSingleBand>
{
    private final Class<T> imageType;
    private ExecutorService executorService;
    public DBLoader(Class<T> imageType)
    {
        this.imageType = imageType;
    }

    /**
     * Extract features for one image
     * @param imagepath
     * @return
     */
    public KeypointDescList<ScalePoint, BrightFeature> processOneImage(String imagepath, SURFExtractor<T> extractor)
    {
        T image = UtilImageIO.loadImage(imagepath, imageType);
        return extractor.harder(image);
    }

    /**
     * Extract features for all images in the category
     * @param dirpath
     * @return
     */
    public HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> processCat(String dirpath, final SURFExtractor<T> extractor)
    {
        final HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> catMap = new HashMap<String, KeypointDescList<ScalePoint, BrightFeature>>();
        HashMap<String, Future<KeypointDescList<ScalePoint, BrightFeature>>> futMap = new HashMap<String, Future<KeypointDescList<ScalePoint, BrightFeature>>>();
        File dir = new File(dirpath);
        if(dir.isDirectory())
        {
            String[] imagedirs = dir.list();
            for (String imagedir : imagedirs)
            {
                final String imgname = imagedir;
                imagedir = dirpath + File.separator + imagedir;
                final String imagepath = imagedir + File.separator + imgname + ".jpg";
                File imagefile = new File(imagepath);
                if (imagefile.exists())
                {
                    futMap.put(imgname, executorService.submit(new Callable<KeypointDescList<ScalePoint, BrightFeature>>()
                    {
                        public KeypointDescList<ScalePoint, BrightFeature> call() throws Exception
                        {
                            return processOneImage(imagepath, new SURFExtractor<T>(imageType));
                        }
                    }));
                    System.out.println("Processing " + imagepath);
                }
                else
                    System.out.println("Could not find image");
            }

            for(Map.Entry<String, Future<KeypointDescList<ScalePoint, BrightFeature>>> future:futMap.entrySet())
            {
                try
                {
                    catMap.put(future.getKey(), future.getValue().get());
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
        }
        return catMap;
    }


    public HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> processDB(String dirpath)
    {
        executorService = Executors.newFixedThreadPool(24);
        SURFExtractor<T> extractor = new SURFExtractor<T>(imageType);
        HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> dbMap = new HashMap<String, KeypointDescList<ScalePoint, BrightFeature>>();
        File dir = new File(dirpath);
        if(dir.isDirectory())
        {
            String[] catdirs = dir.list();
            for (String cat : catdirs)
            {
                cat = dirpath + File.separator + cat;
                System.out.println("Processing " + cat);
                dbMap.putAll(processCat(cat, extractor));
            }
        }
        executorService.shutdown();
        return dbMap;
    }

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            String inputFile = args[0];
            Long start = System.currentTimeMillis();
            DBLoader<ImageFloat32> dbLoader = new DBLoader<ImageFloat32>(ImageFloat32.class);
            HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> map = dbLoader.processDB(inputFile);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + "ms Num:" + map.size());
        }

    }
}
