/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * This class enables to parse the command blocks as a string understandable by the shell
 * @author gwen
 *
 */
public class StringParser
{
	private final char QUOTE_DELIMITER='#';

	/** Class logger */
    private static final Logger LOG = Logger.getLogger(StringParser.class);
    private static final Pattern regex1 = Pattern.compile(".*[#\\\\]\\z",Pattern.DOTALL);
    private static final Pattern regex2_1 = Pattern.compile(".*[\\p{L}\\d]+\\s*\\.\\s*[\\p{L}\\d]+\\s*\\(\\s*\\z",Pattern.DOTALL);
    private static final Pattern regex2_2 = Pattern.compile(".*[\\p{L}\\d]+\\s*=\\s*ne\\w\\s*[A-Z][\\p{L}\\d]*\\s*\\(\\s*\\z",Pattern.DOTALL);
    private static final Pattern regex2_3 = Pattern.compile(".*[\\=!]?=\\s*\\z",Pattern.DOTALL);
    private static final Pattern regex2_4 = Pattern.compile(".*[\\+\\,]\\s*\\z",Pattern.DOTALL);
    private static final Pattern regex3 = Pattern.compile("\\A\\s*else.*",Pattern.DOTALL);


    /**
     * Creates a new empty string parser
     */
	public StringParser()
	{
	}

	/**
	 *  Return a String containing an inverted comma ("), escaped 'level' times
	 *  @param level
	 *  	the number of times the double quote is escaped
	 *  @return
	 *  	the string to return
	 *
	 */
	private String escapedDoubleQuote(int level)
	{
		int length = 1<<level;
		char[] result = new char[length];
		for (int i=0;i<length-1;i++)
		{
			result[i] = '\\';
		}
		result[length-1] = '\"';
		return new String(result);
	}

	/**
	 * This method removes the accents of a character.
	 */
	public char removeAccents(char character)
	{
		switch (character) {
			case 'à':
			case 'á':
			case 'â':
				return 'a';
			case 'À':
			case 'Á':
			case 'Â':
				return 'A';
			case 'é':
			case 'è':
			case 'ê':
			case 'ë':
				return 'e';
			case 'É':
			case 'È':
			case 'Ê':
			case 'Ë':
				return 'E';
			case 'í':
			case 'î':
			case 'ï':
				return 'i';
			case 'Í':
			case 'Î':
			case 'Ï':
				return 'I';
			case 'ó':
			case 'ô':
				return 'o';
			case 'Ó':
			case 'Ô':
				return 'O';
			case 'ù':
			case 'ú':
			case 'û':
				return 'u';
			case 'Ù':
			case 'Ú':
			case 'Û':
				return 'U';
			case 'Ç':
				return 'C';
			case 'ç':
				return 'c';
			case 'ñ':
				return 'n';
			case 'Ñ':
				return 'N';
		}
		return character;
	}

	/**
	 * This method creates a String of delimiters of a given number.
	 */
	public static String someDelimiters(int number)
	{
		String result = "";
		for (int i=0; i<number; i++)
			result += "#";
		return result;
	}

	/**
	 * This method adds to a String its quote delimiters. Indeed, each quotation mark (") must be preceded by a certain number of slashes (\).
	 * Each quotation mark that is already preceded by a slash receives no delimiter. It symbolizes the character '\"', and not a quote extremity.
	 * There are three possible cases:
			/* No we have to to add some sharp symbols.
			 * There are five possible cases:
			 * 1.    #"     There is already a sharp before the quotation mark. In this case, no more sharps should be added.
			 * 2.    \"     This indicates an isolated quotation mark. The level should not change.
			 * 3.    .CommandWord(" the level increases
			 *     NotCommandWord(" the level decreases
			 * 4.    ," or +"    the level increases
			 * 5.    ") or ", or +"    the level decreases
			 */
	static public String addQuoteDelimiters(String text)
	{
		try
		{
			String leftPart="";
			String newPart = "";
			int oldQuoteIndex=0;
			int newQuoteIndex = text.indexOf('\"', 0);
			int level=0;
			while (true) //we scan the String until we got sure there are no more quotation marks to find
			{
				//We search from the left the next index of a quotation mark (").

				//We stop if there is no more quotation mark (")
				if (newQuoteIndex == -1)
				{
					leftPart += text.substring(oldQuoteIndex, text.length());
					return leftPart;
				}

				//We add to the left part the text preceding this new quotation mark.
				newPart = text.substring(oldQuoteIndex, newQuoteIndex);
				leftPart += newPart;

				/* Now we have to to add some sharp symbols.
				 * There are three possible cases:
				 * 1.    #" or \"     There is already a sharp before the quotation mark. In this case, no more sharps should be added.
				 * 2.   The level increases when encountering the following patterns:
				 * 		object.method("
				 * 		object = new Class("
				 * 		= "	or == " or != "
				 * 		," or +"
				 * 3.	otherwise : the level decreases
				 */



				if (regex1.matcher(newPart).matches())
				{
					// We ignore the quotation marks that have already some sharps or backslashes.
					// do nothing
				}
				else if ((regex2_1.matcher(newPart).matches())||(regex2_2.matcher(newPart).matches())||(regex2_3.matcher(newPart).matches())||(regex2_4.matcher(newPart).matches()))
				{
					leftPart += someDelimiters(level);
					level++;
				}
				else if (level>0)
				{
			  	  	level--;
			  	  	leftPart += someDelimiters(level); //We add the expected number of slashes.
				}
				else // if level was to 0, increase it (abnormal case)
				{
					leftPart += someDelimiters(level);
					level++;
				}

				//We set the old index that will let us know from where there is some text to copy.
				oldQuoteIndex = newQuoteIndex;
				newQuoteIndex = text.indexOf('\"', oldQuoteIndex+1);
			}
		}
		catch (Exception e)
		{
			LOG.error("addQuoteDelimiters error: "+e.getMessage());
			return text;
		}
	}

