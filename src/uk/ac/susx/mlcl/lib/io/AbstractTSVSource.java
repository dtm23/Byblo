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
package uk.ac.susx.mlcl.lib.io;

import com.google.common.base.CharMatcher;
import uk.ac.susx.mlcl.lib.io.Lexer.Type;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * Abstract class that holds functionality to read a Tab Separated Values file.
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 * @param <T>
 */
public abstract class AbstractTSVSource<T> 
        implements SeekableSource<T, Lexer.Tell> {

    private static final char RECORD_DELIM = '\n';
    private static final char VALUE_DELIM = '\t';
    private final Lexer lexer;
    private final File file;

    public AbstractTSVSource(File file, Charset charset) throws FileNotFoundException, IOException {
        if(file == null)
            throw new NullPointerException("file == null");
        if(!file.exists())
            throw new FileNotFoundException("Path " + file + " does not exist.");
        if(!file.isFile())
            throw new IllegalArgumentException("Path " + file + " is not a normal file.");
        if(!file.canRead())
            throw new IllegalArgumentException("File " + file + " is not readable.");

        lexer = new Lexer(file, charset);
        lexer.setDelimiterMatcher(CharMatcher.anyOf("\n\t"));
        lexer.setWhitespaceMatcher(CharMatcher.NONE);
        if (lexer.hasNext())
            lexer.advance();
        this.file = file;
    }

    @Override
    public abstract T read() throws IOException;

    @Override
    public void position(Lexer.Tell offset) throws IOException {
        lexer.seek(offset);
    }


    public double percentRead() throws IOException {
        return 100d * bytesRead() / bytesTotal();
    }

    public long bytesRead() {
        return lexer.bytesRead();
    }

    public long bytesTotal() throws IOException {
        return lexer.bytesTotal();
    }
    @Override
    public Lexer.Tell position() { 
        return lexer.tell();
    }

    @Override
    public boolean hasNext() throws CharacterCodingException, IOException {
        return lexer.hasNext();
    }

    protected void skipWhitespace() throws CharacterCodingException, IOException {
        while (lexer.type() == Type.Whitespace && lexer.hasNext()) {
            lexer.advance();
        }
    }

    protected void parseRecordDelimiter() throws CharacterCodingException, IOException {
        if(hasNext())
            parseDelimiter(RECORD_DELIM);
    }

    protected void parseValueDelimiter() throws CharacterCodingException, IOException {
        parseDelimiter(VALUE_DELIM);
    }

    protected String parseString() throws CharacterCodingException, IOException {
        skipWhitespace();
        expectType(Type.Value, lexer.type());
        final String str = lexer.value().toString();
        if (lexer.hasNext())
            lexer.advance();
        return str;
    }

    protected double parseDouble() throws CharacterCodingException, NumberFormatException, IOException {
        return Double.valueOf(parseString());
    }

    protected int parseInt() throws CharacterCodingException, NumberFormatException, IOException {
        return Integer.parseInt(parseString());
    }

    private void parseDelimiter(char delim) throws CharacterCodingException, IOException {
        skipWhitespace();
        expectType(Type.Delimiter, lexer.type());
        expectDelim(delim, lexer.charAt(0));
        if (lexer.hasNext())
            lexer.advance();
    }

    private  void expectType(Type expected, Type actual) {
        if (expected != actual) {
            throw new AssertionError(
                    "Expecting type " + expected + " but found " + actual);
        }
    }

    private void expectDelim(char expected, char actual) {
        if (expected != actual)
            throw new AssertionError("Expecting delimiter "
                    + (int) expected + " but found " + (int) actual
                    + " when parsing file " + file);
    }
}
