import org.crowdcache.Cache;
import org.crowdcache.objreccache.ObjectRecogCache;
import org.crowdcache.objrec.opencv.FeatureExtractor;
import org.crowdcache.objrec.opencv.KeypointDescList;
import org.crowdcache.objrec.opencv.Matcher;
import org.crowdcache.objrec.opencv.Recognizer;
import org.crowdcache.objrec.opencv.extractors.ORB;
import org.crowdcache.objrec.opencv.matchers.BFMatcher_HAM;
import org.crowdcache.objreccache.ObjectRecogCacheNB;
import org.opencv.core.Core;

import java.io.*;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateCache
{
    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 3)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];

            FeatureExtractor extractor = new ORB();
            Matcher matcher = new BFMatcher_HAM();
            Recognizer recognizer = new Recognizer(extractor, matcher, DBdirpath);
            ObjectRecogCache cache = new ObjectRecogCache(50);

            BufferedReader dir = new BufferedReader(new FileReader(queryList));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));
            Byte cachehit = 0;
            Integer count = 0;
            String line = dir.readLine();
            do
            {
                cachehit = 0;
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];
                Long start = System.currentTimeMillis();
                String result;

                Cache.Result<String> res = cache.get(imgpath);
                System.out.println("Confidence:" + res.confidence);
                if(res.confidence > 70 )
                {
                    KeypointDescList input = extractor.extract(imgpath);
                    result = recognizer.recognize(input);
                    if (!result.equals("None"))
                        cache.put(imgpath, result);
                }
                else
                {
                    result = res.value;
                    cachehit = 1;
                }
                Long end = System.currentTimeMillis();

                resultsfile.write(img + "," + result + "," + Long.toString(end - start) + "," + cachehit + "\n");
                System.out.println("Input:" + imgpath + " Matched:" + result);
                System.out.println("Time:" + (System.currentTimeMillis() - start));
                line = dir.readLine();
                count++;
            }while ((line != null));
            resultsfile.flush();
            resultsfile.close();
        }
        System.exit(0);
    }
}
