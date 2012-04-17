/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDeligate;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class SortWeightedTokenCommand extends AbstractSortCommand<Weighted<Token>> {

    private static final Log LOG = LogFactory.getLog(
            SortWeightedTokenCommand.class);

    @ParametersDelegate
    private SingleEnumerating indexDeligate = new SingleEnumeratingDeligate();

    public SortWeightedTokenCommand(File sourceFile, File destinationFile,
                                    Charset charset, SingleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset, Weighted.recordOrder(Token.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public SortWeightedTokenCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }

    @Override
    protected Source<Weighted<Token>> openSource(File file) throws FileNotFoundException, IOException {
        return WeightedTokenSource.open(file, getFilesDeligate().getCharset(),
                                        indexDeligate);
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws FileNotFoundException, IOException {
        WeightedTokenSink s = WeightedTokenSink.open(
                file, getFilesDeligate().getCharset(), indexDeligate);
        return new WeightSumReducerSink<Token>(s);

    }

    public final SingleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(SingleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
