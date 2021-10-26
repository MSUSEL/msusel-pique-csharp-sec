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
 * Evaluator for Quality Aspects. Evaluates as a sum of nodes*weights but limits the score to [0,1]
 * @author Andrew
 *
 */
public class QualityAspectEvaluator extends Evaluator {

    @Override
    public BigDecimal evaluate(ModelNode modelNode) {
    	BigDecimal weightedSum = new BigDecimalWithContext(0.0);
    	
        // Apply weighted sums
        for (ModelNode child : modelNode.getChildren().values()) {
        	weightedSum = weightedSum.add(child.getValue().multiply(modelNode.getWeight(child.getName())));
        }
        if (weightedSum.compareTo(new BigDecimalWithContext(1.0)) >1.0) {//weightedSum>1.0
        	weightedSum = new BigDecimalWithContext(1.0);
        }
        else if (weightedSum.compareTo(new BigDecimalWithContext(0.0))<0) {//weightedSum<0.0
        	weightedSum = new BigDecimalWithContext(0.0);
        }
        
        return weightedSum;
    }
    
    public int numberOfNonZeroWeightChildren(ModelNode modelNode) {
    	int count = 0;
    	
    	for (ModelNode child : modelNode.getChildren().values()) {
        	if (modelNode.getWeight(child.getName()).compareTo(new BigDecimalWithContext(0.0))>0) count++;//modelNode.getWeight(child.getName())>0
        }
    	return count;
    }
}
