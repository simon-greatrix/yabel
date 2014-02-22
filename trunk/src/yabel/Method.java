package yabel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import yabel.attributes.Attribute;
import yabel.attributes.AttributeList;
import yabel.attributes.AttributeListListener;
import yabel.attributes.Exceptions;
import yabel.code.Code;
import yabel.constants.ConstantPool;
import yabel.constants.ConstantUtf8;
import yabel.io.IO;

/**
 * Method specification
 * 
 * @author Simon Greatrix
 * 
 */
public class Method implements AttributeListListener {
    /**
     * Get the class arguments specified by a method signature
     * 
     * @param type
     *            the method signature
     * @return the parameter classes
     */
    public static Class<?>[] getArgClassesForType(String type) {
        List<Class<?>> cls = new ArrayList<Class<?>>();

        if( !type.startsWith("(") )
            throw new IllegalArgumentException("No starting '(' : " + type);
        int p = type.lastIndexOf(')');
        if( p == -1 )
            throw new IllegalArgumentException("No closing ')' : " + type);
        type = type.substring(1, p);
        int start = 0;
        for(int i = 0;i < type.length();i++) {
            char c = type.charAt(i);
            if( "ZBCDFIJS;".indexOf(c) != -1 ) {
                String cl = type.substring(start, i + 1);
                cls.add(ClassUtil.getClass(cl));
            }
        }

        Class<?>[] ret = new Class<?>[cls.size()];
        return cls.toArray(ret);
    }


    /**
     * How many arguments are needed on the stack for a method? Longs and
     * doubles count as 2 as they take two stack slots.
     * 
     * @param type
     *            the method type
     * @return the number of arguments
     */
    public static int getArgsForType(String type) {
        if( !type.startsWith("(") )
            throw new IllegalArgumentException("No starting '(' : " + type);
        int p = type.lastIndexOf(')');
        if( p == -1 )
            throw new IllegalArgumentException("No closing ')' : " + type);
        type = type.substring(1, p);
        int count = 0;
        boolean isArray = false;
        for(int i = 0;i < type.length();i++) {
            char c = type.charAt(i);
            switch (c) {
            case 'Z': // boolean
                count++;
                break;
            case 'B': // byte
                count++;
                break;
            case 'C': // char
                count++;
                break;
            case 'D': // double
                count += isArray ? 1 : 2;
                break;
            case 'F': // float
                count++;
                break;
            case 'I': // int;
                count++;
                break;
            case 'J': // long
                count += isArray ? 1 : 2;
                break;
            case 'S': // short
                count++;
                break;
            case 'L': // object
                count++;
                p = type.indexOf(';', i);
                if( p == -1 )
                    throw new IllegalArgumentException(
                            "No closing ';' to object name at position " + i
                                    + " : " + type);
                i = p;
                break;
            case '[': // array
                isArray = true;
                break;
            }
            if( c != '[' ) isArray = false;
        }

        return count;
    }


    /**
     * Get a method that is inherited from the specified class
     * 
     * @param builder
     *            the class builder that will use the method
     * @param name
     *            the method's name
     * @param args
     *            the method's argument
     * @param ret
     *            the method's return type
     * @param cls
     *            the class the method is inherited through
     * @param samePackage
     *            if true, the search is still in the original package
     * @return Method if found, or null
     */
    public static Method getInheritedMethod(ClassBuilder builder, String name,
            Class<?>[] args, Class<?> ret, Class<?> cls, boolean samePackage) {
        java.lang.reflect.Method[] meths = cls.getDeclaredMethods();
        for(java.lang.reflect.Method m:meths) {
            if( !m.getName().equals(name) ) continue;

            // check it is inheritable
            int mod = m.getModifiers();

            // private members are not inherited
            if( Modifier.isPrivate(mod) ) continue;

            // must be either public, protected or in same package
            if( !(samePackage || Modifier.isProtected(mod) || Modifier.isPublic(mod)) )
                continue;

            Class<?>[] methArgs = m.getParameterTypes();
            if( methArgs.length != args.length ) continue;
            for(int i = 0;i < methArgs.length;i++) {
                if( !methArgs[i].equals(args[i]) ) continue;
            }

            Class<?> methRet = m.getReturnType();
            if( !methRet.equals(ret) ) continue;

            // have found it
            return new Method(builder, m);
        }

        Class<?> superCls = cls.getSuperclass();
        if( superCls != null )
            return getInheritedMethod(
                    builder,
                    name,
                    args,
                    ret,
                    superCls,
                    samePackage
                            && cls.getPackage().equals(superCls.getPackage()));

        // not found
        return null;
    }


    /**
     * Get a method that is inherited from the specified class
     * 
     * @param builder
     *            the class builder that will use the method
     * @param name
     *            the method's name
     * @param type
     *            the method's signature
     * @param cls
     *            the class the method is inherited through
     * @param samePackage
     *            if true, the search is still in the original package
     * @return Method if found, or null
     */
    public static Method getInheritedMethod(ClassBuilder builder, String name,
            String type, Class<?> cls, boolean samePackage) {
        Class<?>[] args = getArgClassesForType(type);
        Class<?> ret = getReturnClassForType(type);
        return getInheritedMethod(builder, name, args, ret, cls, samePackage);
    }


    /**
     * Get the return class specified by a method signature
     * 
     * @param type
     *            the method signature
     * @return the class
     */
    public static Class<?> getReturnClassForType(String type) {
        if( !type.startsWith("(") )
            throw new IllegalArgumentException("No starting '(' : " + type);
        int p = type.lastIndexOf(')');
        if( p == -1 )
            throw new IllegalArgumentException("No closing ')' : " + type);
        type = type.substring(p + 1);
        return ClassUtil.getClass(type);
    }

