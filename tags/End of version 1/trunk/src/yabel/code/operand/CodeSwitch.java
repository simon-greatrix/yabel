package yabel.code.operand;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import yabel.ClassData;
import yabel.OpCodes;
import yabel.SwitchData;
import yabel.code.CodeOperand;
import yabel.code.CompilerOutput;
import yabel.code.YabelWrongTokenCountException;

/**
 * Compile switch related operations
 * 
 * @author Simon Greatrix
 * 
 */
public enum CodeSwitch implements CodeOperand {
    /** Either a LOOKUPSWITCH or a TABLESWITCH, whichever is smaller */
    SWITCH {
        /** {@inheritDoc} */
        @Override
        public void compile(CompilerOutput code, SwitchData data) {
            // Choose the smallest kind of switch.
            // Lookup has a value-label pair for each value
            int lookupSize = 8 * data.size();
            // Table has min, max and labels
            int tableSize = 4 * (1 + data.getMax() - data.getMin()) + 8;
            if( lookupSize < tableSize ) {
                LOOKUPSWITCH.compile(code, data);
            } else {
                TABLESWITCH.compile(code, data);
            }
        }
    },

    /** Compile a LOOKUPSWITCH statement */
    LOOKUPSWITCH {
        /** {@inheritDoc} */
        @Override
        public void compile(CompilerOutput code, SwitchData data) {
            code.appendU1(OpCodes.LOOKUPSWITCH);
            code.appendSwitchPadding();

            // default, value, label, value, label, ...
            String dflt = data.getDefault();
            code.compileJump(dflt, 4);
            code.appendS4(data.size());
            for(Entry<Integer, String> e:data) {
                code.appendS4(e.getKey().intValue());
                code.compileJump(e.getValue(), 4);
            }
        }
    },

    /** Compile a TABLESWITCH statement */
    TABLESWITCH {
        /** {@inheritDoc} */
        @Override
        public void compile(CompilerOutput code, SwitchData data) {
            code.appendU1(OpCodes.TABLESWITCH);
            code.appendSwitchPadding();

            // default, min, max, all labels
            String dflt = data.getDefault();
            code.compileJump(dflt, 4);

            int min = data.getMin();
            int max = data.getMax();
            code.appendS4(min);
            code.appendS4(max);
            for(int i = min;i <= max;i++) {
                Integer ii = Integer.valueOf(i);
                String slbl = data.get(ii);
                if( slbl == null ) {
                    // no lookup means default
                    code.compileJump(dflt, 4);
                } else {
                    code.compileJump(slbl, 4);
                }
            }
        }
    };

    /** {@inheritDoc} */
    @Override
    public void compile(CompilerOutput code, List<String> toks, ClassData cd) {
        SwitchData vals;
        if( toks.size() == 3 ) {
            // named SwitchData
            String p = toks.get(toks.size() - 1);
            vals = cd.getSafe(SwitchData.class, p);
        } else {
            // list of values and labels
            // raw switch default val1 lbl1 val2 lbl2 ...
            // so size must be an odd number
            if( (toks.size() % 2) == 0 ) {
                throw new YabelWrongTokenCountException(toks,
                        "<default> [<value> <label>]*");
            }

            Iterator<String> iter = toks.iterator();
            // skip raw
            iter.hasNext();
            iter.next();

            // skip switch
            iter.hasNext();
            iter.next();

            // get default
            iter.hasNext();
            vals = new SwitchData(iter.next());

            // read pairs
            while( iter.hasNext() ) {
                String value = iter.next();
                iter.hasNext();
                String label = iter.next();

                Integer i = Integer.valueOf(value);
                vals.add(i, label);
            }
        }

        compile(code, vals);
    }


    /**
     * Compile the actual switch statement
     * 
     * @param code
     *            the associated code block
     * @param data
     *            the switch data
     */
    protected abstract void compile(CompilerOutput code, SwitchData data);
}
