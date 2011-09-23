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
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;
import uk.ac.susx.mlcl.lib.tasks.CaseInsensitiveComparator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Merges the contents of two sorted source files, line by line, into a
 * destination file.
 *
 * The source files are assumed to already be ordered according to the
 * comparator.
 * 
 * Any file denoted by the name string "-" is assumed to be standard-in in the
 * case of source files, and standard out in the case of destination files..
 *
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "USAGE_MERGE_COMMAND")
public class MergeTask extends AbstractTask {

    private static final Logger LOG = Logger.getLogger(MergeTask.class.getName());

    private Comparator<String> comparator;

    private Formatter formatter = new DefaultFormatter();

    @Parameter(names = {"-ifa", "--input-file-a"}, required = true,
               descriptionKey = "USAGE_INPUT_FILE_A")
    private File sourceFileA;

    @Parameter(names = {"-ifb", "--input-file-b"}, required = true,
               descriptionKey = "USAGE_INPUT_FILE_B")
    private File sourceFileB;

    @Parameter(names = {"-of", "--output-file"},
               descriptionKey = "USAGE_OUTPUT_FILE")
    private File destinationFile;

    @Parameter(names = {"--charset"},
               descriptionKey = "USAGE_CHARSET")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public MergeTask(File sourceFileA, File sourceFileB, File destination,
            Charset charset) {
        setSourceFileA(sourceFileA);
        setSourceFileB(sourceFileB);
        setDestinationFile(destination);
        setCharset(charset);
        comparator = new CaseInsensitiveComparator<String>();
    }

    public MergeTask() {
        comparator = new CaseInsensitiveComparator<String>();
    }

    @Override
    protected void initialiseTask() throws Exception {
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    public interface Formatter {

        public void write(BufferedWriter writer, String... strings) throws IOException;
    }

    public class DefaultFormatter implements Formatter {

        @Override
        public void write(BufferedWriter writer, String... strings)
                throws IOException {
            for (String str : strings) {
                writer.write(str);
                writer.newLine();
            }
        }
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter == null)
            throw new NullPointerException("formatter is null");
        this.formatter = formatter;
    }

    @Override
    protected void runTask() throws Exception {

        BufferedReader readerA = null;
        BufferedReader readerB = null;
        BufferedWriter writer = null;

        try {
            LOG.log(Level.INFO,
                    "Merging from files \"{0}\" and \"{1}\" to \"{2}\". ({3})",
                    new Object[]{getSourceFileA(), getSourceFileB(),
                        getDestFile(),
                        Thread.currentThread().getName()});

            readerA = IOUtil.openReader(getSourceFileA(), getCharset());
            readerB = IOUtil.openReader(getSourceFileB(), getCharset());
            writer = IOUtil.openWriter(getDestFile(), getCharset());
            String lineA = readerA.readLine();
            String lineB = readerB.readLine();
            while (lineA != null && lineB != null) {
                int comp = comparator.compare(lineA, lineB);
                if (comp < 0) {
                    formatter.write(writer, lineA);
                    lineA = readerA.readLine();
                } else if (comp > 0) {
                    formatter.write(writer, lineB);
                    lineB = readerB.readLine();
                } else {
                    formatter.write(writer, lineA, lineB);
                    lineA = readerA.readLine();
                    lineB = readerB.readLine();
                }
            }
            while (lineA != null) {
                formatter.write(writer, lineA);
                lineA = readerA.readLine();
            }
            while (lineB != null) {
                formatter.write(writer, lineB);
                lineB = readerB.readLine();
            }
        } finally {
            if (readerA != null)
                readerA.close();
            if (readerB != null)
                readerB.close();
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    public File getSourceFileA() {
        return sourceFileA;
    }

    public File getSourceFileB() {
        return sourceFileB;
    }

    public File getDestFile() {
        return destinationFile;
    }

    public Comparator<String> getComparator() {
        return comparator;
    }

    public final void setSourceFileB(File sourceFileB) {
        if (sourceFileB == null)
            throw new NullPointerException("sourceFileB = null");
        if (sourceFileB == sourceFileA)
            throw new IllegalArgumentException("sourceFileB == sourceFileA");
        if (destinationFile == sourceFileB)
            throw new IllegalArgumentException("destination == sourceFileB");
        this.sourceFileB = sourceFileB;
    }

    public final void setSourceFileA(File sourceFileA) {
        if (sourceFileA == null)
            throw new NullPointerException("sourceFileA = null");
        if (sourceFileA == sourceFileB)
            throw new IllegalArgumentException("sourceFileA == sourceFileB");
        if (destinationFile == sourceFileA)
            throw new IllegalArgumentException("destination == sourceFileA");
        this.sourceFileA = sourceFileA;
    }

    public final void setDestinationFile(File destination) {
        if (destination == null)
            throw new NullPointerException("destination = null");
        if (destination == sourceFileB)
            throw new IllegalArgumentException("destination == sourceFileB");
        if (destination == sourceFileA)
            throw new IllegalArgumentException("destination == sourceFileA");
        this.destinationFile = destination;
    }

    public final void setComparator(Comparator<String> comparator) {
        if (comparator == null)
            throw new NullPointerException("comparator = null");
        this.comparator = comparator;
    }
}
