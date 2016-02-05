package org.crowdcache.objrec;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import georegression.struct.point.Point2D_F64;
import org.crowdcache.objrec.surf.SURFExtractor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by utsav on 2/3/16.
 */
public class DBLoader<T extends ImageSingleBand, K extends Point2D_F64, D extends TupleDesc>
{
    private final Class<T> imageType;
    private ExecutorService executorService;
    private final FeatureExtractor<T, K, D> extractor;
    public DBLoader(Class<T> imageType, FeatureExtractor<T, K, D> extractor)
    {
        this.imageType = imageType;
        this.extractor = extractor;
    }

    /**
     * Extract features for one image
     * @param imagepath
     * @return
     */
    public KeypointDescList<K, D> processOneImage(String imagepath)
    {
        T image = UtilImageIO.loadImage(imagepath, imageType);
        return extractor.extract(image);
    }

    /**
     * Extract features for all images in the category
     * @param dirpath
     * @return
     */
    public HashMap<String, KeypointDescList<K, D>> processCat(String dirpath)
    {
        final HashMap<String, KeypointDescList<K, D>> catMap = new HashMap<String, KeypointDescList<K, D>>();
        HashMap<String, Future<KeypointDescList<K, D>>> futMap = new HashMap<String, Future<KeypointDescList<K, D>>>();
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
                    futMap.put(imgname, executorService.submit(new Callable<KeypointDescList<K, D>>()
                    {
                        public KeypointDescList<K, D> call() throws Exception
                        {
                            return processOneImage(imagepath);
                        }
                    }));
                    System.out.println("Processing " + imagepath);
                }
                else
                    System.out.println("Could not find image");
            }

            for(Map.Entry<String, Future<KeypointDescList<K, D>>> future:futMap.entrySet())
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


    public HashMap<String, KeypointDescList<K, D>> processDB(String dirpath)
    {
        executorService = Executors.newFixedThreadPool(24);
        SURFExtractor<T> extractor = new SURFExtractor<T>(imageType);
        HashMap<String, KeypointDescList<K, D>> dbMap = new HashMap<String, KeypointDescList<K, D>>();
        File dir = new File(dirpath);
        if(dir.isDirectory())
        {
            String[] catdirs = dir.list();
            for (String cat : catdirs)
            {
                cat = dirpath + File.separator + cat;
                System.out.println("Processing " + cat);
                dbMap.putAll(processCat(cat));
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
            SURFExtractor<ImageFloat32> surfextractor = new SURFExtractor<ImageFloat32>(ImageFloat32.class);
            DBLoader<ImageFloat32, ScalePoint, BrightFeature> dbLoader = new DBLoader<ImageFloat32, ScalePoint, BrightFeature>(ImageFloat32.class, surfextractor);
            HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> map = dbLoader.processDB(inputFile);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + "ms Num:" + map.size());
        }

    }
}
