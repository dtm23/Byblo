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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.enumerators;

import java.io.File;
import java.io.IOException;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author hiam20
 */
public class EnumeratingDeligates {

    private EnumeratingDeligates() {
    }

    public static SingleEnumerating toSingleEntries(final DoubleEnumerating outer) {
        return new PairToEntriesSingleAdapter(outer);
    }

    public static SingleEnumerating toSingleFeatures(final DoubleEnumerating outer) {
        return new PairToFeaturesSingleAdapter(outer);
    }

    public static DoubleEnumerating toPair(final SingleEnumerating inner) {
        return new SingleToPairAdapter(inner);
    }
//
//    public static Enumerator<String> instantiateEnumerator(
//            final boolean enumerated, final File indexFile)
//            throws IOException {
//        if (enumerated) {
//            if (indexFile != null && indexFile.exists())
//                return Enumerators.loadStringEnumerator(indexFile);
//            else
//                return Enumerators.nullEnumerator();
//        } else {
//            if (indexFile != null && indexFile.exists()) {
//                return Enumerators.loadStringEnumerator(indexFile);
//            } else {
//                return Enumerators.newDefaultStringEnumerator();
//            }
//        }
//    }
//
//    public static Enumerator<String> loadEnumerator(File file) {
//
//        return null;
//    }
//
//    public static Enumerator<String> newEnumerator(File file) {
////        return Enumerators.loadStringEnumerator(File file);
//        return null;
//    }
//
//    public static void saveEnumerator(File file) {
////        return Enumerators.loadStringEnumerator(File file);
//    }

    public static DoubleEnumerating decorateEnumerated(
            final DoubleEnumerating inner, final boolean enumerated) {
        return new EnumeratingDeligates.DoubleToDoubleAdapter(inner) {

            @Override
            public boolean isEntriesEnumerated() {
                return enumerated;
            }

            @Override
            public boolean isFeaturesEnumerated() {
                return enumerated;
            }

        };
    }

    public static SingleEnumerating decorateEnumerated(
            final SingleEnumerating inner, final boolean enumerated) {
        return new EnumeratingDeligates.SingleToSingleAdapter(inner) {

            @Override
            public boolean isEnumerationEnabled() {
                return enumerated;
            }

        };
    }

    public abstract static class AdapterBase<T extends Enumerating>
            implements Enumerating {

        private final T inner;

        public AdapterBase(final T inner) {
            Checks.checkNotNull("inner", inner);
            this.inner = inner;
        }

        protected final T getInner() {
            return inner;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final AdapterBase<T> other = (AdapterBase<T>) obj;
            if (this.inner != other.inner && (this.inner == null || !this.inner.equals(other.inner)))
                return false;
            return true;
        }

        @Override
        public boolean isEnumeratorSkipIndexed1() {
            return getInner().isEnumeratorSkipIndexed1();
        }

        @Override
        public boolean isEnumeratorSkipIndexed2() {
            return getInner().isEnumeratorSkipIndexed2();
        }

        @Override
        public void closeEnumerator() throws IOException {
            getInner().closeEnumerator();
        }

        @Override
        public void saveEnumerator() throws IOException {
            getInner().saveEnumerator();
        }

        @Override
        public void openEnumerator() throws IOException {
            getInner().openEnumerator();
        }

        @Override
        public int hashCode() {
            return 59 * 5 + (this.inner != null ? this.inner.hashCode() : 0);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" + "inner=" + inner + '}';
        }

    }

