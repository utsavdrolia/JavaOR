package edu.cmu.edgecache.objrec.rpc;

import edu.cmu.edgecache.recog.predictors.MarkovPredictor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks previous {@link PredictionManager#getNextPDF(String, T)} requests made by entities (differentiated by their unique string ID). Uses this history to train a
 * {@link MarkovPredictor<T>} on the fly.
 * Created by utsav on 3/8/17.
 */
public class PredictionManager<T>
{
    private HashMap<String, T> previous_tracker;
    private MarkovPredictor<T> predictor;

    /**
     * @param states All possible states
     */
    public PredictionManager(List<T> states)
    {
        predictor = new MarkovPredictor<>(states);
        this.previous_tracker = new HashMap<>();
    }


    /**
     * @param states All possible states
     * @param prior Prior
     */
    public PredictionManager(List<T> states, double prior)
    {
        predictor = new MarkovPredictor<>(states, prior);
        this.previous_tracker = new HashMap<>();
    }

    /**
     * Get PDF for next possible states. Also updates predictor
     * @param id ID of Entity making request - e.g. device ID
     * @param from_state Source state
     * @return PDF
     */
    public Map<T, Double> getNextPDF(String id, T from_state)
    {
        Map<T, Double> ret = predictor.getNextPDF(from_state);
        // Update transitions only if from_state is different from the previous recorded state
        if(!previous_tracker.get(id).equals(from_state))
        {
            predictor.incrementTransition(previous_tracker.get(id), from_state);
            this.previous_tracker.put(id, from_state);
        }
        return ret;
    }
}
