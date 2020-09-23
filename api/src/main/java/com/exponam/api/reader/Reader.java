package com.exponam.api.reader;

import com.exponam.core.crypto.DecryptionUtilities;
import com.exponam.core.reader.BigReader;
import com.exponam.core.reader.Marshaller;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Reader is the single point of entry for accessing the contents of Exponam .BIG files.
 * <p>
 * A .BIG file contains one or more worksheets of data, where a worksheet is essentially a row and column
 * representation of data.  An individual data value is found at the intersection of three zero-based
 * indices: a worksheet index, a row index, and a column index.
 * <p>
 * Data in Exponam .BIG files are strongly typed on a per-column basis.  Depending on how the .BIG file was
 * generated, a column may contain empty values.  Depending on how data values are accessed via Reader, an
 * empty value will be represented as either null or Optional.empty().
 * <p>
 * Non-empty values are eligible to be returned to you in a variety of forms.  If you request the String
 * representation of a value, it will e returned to you respecting any display formats that were applied when
 * the .BIG file was generated.  If you request an alternate type, type conversion will be applied if required
 * and the value returned.  In the event that no conversion is available, an unchecked Exception will be thrown.
 * <p>
 * For example, if you request that a value for a Boolean column be returned as a Date, you will receive an
 * IllegalArgumentException.
 * <p>
 * Note that Reader is Closeable.  Failure to call close() can result in dangling references to underlying
 * structures.
 */
public final class Reader implements Closeable {
    /**
     * The types of columns that are supported in a .BIG file
     */
    public enum ColumnTypes {
        Boolean,
        Date,
        DateTime,
        Double,
        Long,
        String,
        Time
    }

    private BigReader bigReader;
    private final Marshaller marshaller;

    /**
     * Constructs an object that can read from an Exponam .BIG file.  Note that the
     * password is made available to Reader via a Supplier.  This is to facilitate
     * password mechanisms by the caller and minimize the amount of time during which
     * Reader retains access to that password.
     * @param bigFile the Exponam .BIG file to be accessed
     * @param passwordSupplier a caller-supplier function returning the password for the BigFile; can be null or return an empty String for unencrypted files
     * @throws IOException if there is a problem accessing the file
     * @throws BigReader.UnsupportedFileVersionException if the file version is more recent than supported by this version of the API
     */
    public Reader(File bigFile, Supplier<String> passwordSupplier) throws IOException, BigReader.UnsupportedFileVersionException {
        File validatedBigFile = validateBigFileParameter(bigFile);
        bigReader = new BigReader(new FileInputStream(validatedBigFile));

        String password = passwordSupplier == null ? "" : passwordSupplier.get();
        bigReader.setDecryptor(DecryptionUtilities.setupDecryptionForPassword(password, bigReader).get());

        this.marshaller = new Marshaller(this.bigReader);
    }

    /**
     * The number of worksheets contained in the file.
     *
     * Note: currently Exponam .BIG files contain a single worksheet.
     * @return the number of worksheets
     */
    public int getWorksheetCount() {
        return 1;
    }

    /**
     * The name given for a specific worksheet.
     *
     * @param worksheetIndex the zero-based worksheet index
     * @return the worksheet name
     */
    public String getWorksheetName(int worksheetIndex) {
        validateWorksheetIndex(worksheetIndex);
        return "Worksheet";
    }

    /**
     * The number of rows contained in a worksheet.
     *
     * @param worksheetIndex the zero-based worksheet index
     * @return the number of rows in that worksheet
     */
    public int getRowCount(int worksheetIndex) {
        validateWorksheetIndex(worksheetIndex);
        return bigReader.getWorksheet(worksheetIndex).getNumRows();
    }

    /**
     * The number of columns contained in a worksheet.
     *
     * @param worksheetIndex the zero-based worksheet index
     * @return the number of columns in that worksheet
     */
    public int getColumnCount(int worksheetIndex) {
        validateWorksheetIndex(worksheetIndex);
        return bigReader.getWorksheet(worksheetIndex).getColumns().count();
    }

    /**
     * The name of a given column in a worksheet.
     *
     * @param worksheetIndex the zero-based worksheet index
     * @param columnIndex the zero-based column index
     * @return the column name
     */
    public String getColumnName(int worksheetIndex, int columnIndex) {
        validateWorksheetAndColumnIndex(worksheetIndex, columnIndex);
        return bigReader.getWorksheet(worksheetIndex).getColumns().get(columnIndex).getName();
    }

    /**
     * The type of data held in a given column in a worksheet.
     *
     * @param worksheetIndex the zero-based worksheet index
     * @param columnIndex the zero-based column index
     * @return the column type
     */
    public ColumnTypes getColumnType(int worksheetIndex, int columnIndex) {
        validateWorksheetAndColumnIndex(worksheetIndex, columnIndex);
        switch (bigReader.getWorksheet(worksheetIndex).getColumns().get(columnIndex).getType()) {
            case Boolean:
                return ColumnTypes.Boolean;
            case Date:
                return ColumnTypes.Date;
            case DateTime:
                return ColumnTypes.DateTime;
            case Double:
                return ColumnTypes.Double;
            case Long:
                return ColumnTypes.Long;
            case String:
                return ColumnTypes.String;
            case Time:
                return ColumnTypes.Time;
            default:
                throw new IllegalArgumentException("Unknown column type");
        }
    }

