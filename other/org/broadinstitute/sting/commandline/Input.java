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

package org.broadinstitute.sting.commandline;

import java.lang.annotation.*;

/**
 * Annotates fields in objects that should be used as command-line arguments.
 * Any field annotated with @Input can appear as a command-line parameter.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Input {
    /**
     * The full name of the command-line argument.  Full names should be
     * prefixed on the command-line with a double dash (--).
     * @return Selected full name, or "" to use the default.
     */
    String fullName() default "";

    /**
     * Specified short name of the command.  Short names should be prefixed
     * with a single dash.  Argument values can directly abut single-char
     * short names or be separated from them by a space.
     * @return Selected short name, or "" for none.
     */
    String shortName() default "";

    /**
     * Documentation for the command-line argument.  Should appear when the
     * --help argument is specified.
     * @return Doc string associated with this command-line argument.
     */
    String doc() default "Undocumented option";

    /**
     * Is this argument required.  If true, the command-line argument system will
     * make a best guess for populating this argument based on the type descriptor,
     * and will fail if the type can't be populated.
     * @return True if the argument is required.  False otherwise.
     */
    boolean required() default true;

    /**
     * Should this command-line argument be exclusive of others.  Should be
     * a comma-separated list of names of arguments of which this should be
     * independent.
     * @return A comma-separated string listing other arguments of which this
     *         argument should be independent.
     */
    String exclusiveOf() default "";

    /**
     * Provide a regexp-based validation string.
     * @return Non-empty regexp for validation, blank otherwise.
     */
    String validation() default "";
}