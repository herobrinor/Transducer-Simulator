package test;

import simulator.*;

// for testing
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

// Unit tests against Decoder.java
public class DecoderTest {
    
    // global Decoder; set by constructor
    Decoder d;

    public DecoderTest() {
        d = new Decoder();
    }

    @Test
    public void test_decodeTDFT() {
        String encoding = "({q0,q1},{a,b},{b},{(q0,a,b,q0,1),(q0,b,b,q0,1),(q0,&,,q1,0)},{q0},{q1})";
        d.decodeTDFT(encoding);
        assertTrue(false);
    }
}
