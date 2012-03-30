/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligate;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.WeightSumReducerSink;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class SortWeightedTokenCommand extends AbstractSortCommand<Weighted<Token>> {

    private static final Log LOG = LogFactory.getLog(SortWeightedTokenCommand.class);

    @ParametersDelegate
    private IndexDeligate indexDeligate = new IndexDeligate();

    public SortWeightedTokenCommand(File sourceFile, File destinationFile, Charset charset, IndexDeligate indexDeligate) {
        super(sourceFile, destinationFile, charset, Weighted.recordOrder(Token.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public SortWeightedTokenCommand() {
    }

    @Override
    protected Source<Weighted<Token>> openSource(File file) throws FileNotFoundException, IOException {
        return new WeightedTokenSource(
                new TSVSource(file, getFilesDeligate().getCharset()),
                indexDeligate);
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws FileNotFoundException, IOException {
        WeightedTokenSink s = new WeightedTokenSink(
                new TSVSink(file, getFilesDeligate().getCharset()), indexDeligate);
        s.setCompactFormatEnabled(!getFilesDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<Token>(s);

    }

    public final IndexDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(IndexDeligate indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }
    
    

}
