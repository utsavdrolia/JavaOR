package edu.cmu.edgecache.objrec.opencv;

import edu.cmu.edgecache.recog.RecognizeInterface;

import java.util.Map;

/**
 * Created by utsav on 9/8/16.
 */
public class ImageRecognizerInterface implements RecognizeInterface<String, KeypointDescList>
{
    private static final String INVALID = "None";
    private Recognizer recognizer;

    /**
     * @param recognizer The underlying {@link Recognizer} to use
     */
    public ImageRecognizerInterface(Recognizer recognizer)
    {
        this.recognizer = recognizer;
    }

    /**
     * Use Image recognizer {@link Recognizer} to look up value
     * @param value Features of image
     * @return ID/Key if found, null if not
     */
    @Override
    public String recognize(KeypointDescList value)
    {
        return this.recognizer.recognize(value);
    }

    /**
     * Train the {@link Recognizer#matcher} for future look ups
     * @param trainingMap
     */
    @Override
    public void train(Map<String, KeypointDescList> trainingMap)
    {
        this.recognizer.matcher.train(trainingMap);
    }

    @Override
    public boolean isValid(String result)
    {
        return !result.equals(INVALID);
    }

    @Override
    public String invalid()
    {
        return INVALID;
    }
}
