package org.anodyneos.commons.text;

import java.io.Writer;
import java.io.FilterWriter;
import java.io.IOException;

/**
 * CsvWriter can be used as a <code>java.io.Writer</code> to write CSV files.
 * Since there is no formal specification for CSV files, I used the following:
 *
 * <pre>
 * ================================================================================
 * CSV Format
 * ================================================================================
 *
 * To create CSV:
 *
 * - Separate records with newlines
 *     - newlines may be dos or unix
 * - Separate fields with commas
 * - Fields:
 *     - Unquoted fields
 *         - Fields that do not contain quotes or newlines may be appear 'as is'.
 *     - Quoted fields
 *         - Any field may be quoted by beginning and ending the value with double
 *           quotes.
 *         - Embedded newlines (unix only) are OK - they will be treated as part
 *           of the field.
 *         - Each embedded double quote must be represented by two double quotes.
 * - End of file does not have to end with a newline.
 *
 *
 * FYI: "\n","\r","\n\r"
 * FYI2: Java Float.toString() uses "." regardless of locale.
 *
 * ================================================================================
 * http://groups.google.com/groups?hl=en&lr=&ie=UTF-8&oe=UTF-8&selm=19990526044656.12489.00003901%40ngol05.aol.com
 *
 * Why not start yet another thread?
 *
 * The following pseudo code represents how Microsoft Excel imports files in CSV
 * format. It's based on fairly exhaustive testing.
 *
 * loop until end of file {
 *   get character
 *   if (character == newline) {
 *     begin next record
 *     begin first field in record
 *   } else if (character == comma) { begin next field in record
 *   } else if (character == double quote) {
 *     loop until end of file {
 *       get character
 *       if (character == double quote) {
 *         get character
 *         if (character != double quote) {
 *           unget character
 *           break out of loop
 *         }
 *       }
 *       append character to current field
 *   } else { append character to current field
 *   }
 * }
 *
 * Let me propose the following complete CSV specification.
 *
 * 0. There are _no_ invalid records or fields - any text stream can be parsed
 * according to the following rules.
 * 1. All characters other than newlines, commas and double quotes are field
 * contents in all circumstances.
 * 2. Newlines separate records _except_ when embedded in double quoted fields.
 * 3. Commas separate fields _except_ when embedded in double quoted fields.
 * 4. Only if the first character in a field is a double quote is the field double
 * quoted.
 * 5. Double quotes may be embedded in double quoted fields by being doubled.
 * 6. Double quoted fields _don't_ end with the first undoubled double quote after
 * the initial double quote, but after it newlines and commas once again function
 * as record and field separators, respectively.
 * 7. Double quotes following the first undoubled double quote after the initial
 * double quote or in fields that aren't double quoted are field contents.
 * 8. Records and fields can be empty (zero length) strings.
 * 9. End of file serves as the terminator of the final field in the final record
 * (i.e., the last record doesn't have to end with a newline).
 *
 * This leaves open the question whether backslash escaped characters can be
 * embedded in double quoted fields. In the DOS/Windows world, the answer would be
 * no, but I wouldn't rule them out.
 *
 * I realize this is a fairly ugly file format, but it represents a significant
 * chunk of DOS/Windows reality. Choking on stray double quotes, mishandling
 * fields that don't match some regexp and failure to handle embedded newlines
 * render candidate CSV parsers no more than toys.
 *
 * Note: the comp.apps.spreadsheets faq includes a section of CSV format, but it's
 * not particularly detailed. It contains a link to a web page for a perl script
 * that converts CSV files to HTML tables. The CSV parser in that perl code is
 * very close to Jim Monty's offerings.
 *
 * ================================================================================
 * </pre>
 *
 * @author jvas
 */
public class CsvWriter extends FilterWriter {

    public static final char DOUBLE_QUOTE = '"';
    public static final char FIELD_SEPARATOR = ',';
    public static final char CSV_LS = '\n';
    //public static final String UNIX_LS = "\n";
    //public static final String MAC_LS = "\r";
    //public static final String WINDOWS_LS = "\n\r";

    int fieldNum = 0;
    boolean inQuotedField = false;
    boolean lastWasNewline = false;

    public CsvWriter(Writer out) {
        super(out);
    }

    public void close() throws IOException {
        if (inQuotedField) {
            endField();
        }
        out.close();
    }

    public void endRecord() throws IOException {
        if (inQuotedField) {
            throw new IOException("Cannot end record within a field, call endField() first");
        } else {
            out.write(CSV_LS);
            lastWasNewline = false;
            fieldNum = 0;
        }
    }

    public void endField() throws IOException {
        if(inQuotedField) {
            out.write(DOUBLE_QUOTE);
            inQuotedField = false;
        } else {
            // empty field
            out.write(FIELD_SEPARATOR);
        }
        lastWasNewline = false;
    }

    private void prepareQuotedField() throws IOException {
        if(! inQuotedField) {
            prepareField();
            out.write(DOUBLE_QUOTE);
            inQuotedField = true;
        }
    }

    private void prepareField() throws IOException {
        if(0 != fieldNum) {
            out.write(FIELD_SEPARATOR);
        }
        fieldNum++;
    }

    public void write(int c) throws IOException {
        prepareQuotedField();
        if (lastWasNewline && c == '\r') {
            lastWasNewline = false;
        } else if (c == '\r') {
            out.write(CSV_LS);
        } else if (c == '\n') {
            out.write(CSV_LS);
            lastWasNewline = true;
        } else {
            out.write(c);
            lastWasNewline = false;
        }
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        prepareQuotedField();
        boolean lwnl = lastWasNewline;
        /*  find '\r' || '"'
                output preceding chars
                output '', '\n', or two '"' chars
                reset cpOff;
        */
        int cpOff = off;
        for (int i = off; i < cbuf.length && i < off+len; i++) {
            char ch = cbuf[i];
            if (ch == '\r' || ch == '"') {
                // output preceeding chars (if any)
                int cpLen = i - cpOff;
                if (cpLen > 0) {
                    out.write(cbuf, cpOff, cpLen);
                }
                // output for this char (if nec)
                if (ch == '"') {
                    out.write("\"\"");
                } else if (! lwnl) {
                    out.write(CSV_LS);
                }
                // don't include this char on next copy
                cpOff = i+1;
                lwnl = false;
            } else if (ch == '\n') {
                lwnl = true;
            } else {
                lwnl = false;
            }
        }
        // output rest of chars (if any)
        int cpLen = len + off - cpOff;
        if (cpLen > 0) {
            out.write(cbuf, cpOff, cpLen);
        }
        lastWasNewline = lwnl;
    }

    public void write(String str, int off, int len) throws IOException {
        write(str.toCharArray(), off, len);
    }

    public void writeField(String str) throws IOException {
        if (inQuotedField) {
            throw new IOException("Cannot begin new field when already in field");
        } else if (-1 == str.indexOf(DOUBLE_QUOTE) &&
            -1 == str.indexOf(FIELD_SEPARATOR) &&
            -1 == str.indexOf('\r') &&
            -1 == str.indexOf('\n')) {
            // If no quotes, cr, lf, or comma, don't escape:
            prepareField();
            out.write(str, 0, str.length());
        } else {
            write(str, 0, str.length());
            endField();
        }
    }

    public void writeField(Number num) throws IOException {
        if (inQuotedField) {
            throw new IOException("Cannot begin new field when already in field");
        } else {
            prepareField();
            String val = num.toString();
            out.write(val, 0, val.length());
        }
    }

}
