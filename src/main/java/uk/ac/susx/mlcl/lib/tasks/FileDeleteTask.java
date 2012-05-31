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
package uk.ac.susx.mlcl.lib.tasks;

import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.text.MessageFormat.format;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Delete a file.")
public class FileDeleteTask extends AbstractTask
        implements ProgressReporting {

    private final ProgressDeligate progress = new ProgressDeligate(this, true);

    private File file = null;

    public FileDeleteTask(final File file) {
        setFile(file);
    }

    public FileDeleteTask() {
    }

    @Override
    public void runTask() throws Exception {

        progress.startAdjusting();
        progress.setMessage("Deleting file \"" + getFile() + "\".");
        progress.endAdjusting();

        Checks.checkNotNull("file", getFile());

        progress.startAdjusting();
        progress.setStarted();
        progress.setProgressPercent(0);
        progress.endAdjusting();

        if (!getFile().exists())
            throw new FileNotFoundException(format(
                    "Unnable to delete file because it doesn't exist: \"{0}\"",
                    getFile()));

        if (!getFile().delete())
            throw new IOException(
                    "Unnable to delete file: \"" + getFile() + "\"");

        progress.startAdjusting();
        progress.setCompleted();
        progress.setProgressPercent(100);
        progress.endAdjusting();

    }

    public final File getFile() {
        return file;
    }

    public final void setFile(final File file)
            throws NullPointerException {
        Checks.checkNotNull("file", file);
        this.file = file;
    }

    public void removeProgressListener(ProgressListener progressListener) {
        progress.removeProgressListener(progressListener);
    }

    public boolean isStarted() {
        return progress.isStarted();
    }

    public boolean isRunning() {
        return progress.isRunning();
    }

    public boolean isProgressPercentageSupported() {
        return progress.isProgressPercentageSupported();
    }

    public boolean isCompleted() {
        return progress.isCompleted();
    }

    public String getProgressReport() {
        return progress.getProgressReport();
    }

    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    public String getName() {
        return "delete";
    }

    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("file", file);
    }

}
