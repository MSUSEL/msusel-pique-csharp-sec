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
import java.math.MathContext;

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
public class WeightedAverageOfValuedNodesEvaluator extends Evaluator {

    @Override
    public BigDecimal evaluate(ModelNode modelNode) {
    	BigDecimal weightedSum = new BigDecimalWithContext(0.0);    	
    	int numberNonZeroNodes = 0;    	
    	BigDecimal zeroValue = new BigDecimalWithContext(0.5); //value of node with no findings 0 thresholds
    	
    	// Apply weighted sums
        for (ModelNode child : modelNode.getChildren().values()) {
        	BigDecimal[] th = child.getThresholds();
        	if (th!=null) {
	        	if (isZero(th[0],2) && isZero(th[1],2) && isZero(child.getValue().subtract(zeroValue),2)) {
	        		// node has no findings and no thresholds, ignore it
	        	}
	        	else {
	        		numberNonZeroNodes+=1;
	            	weightedSum = weightedSum.add( child.getValue().multiply( modelNode.getWeight(child.getName()) ) );
	        	}
        	}
        	else { //we're in the benchmark stage still
        		weightedSum = weightedSum.add( child.getValue().multiply( modelNode.getWeight(child.getName()) ) );
        		numberNonZeroNodes+=1;
        	}
        }
        
        if (numberNonZeroNodes==0) {
        	weightedSum = new BigDecimalWithContext(0.5);
        }
        else {        	
        	weightedSum = weightedSum.multiply((new BigDecimalWithContext(modelNode.getNumChildren()).divide(new BigDecimalWithContext(numberNonZeroNodes))),BigDecimalWithContext.getMC()); // account for nodes that we ignore
        }
        
        return weightedSum;
    }
    
    public boolean isZero(BigDecimal value, int threshold){
    	MathContext mc = new MathContext(threshold);
    	BigDecimal v = new BigDecimal(value.doubleValue(),mc);
    	return (v.compareTo(new BigDecimal("0.0",mc))==0);
    }
}