    public static class SingleToPairAdapter
            extends AdapterBase<SingleEnumerating>
            implements DoubleEnumerating {

        public SingleToPairAdapter(SingleEnumerating inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEntryEnumerator() throws IOException {
            return getInner().getEnumerator();
        }

        @Override
        public Enumerator<String> getFeatureEnumerator() throws IOException {
            return getInner().getEnumerator();
        }

        @Override
        public File getEntryEnumeratorFile() {
            return getInner().getEnumeratorFile();
        }

        @Override
        public File getFeatureEnumeratorFile() {
            return getInner().getEnumeratorFile();
        }

        @Override
        public boolean isEntriesEnumerated() {
            return getInner().isEnumerationEnabled();
        }

        @Override
        public boolean isFeaturesEnumerated() {
            return getInner().isEnumerationEnabled();
        }

        @Override
        public void openEntriesEnumerator() throws IOException {
            getInner().openEnumerator();
        }

        @Override
        public void openFeaturesEnumerator() throws IOException {
            getInner().openEnumerator();
        }

        @Override
        public void saveEntriesEnumerator() throws IOException {
            getInner().saveEnumerator();
        }

        @Override
        public void saveFeaturesEnumerator() throws IOException {
            getInner().saveEnumerator();
        }

        @Override
        public void closeEntriesEnumerator() throws IOException {
            getInner().closeEnumerator();
        }

        @Override
        public void closeFeaturesEnumerator() throws IOException {
            getInner().closeEnumerator();
        }

        @Override
        public SingleEnumerating getEntriesEnumeratorCarriar() {
            return getInner();
        }

        @Override
        public SingleEnumerating getFeaturesEnumeratorCarriar() {
            return getInner();
        }

    }

    public static class PairToFeaturesSingleAdapter
            extends AdapterBase<DoubleEnumerating>
            implements SingleEnumerating {

        public PairToFeaturesSingleAdapter(DoubleEnumerating inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator() throws IOException {
            return getInner().getFeatureEnumerator();
        }

        @Override
        public File getEnumeratorFile() {
            return getInner().getFeatureEnumeratorFile();
        }

        @Override
        public boolean isEnumerationEnabled() {
            return getInner().isFeaturesEnumerated();
        }

        @Override
        public void openEnumerator() throws IOException {
            getInner().openFeaturesEnumerator();
        }

        @Override
        public void saveEnumerator() throws IOException {
            getInner().saveFeaturesEnumerator();
        }

        @Override
        public void closeEnumerator() throws IOException {
            getInner().closeFeaturesEnumerator();
        }

        @Override
        public DoubleEnumerating getEnumeratorPairCarriar() {
            return getInner();
        }

    }

    public static class PairToEntriesSingleAdapter
            extends AdapterBase<DoubleEnumerating>
            implements SingleEnumerating {

        public PairToEntriesSingleAdapter(final DoubleEnumerating inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator() throws IOException {
            return getInner().getEntryEnumerator();
        }

        @Override
        public File getEnumeratorFile() {
            return getInner().getEntryEnumeratorFile();
        }

        @Override
        public boolean isEnumerationEnabled() {
            return getInner().isEntriesEnumerated();
        }

        @Override
        public void openEnumerator() throws IOException {
            getInner().openEntriesEnumerator();
        }

        @Override
        public void saveEnumerator() throws IOException {
            getInner().saveEntriesEnumerator();
        }

        @Override
        public void closeEnumerator() throws IOException {
            getInner().closeEntriesEnumerator();
        }

        @Override
        public DoubleEnumerating getEnumeratorPairCarriar() {
            return getInner();
        }

    }

    public abstract static class SingleToSingleAdapter
            extends AdapterBase<SingleEnumerating>
            implements SingleEnumerating {

        public SingleToSingleAdapter(final SingleEnumerating inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator() throws IOException {
            return getInner().getEnumerator();
        }

        @Override
        public File getEnumeratorFile() {
            return getInner().getEnumeratorFile();
        }

        @Override
        public boolean isEnumerationEnabled() {
            return getInner().isEnumerationEnabled();
        }

        @Override
        public void openEnumerator() throws IOException {
            getInner().openEnumerator();
        }

        @Override
        public void saveEnumerator() throws IOException {
            getInner().saveEnumerator();
        }

        @Override
        public void closeEnumerator() throws IOException {
            getInner().closeEnumerator();
        }

        @Override
        public DoubleEnumerating getEnumeratorPairCarriar() {
            return toPair(this);
        }

    }

    public abstract static class DoubleToDoubleAdapter
            extends AdapterBase<DoubleEnumerating>
            implements DoubleEnumerating {

        public DoubleToDoubleAdapter(final DoubleEnumerating inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEntryEnumerator() throws IOException {
            return getInner().getEntryEnumerator();
        }

        @Override
        public Enumerator<String> getFeatureEnumerator() throws IOException {
            return getInner().getFeatureEnumerator();
        }

        @Override
        public File getEntryEnumeratorFile() {
            return getInner().getEntryEnumeratorFile();
        }

        @Override
        public File getFeatureEnumeratorFile() {
            return getInner().getFeatureEnumeratorFile();
        }

        @Override
        public boolean isEntriesEnumerated() {
            return getInner().isEntriesEnumerated();
        }

        @Override
        public boolean isFeaturesEnumerated() {
            return getInner().isFeaturesEnumerated();
        }

        @Override
        public void openEntriesEnumerator() throws IOException {
            getInner().openEntriesEnumerator();
        }

        @Override
        public void openFeaturesEnumerator() throws IOException {
            getInner().openFeaturesEnumerator();
        }

        @Override
        public void saveEntriesEnumerator() throws IOException {
            getInner().saveEntriesEnumerator();
        }

        @Override
        public void saveFeaturesEnumerator() throws IOException {
            getInner().saveFeaturesEnumerator();
        }

        @Override
        public void closeEntriesEnumerator() throws IOException {
            getInner().closeEntriesEnumerator();
        }

        @Override
        public void closeFeaturesEnumerator() throws IOException {
            getInner().closeFeaturesEnumerator();
        }

        @Override
        public SingleEnumerating getEntriesEnumeratorCarriar() {
            return toSingleEntries(this);
        }

        @Override
        public SingleEnumerating getFeaturesEnumeratorCarriar() {
            return toSingleFeatures(this);
        }

    }
}