	/**
	 * This method permits to replace quote delimiters (#) by a quotation mark (")
	 * It allows to call a string in an another string (this happens quite often in Tangara).
	 * Ex:  in Tangara:  object.method("object2.methode2(#"...#")) <br>
	 * 		in Java:     object.method("object2.methode(\"...\"))
	 * @param string
	 * 		the string to change
	 * @return
	 * 		the new string
	 */
	public String parseQuotes(String string)
	{
		String result = "";
		char[] charactersTab = string.toCharArray();
		boolean delimiterEncountered = false;
		boolean escapeEncountered = false;
		boolean removeAccents = true;
		int level = 0;
		char character;

		for (int i=0; i<string.length(); i++)
		{
			character = charactersTab[i];
			switch (character)
			{
				case QUOTE_DELIMITER:
					delimiterEncountered = true;
					level++;
					break;
				case '\\':
					escapeEncountered = true;
					result += character;
					break;
				case '"':
					if (delimiterEncountered)
					{
						result+=escapedDoubleQuote(level);
						delimiterEncountered = false;
						level = 0;
					}
					else if (escapeEncountered)
					{
						// Since escape character was encountered before quote, this is not a level 0 quote
						// so we do not switch "removeAccent"
						result += character;
						escapeEncountered = false;
					}
					else
					{   //We have a quotation mark (") of level 0,
						//so we stop removing accents, or we restart removing them.
						if (removeAccents) removeAccents = false;
						else removeAccents = true;
						result += character;
					}
					break;

				default:
					if (delimiterEncountered)
					{
						// false alarm : write the delimiters to result
						for (int j=0;j<level;j++)
							result+=QUOTE_DELIMITER;
						delimiterEncountered = false;
						level = 0;
					}
					escapeEncountered = false;
					if (removeAccents)
						character = removeAccents(character);

					result += character;
					break;
			}
		}
		return result;
	}

	/**
	 * This method says if there is an else in a string after the given position
	 * @param string
	 * 		the string to split as blocks of commands
	 * @param position
	 *      the position from which we have to search an else
	 * @return
	 * 		whether or not there is a following else
	 */
	public boolean else_follows(char[] string, int position)
	{
		int i = position+1;
		if (i >= string.length) return false;

		while (   string[i] == ' '  ||  string[i] == '\t'
			   || string[i] == '\n' ||  string[i] == '\r'  )
		{
			i++;
			if (i >= string.length) return false;
		}

		if ( string.length - i >= 4 &&
			 string[i] == 'e' && string[i+1] == 'l' && string[i+2] == 's' && string[i+3] == 'e' )
			return true;
		return false;
	}


	private boolean else_follows(String text, int index) {
		String subText = text.substring(index);
		return regex3.matcher(subText).matches();
	}

	private void skipEOL(StringCharacterIterator iterator) {
		char c = iterator.next();
		while (c != StringCharacterIterator.DONE && Character.isWhitespace(c) && c !='\n') {
			c = iterator.next();
		}
		if (c !='\n')
			iterator.previous();
	}

	private void skipComment(StringCharacterIterator iterator) {
		char c = iterator.next();
		boolean timesReached = false;
		while (c != StringCharacterIterator.DONE && !(timesReached&&(c=='/'))) {
			timesReached = (c == '*');
			c = iterator.next();
		}
	}


