import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.image.ImageUInt8;
import org.crowdcache.objrec.boofcv.Recognizer;
import org.crowdcache.objrec.boofcv.surf.SURFExtractor;
import org.crowdcache.objrec.boofcv.surf.SurfBFAssociator;

import java.io.*;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateBoofCV
{
    public static void main(String args[]) throws IOException
    {
        if (args.length == 3)
        {
            String queryList = args[0];
            String DBdirpath = args[1];
            String resultspath = args[2];

            SURFExtractor surfExtractor = new SURFExtractor();
            SurfBFAssociator surfBFAssociator = new SurfBFAssociator();
            Recognizer<ImageUInt8, ScalePoint, BrightFeature> recognizer = new Recognizer<ImageUInt8, ScalePoint, BrightFeature>(surfExtractor, surfBFAssociator, DBdirpath, ImageUInt8.class);

            BufferedReader dir = new BufferedReader(new FileReader(queryList));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

            Integer count = 0;
            String line = dir.readLine();
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];

                Long start = System.currentTimeMillis();
                String result = recognizer.recognize(UtilImageIO.loadImage(imgpath, ImageUInt8.class));
                Long end = System.currentTimeMillis();
                if(result == null)
                    result = "None";
                resultsfile.write(img + "," + result + "," + Long.toString(end - start) + "\n");
                System.out.println("Input:" + imgpath + " Matched:" + result);
                System.out.println("Time:" + (System.currentTimeMillis() - start));
                line = dir.readLine();
                count++;
            }while ((line != null) && (count <= 5));
            resultsfile.flush();
            resultsfile.close();
        }
        System.exit(0);
    }
}
