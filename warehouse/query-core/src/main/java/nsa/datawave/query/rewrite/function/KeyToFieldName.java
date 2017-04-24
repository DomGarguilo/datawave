package nsa.datawave.query.rewrite.function;

import java.nio.charset.CharacterCodingException;
import java.util.Map.Entry;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Function to transform the attributes for an event as {@code Entry<Key,Value>} to {@code Entry<Key,String>} where the Entry's value is the fieldName from the
 * Key <br>
 * 
 * 
 * 
 */
public class KeyToFieldName implements Function<Entry<Key,Value>,Entry<Key,String>> {
    
    private boolean includeGroupingContext = false;
    
    public KeyToFieldName() {}
    
    public KeyToFieldName(Boolean includeGroupingContext) {
        this.includeGroupingContext = includeGroupingContext;
    }
    
    @Override
    public Entry<Key,String> apply(Entry<Key,Value> from) {
        String fieldName = getFieldName(from.getKey());
        
        return Maps.immutableEntry(from.getKey(), fieldName);
    }
    
    public String getFieldName(Key k) {
        
        int index = -1;
        
        ByteSequence sequence = k.getColumnQualifierData();
        
        byte[] arrayReference = sequence.getBackingArray();
        
        for (int i = 0; i < sequence.length(); i++) {
            if (!includeGroupingContext && arrayReference[i] == '.') {
                index = i;
                break;
            }
            if (arrayReference[i] == 0x00) {
                index = i;
                break;
            }
        }
        
        if (0 > index) {
            throw new IllegalArgumentException("Could not find null-byte contained in columnqualifier for key: " + k);
        }
        
        try {
            String fieldName = Text.decode(arrayReference, 0, index);
            
            return fieldName;
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