	/**
	 * Splits the commands in blocks of commands
	 * @param string
	 * 		the string to split a blocks of commands
	 * @return
	 * 		a list of blocks
	 */
	public ArrayList<String> splitBlocks(String string) {
		ArrayList<String> blocks = new ArrayList<String>();
		StringCharacterIterator iterator = new StringCharacterIterator(string);
	    boolean inString = false;
	    boolean escape = false;
	    int blockLevel = 0;
	    int bracketsLevel = 0;
	    int parenthesisLevel = 0;
		int startIndex = 0;

		for(char c = iterator.first(); c != StringCharacterIterator.DONE; c = iterator.next()) {
	    	 switch (c) {
	    	 	case '\"' : // String
	    	 		if (!escape)
	    	 			inString = !inString;
	    	 		break;
				case '[' :
					if (!inString)
						bracketsLevel++;
					break;
				case ']' :
					if (!inString)
						bracketsLevel--;
					break;
				case '(' :
					if (!inString)
						parenthesisLevel++;
					break;
				case ')' :
					if (!inString)
						parenthesisLevel--;
					break;
	    	 	case ';' :
					if ( inString || blockLevel>0 || bracketsLevel>0 || parenthesisLevel>0)
						break;
					//	We add a new command only if there is not an "else" following an "if".
					if (!else_follows(string, iterator.getIndex()+1)) {
						blocks.add(string.substring(startIndex, iterator.getIndex()+1));
						skipEOL(iterator);
						startIndex = iterator.getIndex()+1;
					}
					break;
				case '{' :
					if ( inString || bracketsLevel>0 || parenthesisLevel>0)
						break;
					blockLevel++;
					break;
				case '/' :
					if ( !inString ) {
						try {
							switch(string.charAt(iterator.getIndex()+1)){
								case '/':
								case '*':
									skipComment(iterator);
									break;
							}
						} catch (IndexOutOfBoundsException e) {
							// end of string reached
						}
					}
					break;
				case '}' :
					if ( inString || bracketsLevel>0 || parenthesisLevel>0)
						break;
					blockLevel--;
					// In case there are problems in the code, set block level to 0
					if (blockLevel < 0)
						blockLevel = 0;

					if (blockLevel == 0) {
						// We add the block only if there is not an "else" following the block
						if (!else_follows(string, iterator.getIndex()+1)) {
							blocks.add(string.substring(startIndex, iterator.getIndex()+1));
							skipEOL(iterator);
							startIndex = iterator.getIndex()+1;
						}
						break;
					}
	    	}
			if ((c == QUOTE_DELIMITER) || (c == '\\')) {
				escape = true;
			} else {
				escape = false;
			}
		}
		if (startIndex < iterator.getEndIndex())
			blocks.add(string.substring(startIndex));
		return blocks;
	}

	/**
	 * Splits the commands into single ones
	 * This method serves for the history.
	 * @param string
	 * 		the string to parse in commands line
	 * @return
	 * 		an array of commands line
	 */
	public ArrayList<String> splitCommands(String string, boolean keepSpaces) {
		ArrayList<String> commands = new ArrayList<String>();
		if (!keepSpaces) {
			string = string.replace("\n", "");
			string = string.replace("\t", "");
		}
		char[] characters = string.toCharArray();
		boolean inString = false;
		boolean escape = false;
		boolean commandInserted = false;
		boolean isComment = false;
		int bracketsLevel = 0;
		int startIndex = 0;
		for (int i=0;i<characters.length;i++) {
			switch(characters[i]) {
				case '/':
					if(characters[i+1] == '/'){
						isComment = true;
					}
					break;
				case '[' :
				case '(' :
					if (!inString)
						bracketsLevel++;
					escape = false;
					commandInserted = false;
					break;
				case ']' :
				case ')' :
					if (!inString)
						bracketsLevel--;
					escape = false;
					commandInserted = false;
					break;
				case  '\"' :
					if (!escape) {
						inString = !inString;
					}
					escape = false;
					commandInserted = false;
					break;
				case '\\' :
				case QUOTE_DELIMITER :
					escape = true;
					commandInserted = false;
					break;
				case ' ':
				case '\t':
				case '\r':
					if (commandInserted) {
						startIndex=i+1;
					}
					break;
				case '\n' : // only occurs if keepSpaces = true
					if (! commandInserted) {
						commands.add(string.substring(startIndex, i));
						startIndex = i+1;
					} else {
						startIndex = i+1;
					}
					commandInserted = false;
					break;
				case ';' :
					if (bracketsLevel>0) {
						escape = false;
						commandInserted = false;
						break;
					}
				case '{' :
				case '}' :
					if (!inString && !isComment) {
						commands.add(string.substring(startIndex, i+1));
						commandInserted = true;
						startIndex = i+1;
					}
					escape = false;
					break;
				default :
					escape = false;
					commandInserted = false;
					break;
			}
		}
		if (startIndex<characters.length) {
			commands.add(string.substring(startIndex));
		}
		return commands;
	}


}
