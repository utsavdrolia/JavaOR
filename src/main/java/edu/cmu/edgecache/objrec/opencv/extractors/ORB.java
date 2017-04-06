package edu.cmu.edgecache.objrec.opencv.extractors;

import edu.cmu.edgecache.objrec.opencv.FeatureExtractor;
import edu.cmu.edgecache.objrec.opencv.KeypointDescList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by utsav on 2/8/16.
 */
public class ORB extends FeatureExtractor
{
    String pathToPars;
    Integer numDescsToExtract = 0;

    public ORB()
    {
        //Init detector
        detector = FeatureDetector.create(FeatureDetector.ORB);
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    }

    /**
     *
     * @param pars Path to parameters for ORB
     */
    public ORB(String pars) throws IOException
    {
        pathToPars = pars;
        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.read(pars);
    }

    @Override
    public void updateNumDescriptorsExtracted(int num_descriptors) throws IOException
    {
        if(num_descriptors != this.numDescsToExtract)
        {
            BufferedReader reader = new BufferedReader(new FileReader(pathToPars));

            String line = reader.readLine();
            List<String> lines = new ArrayList<>();
            do
            {
                if(line.startsWith(NUM_FEATURES_KEY))
                    line = NUM_FEATURES_KEY + ": " + String.valueOf(num_descriptors);
                lines.add(line);
                line = reader.readLine();
            }
            while ((line != null));
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(pathToPars));
            for (String lin : lines)
            {
                writer.write(lin);
                writer.newLine();
            }

            writer.close();

            numDescsToExtract = num_descriptors;

            extractor.read(pathToPars);
        }
    }

    public static void main(String args[]) throws IOException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length > 0)
        {
            String inputFile = args[0];
            String pars = args[1];
            Mat image = Highgui.imread(inputFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            // run each example
            Long start = System.currentTimeMillis();
            ORB orb = new ORB(pars);
            KeypointDescList points = orb.extract(image);
            System.out.println("Time:" + (System.currentTimeMillis() - start) + " Found:" + points.points.size());
            orb.updateNumDescriptorsExtracted(5000);
            System.out.println("Updated pars");
        }
    }
}
