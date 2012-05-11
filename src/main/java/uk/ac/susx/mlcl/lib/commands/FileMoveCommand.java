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
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.File;
import uk.ac.susx.mlcl.lib.tasks.FileMoveTask;

/**
 * Move source file to a destination.
 *
 * Attempts to perform a fast rename if possible. Otherwise it falls back to
 * slower copy and delete.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Move a file.")
public class FileMoveCommand extends AbstractCommand {

    @ParametersDelegate
    protected final FilePipeDelegate filesDeligate = new FilePipeDelegate();

    public FileMoveCommand(File sourceFile, File destinationFile) {
        filesDeligate.setSourceFile(sourceFile);
        filesDeligate.setDestinationFile(destinationFile);
    }

    public FileMoveCommand() {
    }

    public final void setSourceFile(File sourceFile) {
        filesDeligate.setSourceFile(sourceFile);
    }

    public final void setDestinationFile(File destFile) {
        filesDeligate.setDestinationFile(destFile);
    }

    public final File getSourceFile() {
        return filesDeligate.getSourceFile();
    }

    public final File getDestinationFile() {
        return filesDeligate.getDestinationFile();
    }

    @Override
    public void runCommand() throws Exception {

        FileMoveTask task = new FileMoveTask(
                filesDeligate.getSourceFile(),
                filesDeligate.getDestinationFile());

        task.run();

        while (task.isExceptionTrapped())
            task.throwTrappedException();
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("from", filesDeligate.getSourceFile()).
                add("to", filesDeligate.getDestinationFile());
    }

}