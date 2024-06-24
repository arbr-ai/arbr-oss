// Generated from /home/bill/arbr/arbr-oss/platform/arbr-platform-base/src/main/antlr/json_lenient/JsonLenientLexer.g4 by ANTLR 4.13.1
package com.topdown.parsers.lang.json_lenient.base;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class JsonLenientLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OpenBracket=1, CloseBracket=2, OpenParen=3, CloseParen=4, OpenBrace=5, 
		CloseBrace=6, Comma=7, Colon=8, TRUE=9, FALSE=10, NULL=11, STRING=12, 
		NUMBER=13, MultiLineComment=14, SingleLineComment=15, WS=16;
	public static final int
		COMMENT=2;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN", "COMMENT"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"OpenBracket", "CloseBracket", "OpenParen", "CloseParen", "OpenBrace", 
			"CloseBrace", "Comma", "Colon", "TRUE", "FALSE", "NULL", "STRING", "NUMBER", 
			"MultiLineComment", "SingleLineComment", "ESC", "UNICODE", "HEX", "SAFECODEPOINT", 
			"INT", "EXP", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'", "'('", "')'", "'{'", "'}'", "','", "':'", "'true'", 
			"'false'", "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "OpenBracket", "CloseBracket", "OpenParen", "CloseParen", "OpenBrace", 
			"CloseBrace", "Comma", "Colon", "TRUE", "FALSE", "NULL", "STRING", "NUMBER", 
			"MultiLineComment", "SingleLineComment", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JsonLenientLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JsonLenientLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0010\u00a8\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0005\u000bQ\b\u000b\n\u000b\f\u000bT\t\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0003\fY\b\f\u0001\f\u0001\f\u0001\f"+
		"\u0004\f^\b\f\u000b\f\f\f_\u0003\fb\b\f\u0001\f\u0003\fe\b\f\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0005\rk\b\r\n\r\f\rn\t\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e"+
		"y\b\u000e\n\u000e\f\u000e|\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0003\u000f\u0083\b\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013"+
		"\u0092\b\u0013\n\u0013\f\u0013\u0095\t\u0013\u0003\u0013\u0097\b\u0013"+
		"\u0001\u0014\u0001\u0014\u0003\u0014\u009b\b\u0014\u0001\u0014\u0004\u0014"+
		"\u009e\b\u0014\u000b\u0014\f\u0014\u009f\u0001\u0015\u0004\u0015\u00a3"+
		"\b\u0015\u000b\u0015\f\u0015\u00a4\u0001\u0015\u0001\u0015\u0001l\u0000"+
		"\u0016\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006"+
		"\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e"+
		"\u001d\u000f\u001f\u0000!\u0000#\u0000%\u0000\'\u0000)\u0000+\u0010\u0001"+
		"\u0000\t\u0001\u000009\u0003\u0000\n\n\r\r\u2028\u2029\b\u0000\"\"//\\"+
		"\\bbffnnrrtt\u0003\u000009AFaf\u0003\u0000\u0000\u001f\"\"\\\\\u0001\u0000"+
		"19\u0002\u0000EEee\u0002\u0000++--\u0003\u0000\t\n\r\r  \u00af\u0000\u0001"+
		"\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005"+
		"\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000"+
		"\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000"+
		"\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000"+
		"\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0001-\u0001\u0000\u0000\u0000"+
		"\u0003/\u0001\u0000\u0000\u0000\u00051\u0001\u0000\u0000\u0000\u00073"+
		"\u0001\u0000\u0000\u0000\t5\u0001\u0000\u0000\u0000\u000b7\u0001\u0000"+
		"\u0000\u0000\r9\u0001\u0000\u0000\u0000\u000f;\u0001\u0000\u0000\u0000"+
		"\u0011=\u0001\u0000\u0000\u0000\u0013B\u0001\u0000\u0000\u0000\u0015H"+
		"\u0001\u0000\u0000\u0000\u0017M\u0001\u0000\u0000\u0000\u0019X\u0001\u0000"+
		"\u0000\u0000\u001bf\u0001\u0000\u0000\u0000\u001dt\u0001\u0000\u0000\u0000"+
		"\u001f\u007f\u0001\u0000\u0000\u0000!\u0084\u0001\u0000\u0000\u0000#\u008a"+
		"\u0001\u0000\u0000\u0000%\u008c\u0001\u0000\u0000\u0000\'\u0096\u0001"+
		"\u0000\u0000\u0000)\u0098\u0001\u0000\u0000\u0000+\u00a2\u0001\u0000\u0000"+
		"\u0000-.\u0005[\u0000\u0000.\u0002\u0001\u0000\u0000\u0000/0\u0005]\u0000"+
		"\u00000\u0004\u0001\u0000\u0000\u000012\u0005(\u0000\u00002\u0006\u0001"+
		"\u0000\u0000\u000034\u0005)\u0000\u00004\b\u0001\u0000\u0000\u000056\u0005"+
		"{\u0000\u00006\n\u0001\u0000\u0000\u000078\u0005}\u0000\u00008\f\u0001"+
		"\u0000\u0000\u00009:\u0005,\u0000\u0000:\u000e\u0001\u0000\u0000\u0000"+
		";<\u0005:\u0000\u0000<\u0010\u0001\u0000\u0000\u0000=>\u0005t\u0000\u0000"+
		">?\u0005r\u0000\u0000?@\u0005u\u0000\u0000@A\u0005e\u0000\u0000A\u0012"+
		"\u0001\u0000\u0000\u0000BC\u0005f\u0000\u0000CD\u0005a\u0000\u0000DE\u0005"+
		"l\u0000\u0000EF\u0005s\u0000\u0000FG\u0005e\u0000\u0000G\u0014\u0001\u0000"+
		"\u0000\u0000HI\u0005n\u0000\u0000IJ\u0005u\u0000\u0000JK\u0005l\u0000"+
		"\u0000KL\u0005l\u0000\u0000L\u0016\u0001\u0000\u0000\u0000MR\u0005\"\u0000"+
		"\u0000NQ\u0003\u001f\u000f\u0000OQ\u0003%\u0012\u0000PN\u0001\u0000\u0000"+
		"\u0000PO\u0001\u0000\u0000\u0000QT\u0001\u0000\u0000\u0000RP\u0001\u0000"+
		"\u0000\u0000RS\u0001\u0000\u0000\u0000SU\u0001\u0000\u0000\u0000TR\u0001"+
		"\u0000\u0000\u0000UV\u0005\"\u0000\u0000V\u0018\u0001\u0000\u0000\u0000"+
		"WY\u0005-\u0000\u0000XW\u0001\u0000\u0000\u0000XY\u0001\u0000\u0000\u0000"+
		"YZ\u0001\u0000\u0000\u0000Za\u0003\'\u0013\u0000[]\u0005.\u0000\u0000"+
		"\\^\u0007\u0000\u0000\u0000]\\\u0001\u0000\u0000\u0000^_\u0001\u0000\u0000"+
		"\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`b\u0001\u0000"+
		"\u0000\u0000a[\u0001\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000bd\u0001"+
		"\u0000\u0000\u0000ce\u0003)\u0014\u0000dc\u0001\u0000\u0000\u0000de\u0001"+
		"\u0000\u0000\u0000e\u001a\u0001\u0000\u0000\u0000fg\u0005/\u0000\u0000"+
		"gh\u0005*\u0000\u0000hl\u0001\u0000\u0000\u0000ik\t\u0000\u0000\u0000"+
		"ji\u0001\u0000\u0000\u0000kn\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000"+
		"\u0000lj\u0001\u0000\u0000\u0000mo\u0001\u0000\u0000\u0000nl\u0001\u0000"+
		"\u0000\u0000op\u0005*\u0000\u0000pq\u0005/\u0000\u0000qr\u0001\u0000\u0000"+
		"\u0000rs\u0006\r\u0000\u0000s\u001c\u0001\u0000\u0000\u0000tu\u0005/\u0000"+
		"\u0000uv\u0005/\u0000\u0000vz\u0001\u0000\u0000\u0000wy\b\u0001\u0000"+
		"\u0000xw\u0001\u0000\u0000\u0000y|\u0001\u0000\u0000\u0000zx\u0001\u0000"+
		"\u0000\u0000z{\u0001\u0000\u0000\u0000{}\u0001\u0000\u0000\u0000|z\u0001"+
		"\u0000\u0000\u0000}~\u0006\u000e\u0000\u0000~\u001e\u0001\u0000\u0000"+
		"\u0000\u007f\u0082\u0005\\\u0000\u0000\u0080\u0083\u0007\u0002\u0000\u0000"+
		"\u0081\u0083\u0003!\u0010\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0082"+
		"\u0081\u0001\u0000\u0000\u0000\u0083 \u0001\u0000\u0000\u0000\u0084\u0085"+
		"\u0005u\u0000\u0000\u0085\u0086\u0003#\u0011\u0000\u0086\u0087\u0003#"+
		"\u0011\u0000\u0087\u0088\u0003#\u0011\u0000\u0088\u0089\u0003#\u0011\u0000"+
		"\u0089\"\u0001\u0000\u0000\u0000\u008a\u008b\u0007\u0003\u0000\u0000\u008b"+
		"$\u0001\u0000\u0000\u0000\u008c\u008d\b\u0004\u0000\u0000\u008d&\u0001"+
		"\u0000\u0000\u0000\u008e\u0097\u00050\u0000\u0000\u008f\u0093\u0007\u0005"+
		"\u0000\u0000\u0090\u0092\u0007\u0000\u0000\u0000\u0091\u0090\u0001\u0000"+
		"\u0000\u0000\u0092\u0095\u0001\u0000\u0000\u0000\u0093\u0091\u0001\u0000"+
		"\u0000\u0000\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u0097\u0001\u0000"+
		"\u0000\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0096\u008e\u0001\u0000"+
		"\u0000\u0000\u0096\u008f\u0001\u0000\u0000\u0000\u0097(\u0001\u0000\u0000"+
		"\u0000\u0098\u009a\u0007\u0006\u0000\u0000\u0099\u009b\u0007\u0007\u0000"+
		"\u0000\u009a\u0099\u0001\u0000\u0000\u0000\u009a\u009b\u0001\u0000\u0000"+
		"\u0000\u009b\u009d\u0001\u0000\u0000\u0000\u009c\u009e\u0007\u0000\u0000"+
		"\u0000\u009d\u009c\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000"+
		"\u0000\u009f\u009d\u0001\u0000\u0000\u0000\u009f\u00a0\u0001\u0000\u0000"+
		"\u0000\u00a0*\u0001\u0000\u0000\u0000\u00a1\u00a3\u0007\b\u0000\u0000"+
		"\u00a2\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000"+
		"\u00a4\u00a2\u0001\u0000\u0000\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000"+
		"\u00a5\u00a6\u0001\u0000\u0000\u0000\u00a6\u00a7\u0006\u0015\u0001\u0000"+
		"\u00a7,\u0001\u0000\u0000\u0000\u000f\u0000PRX_adlz\u0082\u0093\u0096"+
		"\u009a\u009f\u00a4\u0002\u0000\u0002\u0000\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}