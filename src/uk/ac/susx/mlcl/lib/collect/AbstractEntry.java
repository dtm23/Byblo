/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.lib.collect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * Immutable
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 30th August 2010
 */
public abstract class AbstractEntry implements Serializable, Comparable<AbstractEntry> {

    private static final long serialVersionUID = 1L;

    private final int key;

    /**
     * Cloning constructor.
     *
     * @param other IndexEntry to clone.
     */
    protected AbstractEntry(final AbstractEntry other) {
        if (other == null)
            throw new NullPointerException("other == null");
        if (other.key < 0)
            throw new IllegalArgumentException("other.key < 0");

        this.key = other.key;
    }

    /**
     * Full dependancy constructor.
     *
     * @param key    the unique integer id of this entry
     */
    public AbstractEntry(final int key) {
        if (key < 0)
            throw new IllegalArgumentException("key < 0");

        this.key = key;
    }

    public final int key() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return this.key == ((AbstractEntry) obj).key;
    }

    @Override
    public int hashCode() {
        return 29 * (29 * 3 + this.key);
    }

    @Override
    public String toString() {
        return Integer.toString(key);
    }

    @Override
    public int compareTo(AbstractEntry that) {
        return KEY_ASC.compare(this, that);
    }

    protected abstract Object writeReplace();

    protected abstract static class Serializer implements Externalizable {

        private static final long serialVersionUID = 1;

        protected int key;

        public Serializer() {
        }

        public Serializer(final AbstractEntry entry) {
            this.key = entry.key;
        }

        @Override
        public void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(key);
        }

        @Override
        public void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            key = in.readInt();
        }

        protected abstract Object readResolve();
    }

    public static final Comparator<AbstractEntry> KEY_ASC =
            new Comparator<AbstractEntry>() {

                @Override
                public final int compare(final AbstractEntry a,
                                         final AbstractEntry b) {
                    return a.key < b.key ? -1 : a.key > b.key ? 1 : 0;
                }
            };

    public static final Comparator<AbstractEntry> KEY_DESC =
            new Comparator<AbstractEntry>() {

                @Override
                public final int compare(final AbstractEntry a,
                                         final AbstractEntry b) {
                    return a.key < b.key ? 1 : a.key > b.key ? -1 : 0;
                }
            };

}
