// Generated from /Users/howechen/GitHub/wecube-platform/platform-core/src/main/resources/DataModel.g4 by ANTLR 4.7.2
package com.webank.wecube.platform.core.parser.datamodel.generated;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DataModelLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TILDE=1, DASH=2, DOT=3, LP=4, RP=5, DC=6, SC=7, DQM=8, ID=9, PKG_ID=10, 
		WS=11;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"TILDE", "DASH", "DOT", "LP", "RP", "DC", "SC", "DQM", "ID", "PKG_ID", 
			"Letter", "Digit", "LetterOrDigit", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'~'", "'-'", "'.'", "'('", "')'", null, "':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TILDE", "DASH", "DOT", "LP", "RP", "DC", "SC", "DQM", "ID", "PKG_ID", 
			"WS"
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


	public DataModelLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DataModel.g4"; }

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
			"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\rI\b\1\4\2\t\2\4" +
					"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t" +
					"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3" +
					"\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\7\n\64\n\n\f\n\16\n" +
					"\67\13\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\5\16A\n\16\3\17\6\17D\n\17" +
					"\r\17\16\17E\3\17\3\17\2\2\20\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13" +
					"\25\f\27\2\31\2\33\2\35\r\3\2\5\b\2##%(,,B\\`ac|\3\2\62;\5\2\13\f\17\17" +
					"\"\"\2H\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2" +
					"\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\35\3" +
					"\2\2\2\3\37\3\2\2\2\5!\3\2\2\2\7#\3\2\2\2\t%\3\2\2\2\13\'\3\2\2\2\r)\3" +
					"\2\2\2\17,\3\2\2\2\21.\3\2\2\2\23\61\3\2\2\2\258\3\2\2\2\27:\3\2\2\2\31" +
					"<\3\2\2\2\33@\3\2\2\2\35C\3\2\2\2\37 \7\u0080\2\2 \4\3\2\2\2!\"\7/\2\2" +
					"\"\6\3\2\2\2#$\7\60\2\2$\b\3\2\2\2%&\7*\2\2&\n\3\2\2\2\'(\7+\2\2(\f\3" +
					"\2\2\2)*\7<\2\2*+\7<\2\2+\16\3\2\2\2,-\7<\2\2-\20\3\2\2\2./\7$\2\2/\60" +
					"\7$\2\2\60\22\3\2\2\2\61\65\5\27\f\2\62\64\5\33\16\2\63\62\3\2\2\2\64" +
					"\67\3\2\2\2\65\63\3\2\2\2\65\66\3\2\2\2\66\24\3\2\2\2\67\65\3\2\2\289" +
					"\5\23\n\29\26\3\2\2\2:;\t\2\2\2;\30\3\2\2\2<=\t\3\2\2=\32\3\2\2\2>A\5" +
					"\27\f\2?A\5\31\r\2@>\3\2\2\2@?\3\2\2\2A\34\3\2\2\2BD\t\4\2\2CB\3\2\2\2" +
					"DE\3\2\2\2EC\3\2\2\2EF\3\2\2\2FG\3\2\2\2GH\b\17\2\2H\36\3\2\2\2\6\2\65" +
					"@E\3\b\2\2";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}