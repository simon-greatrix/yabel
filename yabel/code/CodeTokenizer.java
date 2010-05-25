package yabel.code;

import yabel.ClassData;

import java.util.*;

/**
 * Break the source code into a stream of expanded tokens. The source code
 * is initially whitespace delimited. Parameter lists can be supplied by
 * delimiting the parameters and op-code with colons. Text enclosed in
 * braces (e.g. {like} {this}) will be iteratively expanded. Whitespace and
 * colons in parameters must be represented using normal Java escapes (e.g.
 * {like\40this}).
 * 
 * @author Simon Greatrix
 * 
 */
class CodeTokenizer implements Iterator<List<String>> {
    /** Position in source */
    private int position_ = 0;

    /** Replacements to substitute in */
    private final ClassData replacements_;

    /** Unmodifiable view on returned list */
    private final List<String> ret_;

    /** Source to tokenize */
    private final String src_;

    /** Tokenizer for direct expansions */
    private final LinkedList<CodeTokenizer> subTokenizers_ = new LinkedList<CodeTokenizer>();

    /** List to return */
    private final List<String> token_ = new ArrayList<String>();


    /**
     * New code tokenizer
     * 
     * @param src
     *            source to tokenize
     * @param replacements
     *            replacements to substitute in
     */
    CodeTokenizer(String src, ClassData replacements) {
        src_ = src;
        replacements_ = replacements;
        ret_ = Collections.unmodifiableList(token_);
    }


    /**
     * Expand with substitutions
     * 
     * @param el
     *            the string to expand
     */
    private void expand(String el) {
        StringBuilder buf = new StringBuilder(el);
        int p = 0;
        while( true ) {
            // find a matching pair of braces
            int s = buf.indexOf("{", p);
            if( s == -1 ) break;
            int e = buf.indexOf("}", s);
            if( e == -1 ) break;

            // found a pair, un-escape its contents
            String k = Code.unescapeJava(buf.substring(s + 1, e - 1));
            // expand and replace if possible
            String r = replacements_.get(String.class, k);
            if( r != null ) {
                buf.replace(s, e, r);

                // don't advance as we have to recurse
                p = s;
            } else {
                // advance one
                p++;
            }
        }

        // split on colons to produce tokens
        List<String> work = split(buf.toString());
        for(String w:work) {
            token_.add(Code.unescapeJava(w));
        }
    }


    /**
     * Expand at the top level. At the top level an element may be replaced
     * by an entire list of strings. Below the top level, only string
     * replacement occurs.
     * 
     * @param t
     *            the top level string to expand
     */
    private void expandTopLevel(String t) {
        List<String> work = split(t);

        // each element, expand on "{..}"
        for(String el:work) {
            int s = el.indexOf('{');
            if( s == -1 ) {
                // no "{...}" so add literally
                token_.add(Code.unescapeJava(el));
                continue;
            }

            int e = (s != 0) ? -1 : el.indexOf('}', s);
            if( e == el.length() - 1 ) {
                // top level replacement, so accept string array
                String k = Code.unescapeJava(el.substring(s + 1, e));
                List<String> sa = replacements_.getList(String.class, k);
                if( sa != null ) {
                    for(String el2:sa) {
                        expandTopLevel(el2);
                    }
                    continue;
                }

                // no string array, but can still string replace at top
                // level
                String v = replacements_.get(String.class, k);
                if( v != null ) {
                    expandTopLevel(v);
                    continue;
                }

                // no replacement available so no expansion, insert as
                // literal
                token_.add(Code.unescapeJava(el));
                continue;
            }

            // expansion required at lower level
            expand(el);
        }
    }


    /** {@inheritDoc} */
    public boolean hasNext() {
        Boolean b;
        do {
            b = hasNextInternal();
        } while( b == null );
        return b.booleanValue();
    }


    private Boolean hasNextInternal() {
        while( !subTokenizers_.isEmpty() ) {
            CodeTokenizer ct = subTokenizers_.getFirst();
            boolean hn = ct.hasNext();
            if( hn ) return Boolean.TRUE;
            subTokenizers_.removeFirst();
        }

        token_.clear();

        // skip initial whitespace
        while( (position_ < src_.length())
                && Character.isWhitespace(src_.charAt(position_)) ) {
            position_++;
        }

        // if no characters left, no more tokens
        if( position_ >= src_.length() ) return Boolean.FALSE;

        // we have the start
        int start = position_;
        while( position_ < src_.length()
                && !Character.isWhitespace(src_.charAt(position_)) ) {
            position_++;
        }

        // have raw token
        String raw = src_.substring(start, position_);
        token_.add(raw);

        // special case - if we just have {...} then we have to tokenize
        // the expansion
        int s = raw.indexOf('{');
        int e = (s != 0) ? -1 : raw.indexOf('}');
        if( e == raw.length() - 1 ) {
            String k = Code.unescapeJava(raw.substring(0, e));
            List<String> rv = replacements_.getList(String.class, k);
            if( rv != null ) {
                for(String el:rv) {
                    subTokenizers_.add(new CodeTokenizer(el, replacements_));
                }
            } else {
                String rs = replacements_.get(String.class, k);
                subTokenizers_.add(new CodeTokenizer(rs, replacements_));
            }

            // call back to query sub-tokenizers so return null
            return null;
        }

        // expand the top level
        expandTopLevel(raw);

        return Boolean.TRUE;
    }


    /** {@inheritDoc} */
    public List<String> next() {
        if( subTokenizers_.isEmpty() ) {
            if( ret_.isEmpty() ) throw new NoSuchElementException();
            return ret_;
        }
        return subTokenizers_.getFirst().next();
    }


    /** Not supported */
    public void remove() {
        throw new UnsupportedOperationException();
    }


    /**
     * Split a string on ':'
     * 
     * @param t
     *            the string to split
     * @return a list of the substrings
     */
    private List<String> split(String t) {
        List<String> work = new ArrayList<String>();
        int s = 0;
        int e = t.indexOf(':', s);
        while( e != -1 ) {
            work.add(t.substring(s, e));
            s = e + 1;
            e = t.indexOf(':', s);
        }
        work.add(t.substring(s));
        return work;
    }

}