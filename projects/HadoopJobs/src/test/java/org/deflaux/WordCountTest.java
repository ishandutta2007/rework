package org.deflaux;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;

public class WordCountTest {

	@Test
	public void testNormalization() {
		assertEquals("foobar", testHelper(" Foobar!"));
	}
	
	String testHelper(String rawWord) {
	  	Matcher matcher = WordCount.MapClass.NORMALIZATION_PATTERN.matcher(rawWord.toLowerCase());
        return(matcher.replaceAll(""));
	}

}
