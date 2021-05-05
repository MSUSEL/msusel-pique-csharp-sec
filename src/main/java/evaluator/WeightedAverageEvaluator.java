package evaluator;

import pique.evaluation.Evaluator;
import pique.model.ModelNode;

public class WeightedAverageEvaluator extends Evaluator {

    @Override
    public double evaluate(ModelNode modelNode) {
        double weightedSum = 0.0;
        for (ModelNode child : modelNode.getChildren().values()) {
            weightedSum += child.getValue();
        }
        weightedSum = weightedSum / modelNode.getChildren().size();
        return weightedSum;
    }
}
