/**
  * MIT License
 * Copyright (c) 2019 Montana State University Software Engineering Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package evaluator;

import java.math.BigDecimal;

import pique.evaluation.Evaluator;
import pique.model.ModelNode;
import pique.utility.BigDecimalWithContext;

/**
 * Incomplete version of an evaluator still in the works. The idea is that we want to be able to evaluate a node but not lose sensitivity in the model due to 
 * nodes that don't have any findings in the benchmark or binary under analysis. This is not currently (as of writing this) in use in PIQUE-Bin or any other PIQUE
 * model. This is primarily because it has the unfortunate consequence of potentially raising the TQI when we encounter a new vulnerability in some cases. Perhaps
 * that should be an edge case, but for now, this will remain here, not being used. 
 *
 */
public class PriorityMeasureEvaluator extends Evaluator {

    @Override
    public BigDecimal evaluate(ModelNode modelNode) {
    	BigDecimal weightedSum = new BigDecimalWithContext(0.0);
    	
        // Apply weighted sums
        for (ModelNode child : modelNode.getChildren().values()) {
        	weightedSum = weightedSum.add(child.getValue().multiply(modelNode.getWeight(child.getName())));
        }
        
        return weightedSum;
    }
    
    public int numberOfNonZeroWeightChildren(ModelNode modelNode) {
    	int count = 0;
    	
    	for (ModelNode child : modelNode.getChildren().values()) {
        	if (modelNode.getWeight(child.getName()).compareTo(new BigDecimalWithContext(0.0))>0) count++;
        }
    	return count;
    }
}
