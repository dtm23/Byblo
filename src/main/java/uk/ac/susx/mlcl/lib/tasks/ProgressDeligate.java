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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ProgressDeligate implements ProgressReporting, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(ProgressDeligate.class);

    private CopyOnWriteArrayList<ProgressListener> progressListeners =
            new CopyOnWriteArrayList<ProgressListener>();

    private final ProgressReporting outer;

    private final boolean progressPercentageSupported;

    private int progressPercent = 0;

    private ProgressEvent event = null;

    private String message = null;

    private enum State {

        PENDING, RUNNING, COMPLETED

    }

    private State state = State.PENDING;

    protected boolean stateChangedSinceLastEvent = false;

    private AtomicInteger adjustingCount = new AtomicInteger(0);

    public ProgressDeligate(ProgressReporting outer, boolean progressPercentageSupported) {
        this.outer = outer;
        this.progressPercentageSupported = progressPercentageSupported;
    }

    public ProgressDeligate(ProgressReporting outer) {
        this.outer = outer;
        this.progressPercentageSupported = true;
    }

    public ProgressDeligate(boolean progressPercentageSupported) {
        this.outer = null;
        this.progressPercentageSupported = progressPercentageSupported;
    }

    public ProgressDeligate() {
        this.outer = null;
        this.progressPercentageSupported = true;
    }

    public ProgressReporting getOuter() {
        return outer;
    }

    @Override
    public String getName() {
        if (outer == this || outer == null)
            return "<unamed deligate>";
        else
            return outer.getName();
    }

    public void startAdjusting() {
        assert adjustingCount.get() >= 0;

        adjustingCount.incrementAndGet();
    }

    public void endAdjusting() {
        assert adjustingCount.get() >= 1;

        if (adjustingCount.decrementAndGet() == 0) {
            fireProgressChangedEvent();
        }
    }

    @Override
    public int getProgressPercent() {
        if (!isProgressPercentageSupported())
            throw new UnsupportedOperationException(
                    "Progress percentage reporting is not supported.");
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        if (!isProgressPercentageSupported())
            throw new UnsupportedOperationException(
                    "Progress percentage reporting is not supported.");
        Checks.checkRangeIncl(progressPercent, 0, 100);
        if (progressPercent != this.progressPercent) {
            startAdjusting();
//            if (state != State.STARTED)
//                setStarted();
            this.progressPercent = progressPercent;
            stateChangedSinceLastEvent = true;
            endAdjusting();
        }
    }

    @Override
    public boolean isProgressPercentageSupported() {
        return progressPercentageSupported;
    }

    @Override
    public boolean isStarted() {
        return state == State.RUNNING || state == State.COMPLETED;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public void setStarted() {
        if (state == State.RUNNING)
            return;
        if (state != State.PENDING)
            throw new IllegalStateException(MessageFormat.format(
                    "Unable to transision from state {0} to state {1}",
                    state, State.RUNNING));
        startAdjusting();
        state = State.RUNNING;
        stateChangedSinceLastEvent = true;
        setMessage("Running");
        endAdjusting();
    }

    @Override
    public boolean isCompleted() {
        return state == State.COMPLETED;
    }

    public void setCompleted() {
        if (state == State.COMPLETED)
            return;
        if (state != State.RUNNING)
            throw new IllegalStateException(MessageFormat.format(
                    "Unable to transision from state {0} to state {1}",
                    state, State.COMPLETED));
        startAdjusting();
        state = State.COMPLETED;
        stateChangedSinceLastEvent = true;
        setMessage("Completed");
        endAdjusting();
    }

    public void setMessage(String message) {
        if (this.message != message && (this.message == null || !this.message.equals(message))) {
            startAdjusting();
            this.message = message;
            stateChangedSinceLastEvent = true;
            endAdjusting();
        }
    }

    @Override
    public String getProgressReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(": ");
        if (message != null) {
            sb.append(message);
            sb.append(' ');
        }
        sb.append("(");
        sb.append(isProgressPercentageSupported()
                  ? getProgressPercent() : "unknown");
        sb.append("% complete)");
        return sb.toString();
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        if (!progressListeners.contains(progressListener)) {
            progressListeners.add(progressListener);
        }
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        progressListeners.remove(progressListener);
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progressListeners.toArray(new ProgressListener[0]);
    }

    protected void fireProgressChangedEvent() {
        if (!stateChangedSinceLastEvent)
            return;
        stateChangedSinceLastEvent = false;
        if (progressListeners.isEmpty())
            return;
        if (event == null)
            event = new ProgressEvent(outer == null ? this : outer);
        for (ProgressListener progressListener : progressListeners) {
            try {
                progressListener.progressChanged(event);
            } catch (Exception ex) {
                LOG.error(MessageFormat.format(
                        "Progress listener {0} threw exception while handing event {1}",
                        progressListener, event), ex);
                removeProgressListener(progressListener);
            }
        }
    }

    public boolean equals(ProgressDeligate other) {
        if (this.progressListeners != other.progressListeners && (this.progressListeners == null || !this.progressListeners.equals(other.progressListeners)))
            return false;
        if (this.progressPercent != other.progressPercent)
            return false;
        if (this.outer != other.outer && (this.outer == null || !this.outer.equals(other.outer)))
            return false;
        if (this.event != other.event && (this.event == null || !this.event.equals(other.event)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((ProgressDeligate) obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.progressListeners != null
                            ? this.progressListeners.hashCode() : 0);
        hash = 97 * hash + this.progressPercent;
        hash = 97 * hash + (this.outer != null ? this.outer.hashCode() : 0);
        hash = 97 * hash + (this.event != null ? this.event.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ProgressDeligate{"
                + "progressListeners=" + progressListeners
                + ", progressPercent=" + progressPercent
                + '}';
    }

}