    /** Access modifier */
    private final int access_;

    /** Attributes of this method */
    private final AttributeList attrList_;

    /**
     * ClassBuilder this is a method of
     */
    private final ClassBuilder classBuilder_;

    /** The constant pool for this class */
    private final ConstantPool cp_;

    /** Method name */
    ConstantUtf8 name_;

    /** Method type */
    final ConstantUtf8 type_;


    /**
     * Create a method from ClassData
     * 
     * @param classBuilder
     *            the class the method will be a part of
     * @param data
     *            the description of this method
     */
    Method(ClassBuilder classBuilder, ClassData data) {
        classBuilder_ = classBuilder;
        cp_ = classBuilder_.getConstantPool();
        access_ = Access.accessCode(data.get(String.class, "access", ""));
        name_ = new ConstantUtf8(cp_, data.getSafe(String.class, "name"));
        type_ = new ConstantUtf8(cp_, data.getSafe(String.class, "type"));
        attrList_ = new AttributeList(cp_, data.getList(ClassData.class,
                "attributes"));
        attrList_.setOwner(this);
    }


    /**
     * Read a method from the input.
     * 
     * @param input
     *            the stream
     * @param classBuilder
     *            Class containing this method
     * @throws IOException
     */
    Method(ClassBuilder classBuilder, InputStream input) throws IOException {
        classBuilder_ = classBuilder;
        cp_ = classBuilder_.getConstantPool();
        access_ = IO.readU2(input);
        int id = IO.readU2(input);
        name_ = cp_.validate(id, ConstantUtf8.class);
        id = IO.readU2(input);
        type_ = cp_.validate(id, ConstantUtf8.class);
        attrList_ = new AttributeList(cp_, input);
        attrList_.setOwner(this);
    }


    /**
     * New method
     * 
     * @param access
     *            access modifier
     * @param name
     *            name
     * @param type
     *            type
     * @param classBuilder
     *            Class containing this method
     */
    Method(ClassBuilder classBuilder, int access, String name, String type) {
        classBuilder_ = classBuilder;
        cp_ = classBuilder.getConstantPool();
        access_ = access;
        name_ = new ConstantUtf8(cp_, name);
        type_ = new ConstantUtf8(cp_, type);
        attrList_ = new AttributeList();
        attrList_.setOwner(this);
    }


    /**
     * New method from a <code>java.lang.reflect.Method</code>.
     * 
     * @param classBuilder
     *            the associated class
     * @param method
     *            the method
     */
    Method(ClassBuilder classBuilder, java.lang.reflect.Method method) {
        classBuilder_ = classBuilder;
        cp_ = classBuilder.getConstantPool();
        access_ = Access.accessCode(method);
        name_ = new ConstantUtf8(cp_, method.getName());
        type_ = new ConstantUtf8(cp_,
                ClassUtil.getTypeName(method.getReturnType()));
        attrList_ = new AttributeList();
        attrList_.setOwner(this);
    }


    /**
     * The attributes of this method have changed. The "Code" attribute needs to
     * be told which class and method it is part of.
     * 
     * @param attrId
     *            the name of the changed attribute
     * @param attr
     *            the new value of the attribute
     */
    @Override
    public void attributeChanged(String attrId, Attribute attr) {
        if( attrId.equals(Attribute.ATTR_CODE) && (attr != null) ) {
            ((Code) attr).setOwner(classBuilder_, this);
        }
    }


    /**
     * Get the access modifier.
     * 
     * @return the modifier
     */
    public int getAccess() {
        return access_;
    }


    /**
     * Get the attributes for this method.
     * 
     * @return the attributes
     */
    public AttributeList getAttributes() {
        return attrList_;
    }


    /**
     * Get associated code attribute
     * 
     * @return code
     */
    public Code getCode() {
        Code code = (Code) attrList_.get(cp_, Attribute.ATTR_CODE);
        if( code == null ) {
            code = new Code(cp_);
            attrList_.set(code);
        }
        return code;
    }


    /**
     * Get the constant pool for this method
     * 
     * @return the constant pool
     */
    public ConstantPool getConstantPool() {
        return cp_;
    }


    /**
     * Get associated exceptions attribute
     * 
     * @return exceptions
     */
    public Exceptions getExceptions() {
        Exceptions excep = (Exceptions) attrList_.get(cp_,
                Attribute.ATTR_EXCEPTIONS);
        if( excep == null ) {
            excep = new Exceptions(cp_);
            attrList_.set(excep);
        }
        return excep;
    }


    /**
     * Get the name of this method.
     * 
     * @return this method's name
     */
    public ConstantUtf8 getName() {
        return name_;
    }


    /**
     * Get the type of this method.
     * 
     * @return this method's name
     */
    public ConstantUtf8 getType() {
        return type_;
    }


    /**
     * Set the name of this method. Note that the original method name remains
     * in the constant pool.
     * 
     * @param newName
     *            the new name for this method
     */
    public void setName(String newName) {
        name_ = new ConstantUtf8(cp_, newName);
    }


    /**
     * Get the ClassData representation of this
     * 
     * @return the representation
     */
    public ClassData toClassData() {
        ClassData cd = new ClassData();
        cd.put("access", Access.accessCode(access_));
        cd.put("name", name_.get());
        cd.put("type", type_.get());
        cd.putList(ClassData.class, "attributes", attrList_.toClassData());
        return cd;
    }


    /**
     * Write Method to output
     * 
     * @param baos
     *            output
     */
    public void writeTo(ByteArrayOutputStream baos) {
        IO.writeU2(baos, access_);
        IO.writeU2(baos, name_.getIndex());
        IO.writeU2(baos, type_.getIndex());
        attrList_.writeTo(baos);
    }
}