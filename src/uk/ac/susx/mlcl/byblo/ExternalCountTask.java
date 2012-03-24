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
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.tasks.InputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.OutputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.Task;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Freqency count a structured input instance file.")
public class ExternalCountTask extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(ExternalCountTask.class);

    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines per work unit. Lrger value increase performance and memory usage.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-i", "--input"},
    required = true,
    description = "Input instances file",
    validateWith = InputFileValidator.class)
    private File inputFile;

    @Parameter(names = {"-oef", "--output-entry-features"},
    required = true,
    description = "Output entry-feature frequencies file",
    validateWith = OutputFileValidator.class)
    private File entryFeaturesFile = null;

    @Parameter(names = {"-oe", "--output-entries"},
    required = true,
    description = "Output entry frequencies file",
    validateWith = OutputFileValidator.class)
    private File entriesFile = null;

    @Parameter(names = {"-of", "--output-features"},
    required = true,
    description = "Output feature frequencies file",
    validateWith = OutputFileValidator.class)
    private File featuresFile = null;

    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory used for holding temporary files.")
    private TempFileFactory tempFileFactory = new TempFileFactory();

    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding to use for reading and writing files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @Parameter(names = {"-pe", "--preindexed-entries"},
    description = "Whether entries in the input events file are already indexed.")
    private boolean preindexedEntries = false;

    @Parameter(names = {"-pf", "--preindexed-features"},
    description = "Whether features in the input events file are already indexed.")
    private boolean preindexedFeatures = false;

    private Queue<File> mergeEntryQueue;

    private Queue<File> mergeFeaturesQueue;

    private Queue<File> mergeEntryFeatureQueue;

    public ExternalCountTask(final File instancesFile, final File featuresFile,
                             final File entriesFile, final File contextsFile, Charset charset,
                             int maxChunkSize) {
        this(instancesFile, featuresFile, entriesFile, contextsFile);
        setCharset(charset);
        setMaxChunkSize(maxChunkSize);
    }

    public ExternalCountTask(
            final File instancesFile, final File entryFeaturesFile,
            final File entriesFile, final File featuresFile) {
        setInstancesFile(instancesFile);
        setEntryFeaturesFile(entryFeaturesFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
    }

    public ExternalCountTask() {
        super();
    }

    public boolean isPreindexedEntries() {
        return preindexedEntries;
    }

    public void setPreindexedEntries(boolean preindexedEntries) {
        this.preindexedEntries = preindexedEntries;
    }

    public boolean isPreindexedFeatures() {
        return preindexedFeatures;
    }

    public void setPreindexedFeatures(boolean preindexedFeatures) {
        this.preindexedFeatures = preindexedFeatures;
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public final int getMaxChunkSize() {
        return maxChunkSize;
    }

    public final void setMaxChunkSize(int maxChunkSize) {
        if (maxChunkSize < 1)
            throw new IllegalArgumentException("maxChunkSize < 1");
        this.maxChunkSize = maxChunkSize;
    }

    public final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(final File contextsFile)
            throws NullPointerException {
        if (contextsFile == null)
            throw new NullPointerException("contextsFile is null");
        this.featuresFile = contextsFile;
    }

    public final File getEntryFeaturesFile() {
        return entryFeaturesFile;
    }

    public final void setEntryFeaturesFile(final File featuresFile)
            throws NullPointerException {
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        this.entryFeaturesFile = featuresFile;
    }

    public final File getEntriesFile() {
        return entriesFile;
    }

    public final void setEntriesFile(final File entriesFile)
            throws NullPointerException {
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");
        this.entriesFile = entriesFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public final void setInstancesFile(final File inputFile)
            throws NullPointerException {
        if (inputFile == null)
            throw new NullPointerException("sourceFile is null");
        this.inputFile = inputFile;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        checkState();
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running external count on \"" + inputFile + "\".");

        map();
        reduce();
        finish();

        if (LOG.isInfoEnabled())
            LOG.info("Completed external count");

    }

    protected void map() throws Exception {

        mergeEntryQueue = new ArrayDeque<File>();
        mergeFeaturesQueue = new ArrayDeque<File>();
        mergeEntryFeatureQueue = new ArrayDeque<File>();

        BlockingQueue<File> chunkQueue = new ArrayBlockingQueue<File>(2);

        ChunkTask chunkTask = new ChunkTask(getInputFile(), getCharset(),
                                            getMaxChunkSize());
        chunkTask.setDstFileQueue(chunkQueue);
        chunkTask.setChunkFileFactory(tempFileFactory);
        Future<ChunkTask> chunkFuture = submitTask(chunkTask);

        // Immidiately poll the chunk task so we can start handling other
        // completed tasks
        if (!getFutureQueue().poll().equals(chunkFuture))
            throw new AssertionError("Expecting ChunkTask on future queue.");

        while (!chunkFuture.isDone() || !chunkQueue.isEmpty()) {
            if (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {

                handleCompletedTask(getFutureQueue().poll().get());

            } else if (!chunkQueue.isEmpty()) {

                File chunk = chunkQueue.take();

                File chunk_entriesFile = tempFileFactory.createFile();
                File chunk_featuresFile = tempFileFactory.createFile();
                File chunk_entryFeaturesFile = tempFileFactory.createFile();

                CountTask ct = new CountTask(chunk, chunk_entryFeaturesFile,
                                             chunk_entriesFile, chunk_featuresFile, getCharset());
                ct.setPreindexedEntries(this.isPreindexedEntries());
                ct.setPreindexedFeatures(this.isPreindexedFeatures());

                submitTask(ct);
            }

            // XXX: Nasty hack to stop it tight looping when both queues are empty
            Thread.sleep(1);
        }
        chunkTask.throwException();
    }

    protected void reduce() throws Exception {
        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }
    }

    protected void handleCompletedTask(Task task) throws Exception {
        task.throwException();

        if (task instanceof CountTask) {

            CountTask countTask = (CountTask) task;

            SortTask.EntryFreqsSortTask sortEntTask = new SortTask.EntryFreqsSortTask(
                    countTask.getEntriesFile(),
                    countTask.getEntriesFile(),
                    countTask.getCharset(),
                    countTask.isPreindexedEntries());
            sortEntTask.setComparator(Weighted.recordOrder(Token.INDEX_ORDER));
            submitTask(sortEntTask);

            SortTask.EventFreqsSortTask sortEventTask = new SortTask.EventFreqsSortTask(
                    countTask.getEntryFeaturesFile(),
                    countTask.getEntryFeaturesFile(),
                    countTask.getCharset(),
                    countTask.isPreindexedEntries(),
                    countTask.isPreindexedFeatures());
            sortEventTask.setComparator(Weighted.recordOrder(TokenPair.INDEX_ORDER));
            submitTask(sortEventTask);

            SortTask.FeatureFreqsSortTask sortFeatTask = new SortTask.FeatureFreqsSortTask(
                    countTask.getFeaturesFile(),
                    countTask.getFeaturesFile(),
                    countTask.getCharset(),
                    countTask.isPreindexedFeatures());
            sortFeatTask.setComparator(Weighted.recordOrder(Token.INDEX_ORDER));
            submitTask(sortFeatTask);

            submitTask(new DeleteTask(countTask.getInputFile()));

        } else if (task instanceof SortTask) {


            final SortTask sortTask = (SortTask) task;
            final File dst = sortTask.getDstFile();

            if (task instanceof SortTask.EntryFreqsSortTask) {
                submitMergeEntriesTask(dst);
            } else if (task instanceof SortTask.FeatureFreqsSortTask) {
                submitMergeFeaturesTask(dst);
            } else if (task instanceof SortTask.EventFreqsSortTask) {
                submitMergeEventsTask(dst);
            } else {
                throw new AssertionError("Task does not match known merge types: " + task);
            }



        } else if (task.getClass().equals(MergeTask.class)) {
            MergeTask completedMergeTask = (MergeTask) task;
            File dst = completedMergeTask.getDestFile();

            if (task instanceof SortTask.EntryFreqsSortTask) {
                submitMergeEntriesTask(dst);
            } else if (task instanceof SortTask.FeatureFreqsSortTask) {
                submitMergeFeaturesTask(dst);
            } else if (task instanceof SortTask.EventFreqsSortTask) {
                submitMergeEventsTask(dst);
            } else {
                throw new AssertionError("Task does not match known merge types: " + task);
            }

            submitTask(new DeleteTask(completedMergeTask.getSourceFileA()));
            submitTask(new DeleteTask(completedMergeTask.getSourceFileB()));

        } else if (task instanceof DeleteTask) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass() + " should not have been queued.");
        }
    }

    protected void finish() throws Exception {
        checkState();

        File finalMerge;

        finalMerge = mergeEntryQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry merge queue is empty but final copy has not been completed.");
        new CopyTask(finalMerge, getEntriesFile()).runTask();
        new DeleteTask(finalMerge).runTask();

        finalMerge = mergeEntryFeatureQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry/feature merge queue is empty but final copy has not been completed.");
        new CopyTask(finalMerge, getEntryFeaturesFile()).runTask();
        new DeleteTask(finalMerge).runTask();

        finalMerge = mergeFeaturesQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The feature merge queue is empty but final copy has not been completed.");
        new CopyTask(finalMerge, getFeaturesFile()).runTask();
        new DeleteTask(finalMerge).runTask();
    }

    private void submitMergeEntriesTask(File dst) throws IOException {
        mergeEntryQueue.add(dst);
        if (mergeEntryQueue.size() >= 2) {
            File out = tempFileFactory.createFile();
            MergeTask.EntryFreqsMergeTask mergeTask = new MergeTask.EntryFreqsMergeTask(
                    mergeEntryQueue.poll(), mergeEntryQueue.poll(), out,
                    getCharset(), isPreindexedEntries());
            mergeTask.setComparator(Weighted.recordOrder(Token.INDEX_ORDER));
            mergeTask.setReducer(new MergeTask.WeightSumReducer<Token>());
            submitTask(mergeTask);
        }
    }

    private void submitMergeFeaturesTask(File dst) throws IOException {
        mergeFeaturesQueue.add(dst);
        if (mergeFeaturesQueue.size() >= 2) {
            File out = tempFileFactory.createFile();
            MergeTask.FeatureFreqsMergeTask mergeTask = new MergeTask.FeatureFreqsMergeTask(
                    mergeFeaturesQueue.poll(), mergeFeaturesQueue.poll(), out,
                    getCharset(), isPreindexedFeatures());
            mergeTask.setComparator(Weighted.recordOrder(Token.INDEX_ORDER));
            mergeTask.setReducer(new MergeTask.WeightSumReducer<Token>());
            submitTask(mergeTask);
        }
    }

    private void submitMergeEventsTask(File dst) throws IOException {
        mergeFeaturesQueue.add(dst);
        if (mergeFeaturesQueue.size() >= 2) {
            File out = tempFileFactory.createFile();
            MergeTask.EventFreqsMergeTask mergeTask = new MergeTask.EventFreqsMergeTask(
                    mergeEntryFeatureQueue.poll(), mergeEntryFeatureQueue.poll(), out,
                    getCharset(), isPreindexedEntries(), isPreindexedFeatures());
            mergeTask.setComparator(Weighted.recordOrder(TokenPair.INDEX_ORDER));
            mergeTask.setReducer(new MergeTask.WeightSumReducer<TokenPair>());
            submitTask(mergeTask);
        }
    }

    /**
     * Method that performance a number of sanity checks on the parameterisation
     * of this class. It is necessary to do this because the the class can be
     * instantiated via a null constructor when run from the command line.
     *
     * @throws NullPointerException
     * @throws IllegalStateException
     * @throws FileNotFoundException
     */
    private void checkState() throws NullPointerException, IllegalStateException, FileNotFoundException {
        // Check non of the parameters are null
        if (inputFile == null)
            throw new NullPointerException("inputFile is null");
        if (entryFeaturesFile == null)
            throw new NullPointerException("entryFeaturesFile is null");
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");
        if (charset == null)
            throw new NullPointerException("charset is null");

        // Check that no two files are the same
        if (inputFile.equals(entryFeaturesFile))
            throw new IllegalStateException("inputFile == featuresFile");
        if (inputFile.equals(featuresFile))
            throw new IllegalStateException("inputFile == contextsFile");
        if (inputFile.equals(entriesFile))
            throw new IllegalStateException("inputFile == entriesFile");
        if (entryFeaturesFile.equals(featuresFile))
            throw new IllegalStateException("entryFeaturesFile == featuresFile");
        if (entryFeaturesFile.equals(entriesFile))
            throw new IllegalStateException("entryFeaturesFile == entriesFile");
        if (featuresFile.equals(entriesFile))
            throw new IllegalStateException("featuresFile == entriesFile");


        // Check that the instances file exists and is readable
        if (!inputFile.exists())
            throw new FileNotFoundException(
                    "instances file does not exist: " + inputFile);
        if (!inputFile.isFile())
            throw new IllegalStateException(
                    "instances file is not a normal data file: " + inputFile);
        if (!inputFile.canRead())
            throw new IllegalStateException(
                    "instances file is not readable: " + inputFile);

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        if (entriesFile.exists() && (!entriesFile.isFile() || !entriesFile.canWrite()))
            throw new IllegalStateException(
                    "entries file exists but is not writable: " + entriesFile);
        if (!entriesFile.exists() && !entriesFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entries file does not exists and can not be reated: " + entriesFile);
        }
        if (featuresFile.exists() && (!featuresFile.isFile() || !featuresFile.canWrite()))
            throw new IllegalStateException(
                    "features file exists but is not writable: " + featuresFile);
        if (!featuresFile.exists() && !featuresFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "features file does not exists and can not be reated: " + featuresFile);
        }
        if (entryFeaturesFile.exists() && (!entryFeaturesFile.isFile() || !entryFeaturesFile.canWrite()))
            throw new IllegalStateException(
                    "entry-features file exists but is not writable: " + entryFeaturesFile);
        if (!entryFeaturesFile.exists() && !entryFeaturesFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entry-features file does not exists and can not be reated: " + entryFeaturesFile);
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", inputFile).
                add("entriesOut", entriesFile).
                add("featuresOut", featuresFile).
                add("eventsOut", entryFeaturesFile).
                add("tempDir", tempFileFactory).
                add("charset", charset);
    }

}
