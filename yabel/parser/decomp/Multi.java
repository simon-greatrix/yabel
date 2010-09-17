package yabel.parser.decomp;

/**
 * A source block consisting of more than one component
 * 
 * @author Simon Greatrix
 */
public class Multi implements Source {
    /** The source components */
    private final Source[] source_;


    /**
     * Create new multi
     * 
     * @param source
     *            the component sources
     */
    public Multi(Object... source) {
        source_ = new Source[source.length];
        for(int i=0;i<source.length;i++) {
            Object o = source[i];
            if( o instanceof Source ) {
                source_[i] = (Source) o;
            } else {
                source_[i] = new Simple(String.valueOf(o));
            }
        }
    }


    /**
     * Get concatenated source code. {@inheritDoc}
     * 
     * @see yabel.parser.decomp.Source#source()
     */
    public String source() {
        StringBuilder buf = new StringBuilder();
        for(int i=0;i<source_.length;i++) {
            Source s = source_[i];
            if( i>0 ) buf.append(':');
            buf.append(s.source());
        }
        return buf.toString();
    }
}