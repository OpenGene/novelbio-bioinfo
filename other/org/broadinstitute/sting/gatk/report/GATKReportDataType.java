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

package org.broadinstitute.sting.gatk.report;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The gatherable data types acceptable in a GATK report column.
 */
public enum GATKReportDataType {
    /**
     * The null type should not be used.
     */
    Null("Null"),

    /**
     * The default value when a format string is not present
     */
    Unknown("Unknown"),

    /**
     * Used for boolean values. Will display as true or false in the table.
     */
    Boolean("%[Bb]"),

    /**
     * Used for char values. Will display as a char so use printable values!
     */
    Character("%[Cc]"),

    /**
     * Used for float and double values. Will output a decimal with format %.8f unless otherwise specified.
     */
    Decimal("%.*[EeFf]"),

    /**
     * Used for int, byte, short, and long values. Will display the full number by default.
     */
    Integer("%[Dd]"),

    /**
     * Used for string values. Displays the string itself.
     */
    String("%[Ss]");

    private final String dataTypeString;

    private GATKReportDataType(String dataTypeString) {
        this.dataTypeString = dataTypeString;
    }

    private static final Map<String, GATKReportDataType> lookup = new HashMap<String, GATKReportDataType>();

    static {
        for (GATKReportDataType s : EnumSet.allOf(GATKReportDataType.class))
            lookup.put(s.dataTypeString, s);
    }


    @Override
    public String toString() {
        return this.dataTypeString;
    }

    /**
     * Returns a GATK report data type from the Object specified. It looks through the list of acceptable classes and
     * returns the appropriate data type.
     *
     * @param object the object ot derive the data type from
     * @return the appropriate data type
     */
    public static GATKReportDataType fromObject(Object object) {
        GATKReportDataType value;
        if (object instanceof Boolean) {
            value = GATKReportDataType.Boolean;

        } else if (object instanceof Character) {
            value = GATKReportDataType.Character;

        } else if (object instanceof Float ||
                object instanceof Double) {
            value = GATKReportDataType.Decimal;

        } else if (object instanceof Integer ||
                object instanceof Long ||
                object instanceof Short ||
                object instanceof Byte ) {
            value = GATKReportDataType.Integer;

        } else if (object instanceof String) {
            value = GATKReportDataType.String;

        } else {
            value = GATKReportDataType.Unknown;
            //throw new UserException("GATKReport could not convert the data object into a GATKReportDataType. Acceptable data objects are found in the documentation.");
        }
        return value;
    }

    /**
     * Returns a GATK report data type from the format string specified. It uses regex matching from the enumerated
     * Strings.
     *
     * @param format the format string to derive the data type from
     * @return the appropriate data type
     */
    public static GATKReportDataType fromFormatString(String format) {
        if (format.equals(""))
            return Unknown;
        for (GATKReportDataType type : lookup.values()) {
            if (format.matches(type.toString()) )
                return type;
        }
        return Unknown;
    }

    /**
     * Returns the default value of the data type. It returns an object that matches the class of the data type.
     *
     * @return an object that matches the data type
     */
    public Object getDefaultValue() {
        switch (this) {
            case Decimal:
                return 0.0D;
            case Boolean:
                return false;
            case Character:
                return '0';
            case Integer:
                return 0L;
            case String:
                return "";
            default:
                return null;
        }
    }

    /**
     * Checks if the two objects are equal using the appropriate test form the data types.
     *
     * @param a an object
     * @param b another object to check if equal
     * @return true - the objects are equal, false - the objects are nto equal
     */
    public boolean isEqual(Object a, Object b) {
        switch (this) {
            case Null:
                return true;
            case Decimal:
            case Boolean:
            case Integer:
                return a.toString().equals(b.toString());
            case Character:
            case String:
            default:
                return a.equals(b);
        }
    }

    /**
     * Converts an input String to the appropriate type using the data type. Used for parsing loading a GATK report from
     * file.
     *
     * @param obj The input string
     * @return an object that matches the data type.
     */
    Object Parse(Object obj) {
        if (obj instanceof String) {
            String str = obj.toString();
            switch (this) {
                case Decimal:
                    return Double.parseDouble(str);
                case Boolean:
                    return java.lang.Boolean.parseBoolean(str);
                case Integer:
                    return Long.parseLong(str);
                case String:
                    return str;
                case Character:
                    return str.toCharArray()[0];
                default:
                    return str;
            }
        } else
            return null;
    }

    /**
     * Returns a format string version of the value according to the data type.
     *
     * @return The printf string representation of the object according to data type.
     */
    public String getDefaultFormatString() {
        switch (this) {
            case Decimal:
                return "%.8f";
            case Boolean:
                return "%b";
            case Integer:
                return "%d";
            case String:
                return "%s";
            case Character:
                return "%c";
            case Null:
            default:
                return "%s";
        }
    }
}
