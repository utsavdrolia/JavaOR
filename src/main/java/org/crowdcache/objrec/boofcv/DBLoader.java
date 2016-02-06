package org.crowdcache.objrec.boofcv;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.point.Point2D_F64;
import org.crowdcache.objrec.boofcv.surf.SURFExtractor;

import java.io.*;
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

    /**
     * Format of input file list
     *      imgName,imgPath
     * @param dblistpath
     * @return
     */
    public HashMap<String, KeypointDescList<K, D>> processDB(String dblistpath) throws IOException
    {
        executorService = Executors.newFixedThreadPool(24);
        HashMap<String, KeypointDescList<K, D>> dbMap = new HashMap<String, KeypointDescList<K, D>>();
        HashMap<String, Future<KeypointDescList<K, D>>> futMap = new HashMap<String, Future<KeypointDescList<K, D>>>();
        BufferedReader dir = new BufferedReader(new FileReader(dblistpath));
        HashMap<String, String> paths = new HashMap<String, String>();

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
                futMap.put(imgname, executorService.submit(new Callable<KeypointDescList<K, D>>()
                {
                    public KeypointDescList<K, D> call() throws Exception
                    {
                        return processOneImage(imagepath);
                    }
                }));
                System.out.println("Loading " + imagepath);
            }
            else
                System.out.println("Could not find image");
        }

        for(Map.Entry<String, Future<KeypointDescList<K, D>>> future:futMap.entrySet())
        {
            try
            {
                dbMap.put(future.getKey(), future.getValue().get());
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
        return dbMap;
    }

    public static void main(String args[]) throws IOException
    {
        if (args.length > 0)
        {
            String inputFile = args[0];
            Long start = System.currentTimeMillis();
            SURFExtractor surfextractor = new SURFExtractor();
            DBLoader<ImageUInt8, ScalePoint, BrightFeature> dbLoader = new DBLoader<ImageUInt8, ScalePoint, BrightFeature>(ImageUInt8.class, surfextractor);
            HashMap<String, KeypointDescList<ScalePoint, BrightFeature>> map = dbLoader.processDB(inputFile);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + "ms Num:" + map.size());
        }
    }
}