package org.colombbus.tangara;

import java.io.InputStream;
import java.io.Reader;

import bsh.Parser;
import bsh.ParserTokenManager;

public class ScriptParser extends Parser {

	public ScriptParser(InputStream input) {
		super(input);
	}

	public ScriptParser(Reader reader) {
		super(reader);
	}

	public ScriptParser(ParserTokenManager tokenMgr) {
		super(tokenMgr);
	}

	
}
