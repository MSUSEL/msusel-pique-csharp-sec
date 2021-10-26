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
package evaluatorTests;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import evaluator.BinaryUtility;
import pique.utility.BigDecimalWithContext;

public class binaryUtilityTests {

	@Test
	public void basicUtilityFunctionTest() {
		BinaryUtility b = new BinaryUtility();
		BigDecimal v = new BigDecimalWithContext(5.0);
		BigDecimal[] thresholds = {new BigDecimalWithContext(0.0),new BigDecimalWithContext(10.0)};
		BigDecimal output = b.utilityFunction(v, thresholds, false);
		assertTrue(output.compareTo(new BigDecimalWithContext(0.5))==0);
	}
	
	@Test
	public void utilityFunctionOutputsNegativeValuesTest() {
		BinaryUtility b = new BinaryUtility();
		BigDecimal v = new BigDecimalWithContext(15.0);
		BigDecimal[] thresholds = {new BigDecimalWithContext(5.0),new BigDecimalWithContext(10.0)};
		BigDecimal output = b.utilityFunction(v, thresholds, false);
		assertTrue(output.compareTo(new BigDecimalWithContext(0.0))<0);
	}
	
	@Test
	public void dealsWithNullThresholdsTest() {
		BinaryUtility b = new BinaryUtility();
		BigDecimal v = new BigDecimalWithContext(0.0);
		BigDecimal output = b.utilityFunction(v, null, false);
		assertTrue(output.compareTo(new BigDecimalWithContext(0.0))==0);
	}
}
