package yabel.attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import yabel.ClassData;
import yabel.code.Location;
import yabel.code.NamedLocation;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;

/**
 * 
 * @author Simon Greatrix
 */
public class LocalVariableTable extends Attribute implements
        Iterable<LocalVariableTable.Scope> {

    /**
     * Definition of the scope of a local variable.
     * 
     * @author Simon Greatrix
     */
    public static class Scope {
        /** The end of this variable's scope */
        private Location endPC_;

        /** The local variable's index */
        private final int index_;

        /** The name of this variable */
        private final ConstantUtf8 name_;

        /** The start of this variable's scope */
        private Location startPC_;

        /** The type of this variable */
        private final ConstantUtf8 type_;


        /**
         * Create a variable scope from ClassData
         * 
         * @param cp
         *            the constant pool
         * @param cd
         *            the definition
         */
        Scope(ConstantPool cp, ClassData cd) {
            startPC_ = getLoc(cd, "start");
            endPC_ = getLoc(cd, "end");
            name_ = new ConstantUtf8(cp, cd.getSafe(String.class, "name"));
            type_ = new ConstantUtf8(cp, cd.getSafe(String.class, "type"));
            index_ = cd.getSafe(Integer.class, "index").intValue();
        }


        /**
         * Create a variable scope from byte code
         * 
         * @param cp
         *            the constant pool
         * @param in
         *            the byte code
         */
        Scope(ConstantPool cp, InputStream in) throws IOException {
            int startPC = IO.readU2(in);
            int length = IO.readU2(in);
            startPC_ = new Location(startPC);
            endPC_ = new Location(startPC + length);
            name_ = cp.validate(IO.readU2(in), ConstantUtf8.class);
            type_ = cp.validate(IO.readU2(in), ConstantUtf8.class);
            index_ = IO.readU2(in);
        }


        /**
         * Create a new variable scope
         * 
         * @param cp
         *            the class's constant pool
         * @param index
         *            the variable's index in the local frame
         * @param name
         *            the variable's name
         * @param type
         *            the variable's type
         */
        public Scope(ConstantPool cp, int index, String name, String type) {
            index_ = index;
            name_ = new ConstantUtf8(cp, name);
            type_ = new ConstantUtf8(cp, name);
            startPC_ = new Location();
            endPC_ = new Location();
        }


        public Location getEndPC() {
            return endPC_;
        }


        public int getIndex() {
            return index_;
        }


        public ConstantUtf8 getName() {
            return name_;
        }


        public Location getStartPC() {
            return startPC_;
        }


        public ConstantUtf8 getType() {
            return type_;
        }


        public void setEndPC(Location endPC) {
            endPC_ = endPC;
        }


        public void setStartPC(Location startPC) {
            startPC_ = startPC;
        }


        /**
         * Get this variable definition as class data
         * 
         * @return this variable's definition
         */

        public ClassData toClassData() {
            ClassData cd = new ClassData();
            cd.put("start", startPC_.getIdentifier());
            cd.put("end", endPC_.getIdentifier());
            cd.put("name", name_.get());
            cd.put("type", type_.get());
            cd.put("index", Integer.valueOf(index_));
            return cd;
        }


        /**
         * Write this variable definition in class file format
         * 
         * @param baos
         *            the output stream
         */
        public void writeTo(ByteArrayOutputStream baos) {
            IO.writeU2(baos, startPC_.getLocationSafe());
            IO.writeU2(baos, endPC_.getLocationSafe());
            IO.writeU2(baos, name_.getIndex());
            IO.writeU2(baos, type_.getIndex());
            IO.writeU2(baos, index_);
        }
    }


    /**
     * Get a location to put a variable scope at
     * 
     * @param cd
     *            the ClassData specifying the scope
     * @param name
     *            the name of this part of the scope
     * @return the Location
     */
    static Location getLoc(ClassData cd, String name) {
        String id = cd.get(String.class, name);
        if( id != null ) return new NamedLocation(id);
        Integer loc = cd.getSafe(Integer.class, name);
        return new Location(loc.intValue());
    }

    /** The local variable definitions */
    private final List<Scope> vars_;


    /**
     * Create a new LocalVariableTable.
     * 
     * @param cp
     *            this class's constant pool
     */
    public LocalVariableTable(ConstantPool cp) {
        super(cp, ATTR_LOCAL_VARIABLE_TABLE);
        vars_ = new ArrayList<Scope>();
    }


    /**
     * Create LocalVariableTable from ClassData
     * 
     * @param cp
     *            the class's constant pool
     * @param cd
     *            the class data
     */
    public LocalVariableTable(ConstantPool cp, ClassData cd) {
        super(cp, ATTR_LOCAL_VARIABLE_TABLE);
        List<ClassData> table = cd.getListSafe(ClassData.class, "table");
        int size = table.size();
        vars_ = new ArrayList<Scope>(size);
        for(ClassData data:table) {
            vars_.add(new Scope(cp, data));
        }
    }


    /**
     * Create LocalVariableTable from byte code
     * 
     * @param cp
     *            this class's constant pool
     * @param in
     *            the byte code
     */
    public LocalVariableTable(ConstantPool cp, InputStream in)
            throws IOException {
        super(cp, ATTR_LOCAL_VARIABLE_TABLE);
        int len = IO.readS4(in);
        int size = IO.readU2(in);
        if( size * 10 + 2 != len )
            throw new IOException("LocalVariableTable attribute has " + len
                    + " bytes but contains " + size + " entries");
        vars_ = new ArrayList<Scope>(size);
        for(int i = 0;i < size;i++) {
            vars_.add(new Scope(cp, in));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Scope> iterator() {
        return vars_.iterator();
    }


    /**
     * {@inheritDoc}
     * 
     * @see yabel.attributes.Attribute#toClassData()
     */
    @Override
    public ClassData toClassData() {
        List<ClassData> table = new ArrayList<ClassData>(vars_.size());
        for(Scope v:vars_) {
            table.add(v.toClassData());
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
        IO.writeU2(baos, vars_.size() * 10 + 2);
        IO.writeU2(baos, vars_.size());
        for(Scope v:vars_) {
            v.writeTo(baos);
        }
    }
}
