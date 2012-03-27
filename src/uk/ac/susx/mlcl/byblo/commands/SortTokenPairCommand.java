/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class SortTokenPairCommand extends AbstractSortCommand<TokenPair> {

    private static final Log LOG = LogFactory.getLog(SortWeightedTokenCommand.class);

    @ParametersDelegate
    protected IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public SortTokenPairCommand(File sourceFile, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
        super(sourceFile, destinationFile, charset, TokenPair.indexOrder());
        indexDeligate.setPreindexedTokens1(preindexedTokens1);
        indexDeligate.setPreindexedTokens2(preindexedTokens2);
    }

    public SortTokenPairCommand() {
    }

    @Override
    protected Source<TokenPair> openSource(File file) throws FileNotFoundException, IOException {
        return new TokenPairSource(new TSVSource(file, getFilesDeligate().getCharset()),
                                   indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws FileNotFoundException, IOException {
        return new TokenPairSink(new TSVSink(file, getFilesDeligate().getCharset()),
                                 indexDeligate.getEncoder1(), indexDeligate.getEncoder2());
    }

}