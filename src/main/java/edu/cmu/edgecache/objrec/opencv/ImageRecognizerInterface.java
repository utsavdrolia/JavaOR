package edu.cmu.edgecache.objrec.opencv;

import edu.cmu.edgecache.recog.RecognizeInterface;

import java.util.Map;

/**
 * Interface mainly used by the recognition cache
 */
public class ImageRecognizerInterface implements RecognizeInterface<String, KeypointDescList>
{
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
        return recognizer.isValid(result);
    }

    @Override
    public String invalid()
    {
        return Recognizer.INVALID;
    }
}