    /**
     * A data value held within a .BIG file, at the intersection of a worksheet index,
     * row index, and column index, returned as an instance of desiredClass.
     * <p>
     * The supported data type conversions are:
     * <ul>
     *     <li>If desiredClass is String.class, any display formats specified within the .BIG file are applied to the value returned.</li>
     *     <li>If desiredClass is an Optional type, an empty data value is returned as Optional.empty().</li>
     *     <li>If desiredClass is not an Optional type, an empty data value is returned as null.</li>
     *     <li>The following type conversions are allowed:</li>
     *     <li>Boolean columns can be retrieved as
     *         <ul>
     *             <li>Boolean and Optional&lt;Boolean&gt;</li>
     *             <li>Long, Integer, Short, Byte, Double, Float, BigDecimal, and Optional of any of these</li>
     *         </ul>
     *     </li>
     *     <li>Date and DateTime columns can be retrieved as
     *         <ul>
     *             <li>java.util.Date and java.sql.Date, and Optional of either of these</li>
     *             <li>Long and BigDecimal, and Optional of either of these</li>
     *         </ul>
     *     </li>
     *     <li>Double columns can be retrieved as
     *         <ul>
     *             <li>Double and Optional&lt;Double&gt;</li>
     *             <li>Long, Integer, Short, Byte, Float, BigDecimal, and Optional of any of these</li>
     *         </ul>
     *     </li>
     *     <li>Long columns can be retrieved as
     *         <ul>
     *             <li>Long and Optional&lt;Long&gt;</li>
     *             <li>Integer, Short, Byte, Double, Float, BigDecimal, and Optional of any of these</li>
     *         </ul>
     *     </li>
     *     <li>String columns can be retrieved as
     *         <ul>
     *             <li>String and Optional&lt;String&gt; (note that Optional will never be empty)</li>
     *         </ul>
     *     </li>
     *     <li>Time columns can be retrieved as
     *         <ul>
     *             <li>java.util.Date and java.sql.Date, and Optional of either of these (note that the time is expressed relative to a Date of January 1, 0001)</li>
     *             <li>Long and BigDecimal, and Optional of either of these</li>
     *         </ul>
     *     </li>
     * </ul>
     * @param <T> the type to be returned, must match the desiredClass
     * @param worksheetIndex the zero-based worksheet index
     * @param rowIndex the zero-based row index
     * @param columnIndex the zero-based column index
     * @param desiredClass the desired type for the value
     * @return the column type
     */
    public <T> T getValue(int worksheetIndex, int rowIndex, int columnIndex, Type desiredClass) {
        validateWorksheetColumnAndRowIndex(worksheetIndex, rowIndex, columnIndex);
        Objects.requireNonNull(desiredClass, "desiredClass");
        return marshaller.getColumnValueAs(worksheetIndex, rowIndex, columnIndex, desiredClass);
    }

    /**
     * This method is recommended for reading large numbers of row-wise data.  The types that
     * are supported in desiredClassForColumns follow the same rules as given for getValue.
     * @param worksheetIndex the zero-based worksheet index
     * @param startRow the zero-based index for the first row in the range of rows to be retrieved
     * @param endRow the zero-based index for the last row in the range of rows to be retrieved
     * @param columnIndexes array of the zero-based column indexes for the columns to be retrieved
     * @param desiredClassForColumns array of the desired type for for each of the columns to be retrieved
     * @param rowConsumer the callback invoked for each row that is retrieved
     */
    public void getRowValues(int worksheetIndex, int startRow, int endRow,
                             int[] columnIndexes, Type[] desiredClassForColumns,
                             BiConsumer<Integer, Object[]> rowConsumer) {
        validateWorksheetIndex(worksheetIndex);
        if (startRow < 0) throw new RuntimeException("Start row must be >= 0");
        if (endRow >= getRowCount(worksheetIndex)) throw new RuntimeException(
                String.format("End row must be < %d", getRowCount(worksheetIndex)));
        if (startRow > endRow) throw new RuntimeException("Start row must be <= end row");

        marshaller.fetchRows(worksheetIndex, startRow, endRow,
                columnIndexes, desiredClassForColumns,
                rowConsumer);
    }

    /**
     * Close the Reader when through, allowing underlying resources to be properly released.
     *
     * @throws IOException exception
     */
    @Override
    public void close() throws IOException {
        if (bigReader != null) {
            bigReader.close();
            bigReader = null;
        }
    }

    private static File validateBigFileParameter(File bigFile) {
        Objects.requireNonNull(bigFile, "bigFile");
        if (bigFile.isDirectory()) throw new IllegalArgumentException(String.format("'%s' is a directory.", bigFile.getName()));
        if (!bigFile.exists()) throw new IllegalArgumentException(String.format("'%s' does not exist", bigFile.getName()));
        return bigFile;
    }

    private void validateWorksheetIndex(int worksheetIndex) {
        if (worksheetIndex < 0 || worksheetIndex >= getWorksheetCount())
            throw new IllegalArgumentException(String.format("Worksheet index '%d' out of range", worksheetIndex));
    }

    private void validateWorksheetAndColumnIndex(int worksheetIndex, int columnIndex) {
        validateWorksheetIndex(worksheetIndex);
        if (columnIndex < 0 || columnIndex >= getColumnCount(worksheetIndex))
            throw new IllegalArgumentException(String.format("Column index '%d' out of range", columnIndex));
    }

    private void validateWorksheetColumnAndRowIndex(int worksheetIndex, int rowIndex, int columnIndex) {
        validateWorksheetAndColumnIndex(worksheetIndex, columnIndex);
        if (rowIndex < 0 || rowIndex >= getRowCount(worksheetIndex))
            throw new IllegalArgumentException(String.format("Row index '%d' out of range", rowIndex));
    }
}
