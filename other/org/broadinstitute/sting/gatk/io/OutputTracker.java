/*
* Copyright (c) 2012 The Broad Institute
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.sting.gatk.io;

import net.sf.samtools.SAMFileReader;
import org.broadinstitute.sting.commandline.ArgumentSource;
import org.broadinstitute.sting.gatk.io.storage.Storage;
import org.broadinstitute.sting.gatk.io.storage.StorageFactory;
import org.broadinstitute.sting.gatk.io.stubs.OutputStreamStub;
import org.broadinstitute.sting.gatk.io.stubs.Stub;
import org.broadinstitute.sting.gatk.walkers.Walker;
import org.broadinstitute.sting.utils.classloader.JVMUtils;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.sam.SAMFileReaderBuilder;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the output and err streams that are created specifically for walker
 * output.
 */
public abstract class OutputTracker {
    /**
     * The streams to which walker users should be reading directly.
     */
    protected Map<ArgumentSource, Object> inputs = new HashMap<ArgumentSource,Object>();

    /**
     * The streams to which walker users should be writing directly.
     */
    protected Map<Stub,Storage> outputs = new HashMap<Stub,Storage>();

    /**
     * Special-purpose stub.  Provides a connection to output streams.
     */
    protected OutputStreamStub outStub = null;

    /**
     * Special-purpose stream.  Provides a connection to error streams.
     */
    protected OutputStreamStub errStub = null;

    /**
     * Gets the output storage associated with a given stub.
     * @param stub The stub for which to find / create the right output stream.
     * @param <T> Type of the stream to create.
     * @return Storage object with a facade of type T.
     */
    public abstract <T> T getStorage( Stub<T> stub );

    public void prepareWalker( Walker walker, SAMFileReader.ValidationStringency strictnessLevel ) {
        for( Map.Entry<ArgumentSource,Object> io: inputs.entrySet() ) {
            ArgumentSource targetField = io.getKey();
            Object targetValue = io.getValue();

            // Ghastly hack: reaches in and finishes building out the SAMFileReader.
            // TODO: Generalize this, and move it to its own initialization step.
            if( targetValue instanceof SAMFileReaderBuilder) {
                SAMFileReaderBuilder builder = (SAMFileReaderBuilder)targetValue;
                builder.setValidationStringency(strictnessLevel);
                targetValue = builder.build();
            }

            JVMUtils.setFieldValue( targetField.field, walker, targetValue );
        }
    }

    /**
     * Provide a mechanism for injecting supplemental streams for external management.
     * @param argumentSource source Class / field into which to inject this stream.
     * @param stub Stream to manage.
     */
    public void addInput( ArgumentSource argumentSource, Object stub ) {
        inputs.put(argumentSource,stub);
    }

    /**
     * Provide a mechanism for injecting supplemental streams for external management.
     * @param stub Stream to manage.
     */
    public <T> void addOutput(Stub<T> stub) {
        addOutput(stub,null);
    }

    /**
     * Provide a mechanism for injecting supplemental streams for external management.
     * @param stub Stream to manage.
     */
    public <T> void addOutput(Stub<T> stub, Storage<T> storage) {
        stub.register(this);
        outputs.put(stub,storage);        
    }

    /**
     * Close down all existing output streams.
     */
    public void close() {
        for( Stub stub: outputs.keySet() ) {
            // If the stream hasn't yet been created, create it so that there's at least an empty file present.
            if( outputs.get(stub) == null )
                getTargetStream(stub);

            // Close down the storage.
            outputs.get(stub).close();
        }
    }

    /**
     * Collects the target stream for this data.
     * @param stub The stub for this stream.
     * @param <T> type of stub.
     * @return An instantiated file into which data can be written.
     */
    protected <T> T getTargetStream( Stub<T> stub ) {
        if( !outputs.containsKey(stub) )
            throw new ReviewedStingException("OutputTracker was not notified that this stub exists: " + stub);
        Storage<T> storage = outputs.get(stub);
        if( storage == null ) {
            storage = StorageFactory.createStorage(stub);
            outputs.put(stub,storage);
        }
        return (T)storage;
    }

    /**
     * Install an OutputStreamStub into the given fieldName of the given walker.
     * @param walker Walker into which to inject the field name.
     * @param fieldName Name of the field into which to inject the stub.
     */
    private void installStub( Walker walker, String fieldName, OutputStream outputStream ) {
        Field field = JVMUtils.findField( walker.getClass(), fieldName );
        JVMUtils.setFieldValue( field, walker, outputStream );
    }    
}
