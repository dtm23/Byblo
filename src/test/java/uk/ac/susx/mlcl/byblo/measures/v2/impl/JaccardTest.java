/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.measures.v2.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.*;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class JaccardTest {

    @Test
    public void testJaccardCLI() throws Exception {
        System.out.println("testJaccardCLI");

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Jaccard");
        output.delete();

        try {
            enableExistTrapping();
            Tools.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "Jaccard",
                        "--input", TEST_FRUIT_EVENTS.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", output.toString()
                    });
        } finally {
            disableExitTrapping();
        }


        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }

    /**
     * http://people.revoledu.com/kardi/tutorial/Similarity/Jaccard.html
     */
    @Test
    public void testJaccardExample1() {
        System.out.println("Testing Jaccard example1");
        double[] objectA = new double[]{1, 1, 1, 1};
        double[] objectB = new double[]{0, 1, 0, 0};
        int p = 1; // number of variables that positive for both objects
        int q = 3; // number of variables that positive for the th objects and negative for the th object
        int r = 0; // number of variables that negative for the th objects and positive for the th object
        int s = 0; // number of variables that negative for both objects
        int t = p + q + r + s; // total number of variables
        double jaccardDistance = (double) (q + r) / (double) (p + q + r);
        double jaccardCoef = (double) p / (double) (p + q + r);

        SparseDoubleVector vecA = SparseDoubleVector.from(objectA);
        SparseDoubleVector vecB = SparseDoubleVector.from(objectB);

        Jaccard instance = new Jaccard();
        double result = instance.combine(instance.shared(vecA, vecB),
                                         instance.left(vecA),
                                         instance.left(vecB));

        assertEquals(jaccardCoef, result, 0.0001);
    }

    /**
     * http://people.revoledu.com/kardi/tutorial/Similarity/Jaccard.html
     */
    @Test
    public void testJaccardExample2() {
        System.out.println("Testing Jaccard example2");
        Set<Integer> A = new HashSet<Integer>();
        A.addAll(Arrays.asList(7, 3, 2, 4, 1));

        Set<Integer> B = new HashSet<Integer>();
        B.addAll(Arrays.asList(4, 1, 9, 7, 5));

        Set<Integer> union = new HashSet<Integer>();
        union.addAll(A);
        union.addAll(B);

        Set<Integer> intersection = new HashSet<Integer>();
        intersection.addAll(A);
        intersection.retainAll(B);

        double jaccardCoef = (double) intersection.size() / (double) union.size();


        SparseDoubleVector vecA = new SparseDoubleVector(10);
        for (int a : A) {
            vecA.set(a, 1);
        }
        SparseDoubleVector vecB = new SparseDoubleVector(10);
        for (int b : B) {
            vecB.set(b, 1);
        }

        Jaccard instance = new Jaccard();
        double result = instance.combine(instance.shared(vecA, vecB),
                                         instance.left(vecA),
                                         instance.left(vecB));

        assertEquals(jaccardCoef, result, 0.0001);
    }

    /**
     * Test of shared method, of class Jaccard.
     */
    @Test
    public void testShared() {
        System.out.println("Testing shared()");
        SparseDoubleVector Q = SparseDoubleVector.from(
                new double[]{0, 1, 0, 1, 0, 1, 0, 1, 1, 1});
        SparseDoubleVector R = SparseDoubleVector.from(
                new double[]{1, 0, 1, 0, 1, 0, 1, 1, 1, 0});
        double expResult = 2.0;
        Jaccard instance = new Jaccard();
        double result = instance.shared(Q, R);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of left method, of class Jaccard.
     */
    @Test
    public void testLeft() {
        System.out.println("Testing left()");
        SparseDoubleVector Q = SparseDoubleVector.from(
                new double[]{0, 1, 0, 1, 0, 1, 0, 1, 1, 1});
        double expResult = 6.0;
        Jaccard instance = new Jaccard();
        double result = instance.left(Q);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of right method, of class Jaccard.
     */
    @Test
    public void testRight() {
        System.out.println("Testing right()");
        SparseDoubleVector R = SparseDoubleVector.from(
                new double[]{0, 1, 0, 1, 0, 1, 0, 1, 1, 1});
        double expResult = 6.0;
        Jaccard instance = new Jaccard();
        double result = instance.right(R);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of combine method, of class Jaccard.
     */
    @Test
    public void testCombine() {
        System.out.println("Testing combine()");
        double shared = 7.0;
        double left = 5.0;
        double right = 3.0;
        double expResult = shared / (left + right - shared);
        Jaccard instance = new Jaccard();
        double result = instance.combine(shared, left, right);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of isSymmetric method, of class Jaccard.
     */
    @Test
    public void testIsSymmetric() {
        System.out.println("Testing isSymmetric()");
        boolean expResult = true;
        Jaccard instance = new Jaccard();
        boolean result = instance.isCommutative();
        assertEquals(expResult, result);
    }
}
