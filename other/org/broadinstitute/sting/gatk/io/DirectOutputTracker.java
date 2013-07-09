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

import org.broadinstitute.sting.gatk.io.storage.Storage;
import org.broadinstitute.sting.gatk.io.storage.StorageFactory;
import org.broadinstitute.sting.gatk.io.stubs.Stub;

/**
 * Maps creation of storage directly to output streams in parent.
 *
 * @author mhanna
 * @version 0.1
 */
public class DirectOutputTracker extends OutputTracker {
    public <T> T getStorage( Stub<T> stub ) {
        Storage target = outputs.get(stub);
        if( target == null ) {
            target = StorageFactory.createStorage(stub);
            outputs.put(stub, target);
        }
        return (T)target;
    }

}
