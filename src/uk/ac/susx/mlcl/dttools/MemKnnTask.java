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
package uk.ac.susx.mlcl.dttools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters()
public class MemKnnTask extends MemSortTask {

    private static final Logger LOG =
            Logger.getLogger(MemKnnTask.class.getName());

    @Parameter(names = {"-k"}, descriptionKey = "USAGE_K")
    private int k = ExtKnnTask.DEFAULT_K;

    public MemKnnTask(File sourceFile, File destinationFile, Charset charset,
            Comparator<String> comparator, int k) {
        super(sourceFile, destinationFile, charset, comparator);
        this.k = k;
    }

    public final int getK() {
        return k;
    }

    public final void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("k < 1");
        this.k = k;
    }

    @Override
    protected void runTask() throws Exception {
        LOG.log(Level.INFO,
                "Running kNN in memory from \"{0}\" to \"{1}\". ({2})",
                new Object[]{getSrcFile(), getDstFile(), Thread.currentThread().
                    getName()});
        final List<String> linesIn = new ArrayList<String>();
        IOUtil.readAllLines(getSrcFile(), getCharset(),linesIn);
        final List<String> linesOut = new ArrayList<String>();
        knnLines(linesIn, linesOut);
        IOUtil.writeAllLines(getDstFile(), getCharset(),linesOut);
    }

    protected void knnLines(Collection<? extends String> in,
            Collection<? super String> out) {
        String currentWord = null;
        int count = 0;
        for (String line : in) {
            String[] parts = line.split("\t");
            String word = parts[0];
            if (!word.equals(currentWord)) {
                currentWord = word;
                count = 1;
            } else {
                count++;
            }
            if (count <= k) {
                out.add(line);
            }
        }
    }
}
