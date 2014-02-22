package yabel.attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import yabel.ClassData;
import yabel.constants.ConstantPool;
import yabel.io.IO;

/**
 * The line number table attribute.
 * 
 * @author Simon Greatrix
 */
public class LineNumberTable extends Attribute implements
        Iterable<LineNumberTable.LNV> {
    /**
     * A line number value indicating the starting PC, the line number and the
     * number of preceding markers on the same line.
     * 
     * @author Simon Greatrix
     */
    public class LNV implements Comparable<LNV> {
        /** Number of entries that precede this and specify the same line */
        int count_ = 0;

        /** Associated line number */
        private final int lineNum_;

        /** Starting position in Code block */
        private final int startPC_;


        /**
         * Create new line number value
         * 
         * @param classdata
         *            data for line number in source code
         */
        LNV(ClassData classdata) {
            startPC_ = classdata.getSafe(Integer.class, "start").intValue();
            lineNum_ = classdata.getSafe(Integer.class, "lineNumber").intValue();
        }


        /**
         * Read a new line number value
         * 
         * @param in
         *            stream to read from
         */
        LNV(InputStream in) throws IOException {
            startPC_ = IO.readU2(in);
            lineNum_ = IO.readU2(in);
        }


        /**
         * Create new line number value
         * 
         * @param startPC
         *            starting PC
         * @param lineNum
         *            line number in source code
         */
        LNV(int startPC, int lineNum) {
            startPC_ = startPC;
            lineNum_ = lineNum;
        }


        /**
         * Compares two LNV and sorts them by line-number and then start
         * position.
         * 
         * @param other
         *            the other LNV
         * @return comparison result
         */
        @Override
        public int compareTo(LNV other) {
            int c = lineNum_ - other.lineNum_;
            if( c != 0 ) return c;
            return startPC_ - other.startPC_;
        }


        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            if( obj == null ) return false;
            if( obj == this ) return true;
            LineNumberTable.LNV other = (LineNumberTable.LNV) obj;
            return (lineNum_ == other.lineNum_) && (startPC_ == other.startPC_);
        }


        /**
         * Get the number of table entries that define the same line but precede
         * this one
         * 
         * @return count of preceding entries
         */
        public int getCount() {
            return count_;
        }


        /**
         * Get the associated line number.
         * 
         * @return the line number
         */
        public int getLine() {
            return lineNum_;
        }


        /**
         * Get the starting position in the Code block.
         * 
         * @return the starting position
         */
        public int getStartPC() {
            return startPC_;
        }


        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return lineNum_ ^ startPC_;
        }


        /**
         * Create the ClassData representation of this.
         * 
         * @return the ClassData
         */
        public ClassData toClassData() {
            ClassData cd = new ClassData();
            cd.put("start", Integer.valueOf(startPC_));
            cd.put("lineNumber", Integer.valueOf(lineNum_));
            return cd;
        }


        /**
         * Convert this to a String. The String will be of the form
         * "LINE_&lt;number&gt;" or "LINE_&lt;number&gt;_&lt;count&gt;" and may
         * be used as a label in the code.
         * 
         * @return this as a label
         */
        @Override
        public String toString() {
            Integer l = Integer.valueOf(lineNum_);
            if( count_ == 0 ) return String.format("LINE_%d", l);

            Integer c = Integer.valueOf(count_ + 1);
            return String.format("LINE_%d_%d", l, c);
        }


        /**
         * Write this LNV to a stream
         * 
         * @param baos
         *            stream to write to
         */
        void writeTo(ByteArrayOutputStream baos) {
            IO.writeU2(baos, startPC_);
            IO.writeU2(baos, lineNum_);
        }
    }

    /** The line number values in this table */
    private final List<LNV> lnvs_;


    /**
     * Create new LineNumberTable attribute from ClassData
     * 
     * @param cp
     *            the constant pool
     */
    public LineNumberTable(ConstantPool cp) {
        super(cp, ATTR_LINE_NUMBER_TABLE);
        lnvs_ = new ArrayList<LNV>();
        resetCounts();
    }


    /**
     * Create new LineNumberTable attribute from ClassData
     * 
     * @param cp
     *            the constant pool
     * @param cd
     *            the class data
     */
    public LineNumberTable(ConstantPool cp, ClassData cd) {
        super(cp, ATTR_LINE_NUMBER_TABLE);
        List<ClassData> table = cd.getListSafe(ClassData.class, "table");
        int size = table.size();
        lnvs_ = new ArrayList<LNV>(size);
        for(ClassData data:table) {
            lnvs_.add(new LNV(data));
        }
        resetCounts();
    }


    /**
     * Read new LineNumberTable attribute from a stream
     * 
     * @param cp
     *            the constant pool
     * @param in
     *            the stream
     */
    public LineNumberTable(ConstantPool cp, InputStream in) throws IOException {
        super(cp, ATTR_LINE_NUMBER_TABLE);
        int len = IO.readS4(in);
        int size = IO.readU2(in);
        if( size * 4 + 2 != len )
            throw new IOException("LineNumberTable attribute has " + len
                    + " bytes but contains " + size + " entries");
        lnvs_ = new ArrayList<LNV>(size);
        for(int i = 0;i < size;i++) {
            lnvs_.add(new LNV(in));
        }
        resetCounts();
    }


    /**
     * Add an entry to this line number table
     * 
     * @param startPc
     *            the starting PC for the line
     * @param lineNum
     *            the line number
     */
    public void add(int startPc, int lineNum) {
        LNV lnv = new LNV(startPc, lineNum);
        if( !lnvs_.contains(lnv) ) {
            lnvs_.add(lnv);
            resetCounts();
        }
    }


    /**
     * Is this table empty?
     * 
     * @return true if empty
     */
    public boolean isEmpty() {
        return lnvs_.isEmpty();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<LNV> iterator() {
        return lnvs_.iterator();
    }


    /**
     * Remove an entry from this table
     * 
     * @param startPc
     *            the starting PC to match (or -1 to match any)
     * @param lineNum
     *            the line number to match (or -1 to match any)
     * @return the number of entries removed
     */
    public int remove(int startPc, int lineNum) {
        Iterator<LNV> iter = lnvs_.iterator();
        int cnt = 0;
        while( iter.hasNext() ) {
            LNV lnv = iter.next();
            if( (startPc == -1) || (startPc == lnv.getStartPC()) ) {
                if( (lineNum == -1) || (lineNum == lnv.getLine()) ) {
                    iter.remove();
                    cnt++;
                }
            }
        }
        if( cnt > 0 ) {
            resetCounts();
        }
        return cnt;
    }


    /** Reset the counts to ensure uniqueness */
    private void resetCounts() {
        if( lnvs_.isEmpty() ) return;

        // reset the counts to ensure uniqueness
        Collections.sort(lnvs_);
        int prevLine = lnvs_.get(0).getLine() - 1;
        int count = -1;
        for(LNV v:lnvs_) {
            if( v.getLine() != prevLine ) {
                count = 0;
                prevLine = v.getLine();
            } else {
                count++;
            }
            v.count_ = count;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.attributes.Attribute#toClassData()
     */
    @Override
    public ClassData toClassData() {
        List<ClassData> table = new ArrayList<ClassData>(lnvs_.size());
        for(LNV lnv:lnvs_) {
            table.add(lnv.toClassData());
        }
        ClassData ret = makeClassData();
        ret.putList(ClassData.class, "table", table);
        return ret;
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.attributes.Attribute#writeTo(java.io.ByteArrayOutputStream)
     */
    @Override
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, attrId_.getIndex());
        IO.writeS4(baos, 2 + 4 * lnvs_.size());
        for(LNV lnv:lnvs_) {
            lnv.writeTo(baos);
        }
    }
}
