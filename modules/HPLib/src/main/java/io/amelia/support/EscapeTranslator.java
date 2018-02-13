/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

class EscapeTranslator
{
	private static final Map<String, String> BASIC_ESCAPE;
	private static final Map<String, String> HTML_ESCAPE;
	private static final Map<String, String> ISO8859_1_ESCAPE;
	private static final Map<String, String> JAVA_CTRL_CHARS_ESCAPE;

	static
	{
		Maps.MapBuilder<String, String> iso88591Builder = Maps.builder();
		iso88591Builder.put( "\u00A0", "&nbsp;" ); // non-breaking space
		iso88591Builder.put( "\u00A1", "&iexcl;" ); // inverted exclamation mark
		iso88591Builder.put( "\u00A2", "&cent;" ); // cent sign
		iso88591Builder.put( "\u00A3", "&pound;" ); // pound sign
		iso88591Builder.put( "\u00A4", "&curren;" ); // currency sign
		iso88591Builder.put( "\u00A5", "&yen;" ); // yen sign = yuan sign
		iso88591Builder.put( "\u00A6", "&brvbar;" ); // broken bar = broken vertical bar
		iso88591Builder.put( "\u00A7", "&sect;" ); // section sign
		iso88591Builder.put( "\u00A8", "&uml;" ); // diaeresis = spacing diaeresis
		iso88591Builder.put( "\u00A9", "&copy;" ); // � - copyright sign
		iso88591Builder.put( "\u00AA", "&ordf;" ); // feminine ordinal indicator
		iso88591Builder.put( "\u00AB", "&laquo;" ); // left-pointing double angle quotation mark = left pointing guillemet
		iso88591Builder.put( "\u00AC", "&not;" ); // not sign
		iso88591Builder.put( "\u00AD", "&shy;" ); // soft hyphen = discretionary hyphen
		iso88591Builder.put( "\u00AE", "&reg;" ); // � - registered trademark sign
		iso88591Builder.put( "\u00AF", "&macr;" ); // macron = spacing macron = overline = APL overbar
		iso88591Builder.put( "\u00B0", "&deg;" ); // degree sign
		iso88591Builder.put( "\u00B1", "&plusmn;" ); // plus-minus sign = plus-or-minus sign
		iso88591Builder.put( "\u00B2", "&sup2;" ); // superscript two = superscript digit two = squared
		iso88591Builder.put( "\u00B3", "&sup3;" ); // superscript three = superscript digit three = cubed
		iso88591Builder.put( "\u00B4", "&acute;" ); // acute accent = spacing acute
		iso88591Builder.put( "\u00B5", "&micro;" ); // micro sign
		iso88591Builder.put( "\u00B6", "&para;" ); // pilcrow sign = paragraph sign
		iso88591Builder.put( "\u00B7", "&middot;" ); // middle dot = Georgian comma = Greek middle dot
		iso88591Builder.put( "\u00B8", "&cedil;" ); // cedilla = spacing cedilla
		iso88591Builder.put( "\u00B9", "&sup1;" ); // superscript one = superscript digit one
		iso88591Builder.put( "\u00BA", "&ordm;" ); // masculine ordinal indicator
		iso88591Builder.put( "\u00BB", "&raquo;" ); // right-pointing double angle quotation mark = right pointing guillemet
		iso88591Builder.put( "\u00BC", "&frac14;" ); // vulgar fraction one quarter = fraction one quarter
		iso88591Builder.put( "\u00BD", "&frac12;" ); // vulgar fraction one half = fraction one half
		iso88591Builder.put( "\u00BE", "&frac34;" ); // vulgar fraction three quarters = fraction three quarters
		iso88591Builder.put( "\u00BF", "&iquest;" ); // inverted question mark = turned question mark
		iso88591Builder.put( "\u00C0", "&Agrave;" ); // � - uppercase A, grave accent
		iso88591Builder.put( "\u00C1", "&Aacute;" ); // � - uppercase A, acute accent
		iso88591Builder.put( "\u00C2", "&Acirc;" ); // � - uppercase A, circumflex accent
		iso88591Builder.put( "\u00C3", "&Atilde;" ); // � - uppercase A, tilde
		iso88591Builder.put( "\u00C4", "&Auml;" ); // � - uppercase A, umlaut
		iso88591Builder.put( "\u00C5", "&Aring;" ); // � - uppercase A, ring
		iso88591Builder.put( "\u00C6", "&AElig;" ); // � - uppercase AE
		iso88591Builder.put( "\u00C7", "&Ccedil;" ); // � - uppercase C, cedilla
		iso88591Builder.put( "\u00C8", "&Egrave;" ); // � - uppercase E, grave accent
		iso88591Builder.put( "\u00C9", "&Eacute;" ); // � - uppercase E, acute accent
		iso88591Builder.put( "\u00CA", "&Ecirc;" ); // � - uppercase E, circumflex accent
		iso88591Builder.put( "\u00CB", "&Euml;" ); // � - uppercase E, umlaut
		iso88591Builder.put( "\u00CC", "&Igrave;" ); // � - uppercase I, grave accent
		iso88591Builder.put( "\u00CD", "&Iacute;" ); // � - uppercase I, acute accent
		iso88591Builder.put( "\u00CE", "&Icirc;" ); // � - uppercase I, circumflex accent
		iso88591Builder.put( "\u00CF", "&Iuml;" ); // � - uppercase I, umlaut
		iso88591Builder.put( "\u00D0", "&ETH;" ); // � - uppercase Eth, Icelandic
		iso88591Builder.put( "\u00D1", "&Ntilde;" ); // � - uppercase N, tilde
		iso88591Builder.put( "\u00D2", "&Ograve;" ); // � - uppercase O, grave accent
		iso88591Builder.put( "\u00D3", "&Oacute;" ); // � - uppercase O, acute accent
		iso88591Builder.put( "\u00D4", "&Ocirc;" ); // � - uppercase O, circumflex accent
		iso88591Builder.put( "\u00D5", "&Otilde;" ); // � - uppercase O, tilde
		iso88591Builder.put( "\u00D6", "&Ouml;" ); // � - uppercase O, umlaut
		iso88591Builder.put( "\u00D7", "&times;" ); // multiplication sign
		iso88591Builder.put( "\u00D8", "&Oslash;" ); // � - uppercase O, slash
		iso88591Builder.put( "\u00D9", "&Ugrave;" ); // � - uppercase U, grave accent
		iso88591Builder.put( "\u00DA", "&Uacute;" ); // � - uppercase U, acute accent
		iso88591Builder.put( "\u00DB", "&Ucirc;" ); // � - uppercase U, circumflex accent
		iso88591Builder.put( "\u00DC", "&Uuml;" ); // � - uppercase U, umlaut
		iso88591Builder.put( "\u00DD", "&Yacute;" ); // � - uppercase Y, acute accent
		iso88591Builder.put( "\u00DE", "&THORN;" ); // � - uppercase THORN, Icelandic
		iso88591Builder.put( "\u00DF", "&szlig;" ); // � - lowercase sharps, German
		iso88591Builder.put( "\u00E0", "&agrave;" ); // � - lowercase a, grave accent
		iso88591Builder.put( "\u00E1", "&aacute;" ); // � - lowercase a, acute accent
		iso88591Builder.put( "\u00E2", "&acirc;" ); // � - lowercase a, circumflex accent
		iso88591Builder.put( "\u00E3", "&atilde;" ); // � - lowercase a, tilde
		iso88591Builder.put( "\u00E4", "&auml;" ); // � - lowercase a, umlaut
		iso88591Builder.put( "\u00E5", "&aring;" ); // � - lowercase a, ring
		iso88591Builder.put( "\u00E6", "&aelig;" ); // � - lowercase ae
		iso88591Builder.put( "\u00E7", "&ccedil;" ); // � - lowercase c, cedilla
		iso88591Builder.put( "\u00E8", "&egrave;" ); // � - lowercase e, grave accent
		iso88591Builder.put( "\u00E9", "&eacute;" ); // � - lowercase e, acute accent
		iso88591Builder.put( "\u00EA", "&ecirc;" ); // � - lowercase e, circumflex accent
		iso88591Builder.put( "\u00EB", "&euml;" ); // � - lowercase e, umlaut
		iso88591Builder.put( "\u00EC", "&igrave;" ); // � - lowercase i, grave accent
		iso88591Builder.put( "\u00ED", "&iacute;" ); // � - lowercase i, acute accent
		iso88591Builder.put( "\u00EE", "&icirc;" ); // � - lowercase i, circumflex accent
		iso88591Builder.put( "\u00EF", "&iuml;" ); // � - lowercase i, umlaut
		iso88591Builder.put( "\u00F0", "&eth;" ); // � - lowercase eth, Icelandic
		iso88591Builder.put( "\u00F1", "&ntilde;" ); // � - lowercase n, tilde
		iso88591Builder.put( "\u00F2", "&ograve;" ); // � - lowercase o, grave accent
		iso88591Builder.put( "\u00F3", "&oacute;" ); // � - lowercase o, acute accent
		iso88591Builder.put( "\u00F4", "&ocirc;" ); // � - lowercase o, circumflex accent
		iso88591Builder.put( "\u00F5", "&otilde;" ); // � - lowercase o, tilde
		iso88591Builder.put( "\u00F6", "&ouml;" ); // � - lowercase o, umlaut
		iso88591Builder.put( "\u00F7", "&divide;" ); // division sign
		iso88591Builder.put( "\u00F8", "&oslash;" ); // � - lowercase o, slash
		iso88591Builder.put( "\u00F9", "&ugrave;" ); // � - lowercase u, grave accent
		iso88591Builder.put( "\u00FA", "&uacute;" ); // � - lowercase u, acute accent
		iso88591Builder.put( "\u00FB", "&ucirc;" ); // � - lowercase u, circumflex accent
		iso88591Builder.put( "\u00FC", "&uuml;" ); // � - lowercase u, umlaut
		iso88591Builder.put( "\u00FD", "&yacute;" ); // � - lowercase y, acute accent
		iso88591Builder.put( "\u00FE", "&thorn;" ); // � - lowercase thorn, Icelandic
		iso88591Builder.put( "\u00FF", "&yuml;" ); // � - lowercase y, umlaut
		ISO8859_1_ESCAPE = iso88591Builder.hashMap();

		Maps.MapBuilder<String, String> htmlBuilder = Maps.builder();
		// <!-- Latin Extended-B -->
		htmlBuilder.put( "\u0192", "&fnof;" ); // latin small f with hook = function= florin, U+0192 ISOtech -->
		// <!-- Greek -->
		htmlBuilder.put( "\u0391", "&Alpha;" ); // greek capital letter alpha, U+0391 -->
		htmlBuilder.put( "\u0392", "&Beta;" ); // greek capital letter beta, U+0392 -->
		htmlBuilder.put( "\u0393", "&Gamma;" ); // greek capital letter gamma,U+0393 ISOgrk3 -->
		htmlBuilder.put( "\u0394", "&Delta;" ); // greek capital letter delta,U+0394 ISOgrk3 -->
		htmlBuilder.put( "\u0395", "&Epsilon;" ); // greek capital letter epsilon, U+0395 -->
		htmlBuilder.put( "\u0396", "&Zeta;" ); // greek capital letter zeta, U+0396 -->
		htmlBuilder.put( "\u0397", "&Eta;" ); // greek capital letter eta, U+0397 -->
		htmlBuilder.put( "\u0398", "&Theta;" ); // greek capital letter theta,U+0398 ISOgrk3 -->
		htmlBuilder.put( "\u0399", "&Iota;" ); // greek capital letter iota, U+0399 -->
		htmlBuilder.put( "\u039A", "&Kappa;" ); // greek capital letter kappa, U+039A -->
		htmlBuilder.put( "\u039B", "&Lambda;" ); // greek capital letter lambda,U+039B ISOgrk3 -->
		htmlBuilder.put( "\u039C", "&Mu;" ); // greek capital letter mu, U+039C -->
		htmlBuilder.put( "\u039D", "&Nu;" ); // greek capital letter nu, U+039D -->
		htmlBuilder.put( "\u039E", "&Xi;" ); // greek capital letter xi, U+039E ISOgrk3 -->
		htmlBuilder.put( "\u039F", "&Omicron;" ); // greek capital letter omicron, U+039F -->
		htmlBuilder.put( "\u03A0", "&Pi;" ); // greek capital letter pi, U+03A0 ISOgrk3 -->
		htmlBuilder.put( "\u03A1", "&Rho;" ); // greek capital letter rho, U+03A1 -->
		// <!-- there is no Sigmaf, and no U+03A2 character either -->
		htmlBuilder.put( "\u03A3", "&Sigma;" ); // greek capital letter sigma,U+03A3 ISOgrk3 -->
		htmlBuilder.put( "\u03A4", "&Tau;" ); // greek capital letter tau, U+03A4 -->
		htmlBuilder.put( "\u03A5", "&Upsilon;" ); // greek capital letter upsilon,U+03A5 ISOgrk3 -->
		htmlBuilder.put( "\u03A6", "&Phi;" ); // greek capital letter phi,U+03A6 ISOgrk3 -->
		htmlBuilder.put( "\u03A7", "&Chi;" ); // greek capital letter chi, U+03A7 -->
		htmlBuilder.put( "\u03A8", "&Psi;" ); // greek capital letter psi,U+03A8 ISOgrk3 -->
		htmlBuilder.put( "\u03A9", "&Omega;" ); // greek capital letter omega,U+03A9 ISOgrk3 -->
		htmlBuilder.put( "\u03B1", "&alpha;" ); // greek small letter alpha,U+03B1 ISOgrk3 -->
		htmlBuilder.put( "\u03B2", "&beta;" ); // greek small letter beta, U+03B2 ISOgrk3 -->
		htmlBuilder.put( "\u03B3", "&gamma;" ); // greek small letter gamma,U+03B3 ISOgrk3 -->
		htmlBuilder.put( "\u03B4", "&delta;" ); // greek small letter delta,U+03B4 ISOgrk3 -->
		htmlBuilder.put( "\u03B5", "&epsilon;" ); // greek small letter epsilon,U+03B5 ISOgrk3 -->
		htmlBuilder.put( "\u03B6", "&zeta;" ); // greek small letter zeta, U+03B6 ISOgrk3 -->
		htmlBuilder.put( "\u03B7", "&eta;" ); // greek small letter eta, U+03B7 ISOgrk3 -->
		htmlBuilder.put( "\u03B8", "&theta;" ); // greek small letter theta,U+03B8 ISOgrk3 -->
		htmlBuilder.put( "\u03B9", "&iota;" ); // greek small letter iota, U+03B9 ISOgrk3 -->
		htmlBuilder.put( "\u03BA", "&kappa;" ); // greek small letter kappa,U+03BA ISOgrk3 -->
		htmlBuilder.put( "\u03BB", "&lambda;" ); // greek small letter lambda,U+03BB ISOgrk3 -->
		htmlBuilder.put( "\u03BC", "&mu;" ); // greek small letter mu, U+03BC ISOgrk3 -->
		htmlBuilder.put( "\u03BD", "&nu;" ); // greek small letter nu, U+03BD ISOgrk3 -->
		htmlBuilder.put( "\u03BE", "&xi;" ); // greek small letter xi, U+03BE ISOgrk3 -->
		htmlBuilder.put( "\u03BF", "&omicron;" ); // greek small letter omicron, U+03BF NEW -->
		htmlBuilder.put( "\u03C0", "&pi;" ); // greek small letter pi, U+03C0 ISOgrk3 -->
		htmlBuilder.put( "\u03C1", "&rho;" ); // greek small letter rho, U+03C1 ISOgrk3 -->
		htmlBuilder.put( "\u03C2", "&sigmaf;" ); // greek small letter final sigma,U+03C2 ISOgrk3 -->
		htmlBuilder.put( "\u03C3", "&sigma;" ); // greek small letter sigma,U+03C3 ISOgrk3 -->
		htmlBuilder.put( "\u03C4", "&tau;" ); // greek small letter tau, U+03C4 ISOgrk3 -->
		htmlBuilder.put( "\u03C5", "&upsilon;" ); // greek small letter upsilon,U+03C5 ISOgrk3 -->
		htmlBuilder.put( "\u03C6", "&phi;" ); // greek small letter phi, U+03C6 ISOgrk3 -->
		htmlBuilder.put( "\u03C7", "&chi;" ); // greek small letter chi, U+03C7 ISOgrk3 -->
		htmlBuilder.put( "\u03C8", "&psi;" ); // greek small letter psi, U+03C8 ISOgrk3 -->
		htmlBuilder.put( "\u03C9", "&omega;" ); // greek small letter omega,U+03C9 ISOgrk3 -->
		htmlBuilder.put( "\u03D1", "&thetasym;" ); // greek small letter theta symbol,U+03D1 NEW -->
		htmlBuilder.put( "\u03D2", "&upsih;" ); // greek upsilon with hook symbol,U+03D2 NEW -->
		htmlBuilder.put( "\u03D6", "&piv;" ); // greek pi symbol, U+03D6 ISOgrk3 -->
		// <!-- General Punctuation -->
		htmlBuilder.put( "\u2022", "&bull;" ); // bullet = black small circle,U+2022 ISOpub -->
		// <!-- bullet is NOT the same as bullet operator, U+2219 -->
		htmlBuilder.put( "\u2026", "&hellip;" ); // horizontal ellipsis = three dot leader,U+2026 ISOpub -->
		htmlBuilder.put( "\u2032", "&prime;" ); // prime = minutes = feet, U+2032 ISOtech -->
		htmlBuilder.put( "\u2033", "&Prime;" ); // double prime = seconds = inches,U+2033 ISOtech -->
		htmlBuilder.put( "\u203E", "&oline;" ); // overline = spacing overscore,U+203E NEW -->
		htmlBuilder.put( "\u2044", "&frasl;" ); // fraction slash, U+2044 NEW -->
		// <!-- Letterlike Symbols -->
		htmlBuilder.put( "\u2118", "&weierp;" ); // script capital P = power set= Weierstrass p, U+2118 ISOamso -->
		htmlBuilder.put( "\u2111", "&image;" ); // blackletter capital I = imaginary part,U+2111 ISOamso -->
		htmlBuilder.put( "\u211C", "&real;" ); // blackletter capital R = real part symbol,U+211C ISOamso -->
		htmlBuilder.put( "\u2122", "&trade;" ); // trade mark sign, U+2122 ISOnum -->
		htmlBuilder.put( "\u2135", "&alefsym;" ); // alef symbol = first transfinite cardinal,U+2135 NEW -->
		// <!-- alef symbol is NOT the same as hebrew letter alef,U+05D0 although the
		// same glyph could be used to depict both characters -->
		// <!-- Arrows -->
		htmlBuilder.put( "\u2190", "&larr;" ); // leftwards arrow, U+2190 ISOnum -->
		htmlBuilder.put( "\u2191", "&uarr;" ); // upwards arrow, U+2191 ISOnum-->
		htmlBuilder.put( "\u2192", "&rarr;" ); // rightwards arrow, U+2192 ISOnum -->
		htmlBuilder.put( "\u2193", "&darr;" ); // downwards arrow, U+2193 ISOnum -->
		htmlBuilder.put( "\u2194", "&harr;" ); // left right arrow, U+2194 ISOamsa -->
		htmlBuilder.put( "\u21B5", "&crarr;" ); // downwards arrow with corner leftwards= carriage return, U+21B5 NEW -->
		htmlBuilder.put( "\u21D0", "&lArr;" ); // leftwards double arrow, U+21D0 ISOtech -->
		// <!-- ISO 10646 does not say that lArr is the same as the 'is implied by'
		// arrow but also does not have any other character for that function.
		// So ? lArr canbe used for 'is implied by' as ISOtech suggests -->
		htmlBuilder.put( "\u21D1", "&uArr;" ); // upwards double arrow, U+21D1 ISOamsa -->
		htmlBuilder.put( "\u21D2", "&rArr;" ); // rightwards double arrow,U+21D2 ISOtech -->
		// <!-- ISO 10646 does not say this is the 'implies' character but does not
		// have another character with this function so ?rArr can be used for
		// 'implies' as ISOtech suggests -->
		htmlBuilder.put( "\u21D3", "&dArr;" ); // downwards double arrow, U+21D3 ISOamsa -->
		htmlBuilder.put( "\u21D4", "&hArr;" ); // left right double arrow,U+21D4 ISOamsa -->
		// <!-- Mathematical Operators -->
		htmlBuilder.put( "\u2200", "&forall;" ); // for all, U+2200 ISOtech -->
		htmlBuilder.put( "\u2202", "&part;" ); // partial differential, U+2202 ISOtech -->
		htmlBuilder.put( "\u2203", "&exist;" ); // there exists, U+2203 ISOtech -->
		htmlBuilder.put( "\u2205", "&empty;" ); // empty set = null set = diameter,U+2205 ISOamso -->
		htmlBuilder.put( "\u2207", "&nabla;" ); // nabla = backward difference,U+2207 ISOtech -->
		htmlBuilder.put( "\u2208", "&isin;" ); // element of, U+2208 ISOtech -->
		htmlBuilder.put( "\u2209", "&notin;" ); // not an element of, U+2209 ISOtech -->
		htmlBuilder.put( "\u220B", "&ni;" ); // contains as member, U+220B ISOtech -->
		// <!-- should there be a more memorable name than 'ni'? -->
		htmlBuilder.put( "\u220F", "&prod;" ); // n-ary product = product sign,U+220F ISOamsb -->
		// <!-- prod is NOT the same character as U+03A0 'greek capital letter pi'
		// though the same glyph might be used for both -->
		htmlBuilder.put( "\u2211", "&sum;" ); // n-ary summation, U+2211 ISOamsb -->
		// <!-- sum is NOT the same character as U+03A3 'greek capital letter sigma'
		// though the same glyph might be used for both -->
		htmlBuilder.put( "\u2212", "&minus;" ); // minus sign, U+2212 ISOtech -->
		htmlBuilder.put( "\u2217", "&lowast;" ); // asterisk operator, U+2217 ISOtech -->
		htmlBuilder.put( "\u221A", "&radic;" ); // square root = radical sign,U+221A ISOtech -->
		htmlBuilder.put( "\u221D", "&prop;" ); // proportional to, U+221D ISOtech -->
		htmlBuilder.put( "\u221E", "&infin;" ); // infinity, U+221E ISOtech -->
		htmlBuilder.put( "\u2220", "&ang;" ); // angle, U+2220 ISOamso -->
		htmlBuilder.put( "\u2227", "&and;" ); // logical and = wedge, U+2227 ISOtech -->
		htmlBuilder.put( "\u2228", "&or;" ); // logical or = vee, U+2228 ISOtech -->
		htmlBuilder.put( "\u2229", "&cap;" ); // intersection = cap, U+2229 ISOtech -->
		htmlBuilder.put( "\u222A", "&cup;" ); // union = cup, U+222A ISOtech -->
		htmlBuilder.put( "\u222B", "&int;" ); // integral, U+222B ISOtech -->
		htmlBuilder.put( "\u2234", "&there4;" ); // therefore, U+2234 ISOtech -->
		htmlBuilder.put( "\u223C", "&sim;" ); // tilde operator = varies with = similar to,U+223C ISOtech -->
		// <!-- tilde operator is NOT the same character as the tilde, U+007E,although
		// the same glyph might be used to represent both -->
		htmlBuilder.put( "\u2245", "&cong;" ); // approximately equal to, U+2245 ISOtech -->
		htmlBuilder.put( "\u2248", "&asymp;" ); // almost equal to = asymptotic to,U+2248 ISOamsr -->
		htmlBuilder.put( "\u2260", "&ne;" ); // not equal to, U+2260 ISOtech -->
		htmlBuilder.put( "\u2261", "&equiv;" ); // identical to, U+2261 ISOtech -->
		htmlBuilder.put( "\u2264", "&le;" ); // less-than or equal to, U+2264 ISOtech -->
		htmlBuilder.put( "\u2265", "&ge;" ); // greater-than or equal to,U+2265 ISOtech -->
		htmlBuilder.put( "\u2282", "&sub;" ); // subset of, U+2282 ISOtech -->
		htmlBuilder.put( "\u2283", "&sup;" ); // superset of, U+2283 ISOtech -->
		// <!-- note that nsup, 'not a superset of, U+2283' is not covered by the
		// Symbol font encoding and is not included. Should it be, for symmetry?
		// It is in ISOamsn --> <!ENTITY nsub", "8836",
		// not a subset of, U+2284 ISOamsn -->
		htmlBuilder.put( "\u2286", "&sube;" ); // subset of or equal to, U+2286 ISOtech -->
		htmlBuilder.put( "\u2287", "&supe;" ); // superset of or equal to,U+2287 ISOtech -->
		htmlBuilder.put( "\u2295", "&oplus;" ); // circled plus = direct sum,U+2295 ISOamsb -->
		htmlBuilder.put( "\u2297", "&otimes;" ); // circled times = vector product,U+2297 ISOamsb -->
		htmlBuilder.put( "\u22A5", "&perp;" ); // up tack = orthogonal to = perpendicular,U+22A5 ISOtech -->
		htmlBuilder.put( "\u22C5", "&sdot;" ); // dot operator, U+22C5 ISOamsb -->
		// <!-- dot operator is NOT the same character as U+00B7 middle dot -->
		// <!-- Miscellaneous Technical -->
		htmlBuilder.put( "\u2308", "&lceil;" ); // left ceiling = apl upstile,U+2308 ISOamsc -->
		htmlBuilder.put( "\u2309", "&rceil;" ); // right ceiling, U+2309 ISOamsc -->
		htmlBuilder.put( "\u230A", "&lfloor;" ); // left floor = apl downstile,U+230A ISOamsc -->
		htmlBuilder.put( "\u230B", "&rfloor;" ); // right floor, U+230B ISOamsc -->
		htmlBuilder.put( "\u2329", "&lang;" ); // left-pointing angle bracket = bra,U+2329 ISOtech -->
		// <!-- lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation
		// mark' -->
		htmlBuilder.put( "\u232A", "&rang;" ); // right-pointing angle bracket = ket,U+232A ISOtech -->
		// <!-- rang is NOT the same character as U+003E 'greater than' or U+203A
		// 'single right-pointing angle quotation mark' -->
		// <!-- Geometric Shapes -->
		htmlBuilder.put( "\u25CA", "&loz;" ); // lozenge, U+25CA ISOpub -->
		// <!-- Miscellaneous Symbols -->
		htmlBuilder.put( "\u2660", "&spades;" ); // black spade suit, U+2660 ISOpub -->
		// <!-- black here seems to mean filled as opposed to hollow -->
		htmlBuilder.put( "\u2663", "&clubs;" ); // black club suit = shamrock,U+2663 ISOpub -->
		htmlBuilder.put( "\u2665", "&hearts;" ); // black heart suit = valentine,U+2665 ISOpub -->
		htmlBuilder.put( "\u2666", "&diams;" ); // black diamond suit, U+2666 ISOpub -->

		// <!-- Latin Extended-A -->
		htmlBuilder.put( "\u0152", "&OElig;" ); // -- latin capital ligature OE,U+0152 ISOlat2 -->
		htmlBuilder.put( "\u0153", "&oelig;" ); // -- latin small ligature oe, U+0153 ISOlat2 -->
		// <!-- ligature is a misnomer, this is a separate character in some languages -->
		htmlBuilder.put( "\u0160", "&Scaron;" ); // -- latin capital letter S with caron,U+0160 ISOlat2 -->
		htmlBuilder.put( "\u0161", "&scaron;" ); // -- latin small letter s with caron,U+0161 ISOlat2 -->
		htmlBuilder.put( "\u0178", "&Yuml;" ); // -- latin capital letter Y with diaeresis,U+0178 ISOlat2 -->
		// <!-- Spacing Modifier Letters -->
		htmlBuilder.put( "\u02C6", "&circ;" ); // -- modifier letter circumflex accent,U+02C6 ISOpub -->
		htmlBuilder.put( "\u02DC", "&tilde;" ); // small tilde, U+02DC ISOdia -->
		// <!-- General Punctuation -->
		htmlBuilder.put( "\u2002", "&ensp;" ); // en space, U+2002 ISOpub -->
		htmlBuilder.put( "\u2003", "&emsp;" ); // em space, U+2003 ISOpub -->
		htmlBuilder.put( "\u2009", "&thinsp;" ); // thin space, U+2009 ISOpub -->
		htmlBuilder.put( "\u200C", "&zwnj;" ); // zero width non-joiner,U+200C NEW RFC 2070 -->
		htmlBuilder.put( "\u200D", "&zwj;" ); // zero width joiner, U+200D NEW RFC 2070 -->
		htmlBuilder.put( "\u200E", "&lrm;" ); // left-to-right mark, U+200E NEW RFC 2070 -->
		htmlBuilder.put( "\u200F", "&rlm;" ); // right-to-left mark, U+200F NEW RFC 2070 -->
		htmlBuilder.put( "\u2013", "&ndash;" ); // en dash, U+2013 ISOpub -->
		htmlBuilder.put( "\u2014", "&mdash;" ); // em dash, U+2014 ISOpub -->
		htmlBuilder.put( "\u2018", "&lsquo;" ); // left single quotation mark,U+2018 ISOnum -->
		htmlBuilder.put( "\u2019", "&rsquo;" ); // right single quotation mark,U+2019 ISOnum -->
		htmlBuilder.put( "\u201A", "&sbquo;" ); // single low-9 quotation mark, U+201A NEW -->
		htmlBuilder.put( "\u201C", "&ldquo;" ); // left double quotation mark,U+201C ISOnum -->
		htmlBuilder.put( "\u201D", "&rdquo;" ); // right double quotation mark,U+201D ISOnum -->
		htmlBuilder.put( "\u201E", "&bdquo;" ); // double low-9 quotation mark, U+201E NEW -->
		htmlBuilder.put( "\u2020", "&dagger;" ); // dagger, U+2020 ISOpub -->
		htmlBuilder.put( "\u2021", "&Dagger;" ); // double dagger, U+2021 ISOpub -->
		htmlBuilder.put( "\u2030", "&permil;" ); // per mille sign, U+2030 ISOtech -->
		htmlBuilder.put( "\u2039", "&lsaquo;" ); // single left-pointing angle quotation mark,U+2039 ISO proposed -->
		// <!-- lsaquo is proposed but not yet ISO standardized -->
		htmlBuilder.put( "\u203A", "&rsaquo;" ); // single right-pointing angle quotation mark,U+203A ISO proposed -->
		// <!-- rsaquo is proposed but not yet ISO standardized -->
		htmlBuilder.put( "\u20AC", "&euro;" ); // -- euro sign, U+20AC NEW -->
		HTML_ESCAPE = htmlBuilder.hashMap();

		Maps.MapBuilder<String, String> basicBuilder = Maps.builder();
		basicBuilder.put( "\"", "&quot;" ); // " - double-quote
		basicBuilder.put( "&", "&amp;" ); // & - ampersand
		basicBuilder.put( "<", "&lt;" ); // < - less-than
		basicBuilder.put( ">", "&gt;" ); // > - greater-than
		BASIC_ESCAPE = basicBuilder.hashMap();

		Maps.MapBuilder<String, String> ctrlCharBuilder = Maps.builder();
		basicBuilder.put( "\b", "\\b" );
		basicBuilder.put( "\n", "\\n" );
		basicBuilder.put( "\t", "\\t" );
		basicBuilder.put( "\f", "\\f" );
		basicBuilder.put( "\r", "\\r" );
		JAVA_CTRL_CHARS_ESCAPE = ctrlCharBuilder.hashMap();
	}

	public static EscapeTranslator APOS_ESCAPE()
	{
		return new EscapeTranslator( Maps.builder( "'", "&apos;" ).hashMap() );
	}

	public static EscapeTranslator APOS_UNESCAPE()
	{
		return new EscapeTranslator( Maps.builder( "&apos;", "'" ).hashMap() );
	}

	public static EscapeTranslator BASIC_ESCAPE()
	{
		return new EscapeTranslator( BASIC_ESCAPE );
	}

	public static EscapeTranslator BASIC_UNESCAPE()
	{
		return new EscapeTranslator( Maps.flipKeyValue( BASIC_ESCAPE ) );
	}

	public static EscapeTranslator HTML_ESCAPE()
	{
		return new EscapeTranslator( ISO8859_1_ESCAPE, HTML_ESCAPE );
	}

	public static EscapeTranslator HTML_UNESCAPE()
	{
		return new EscapeTranslator( Maps.flipKeyValue( ISO8859_1_ESCAPE ), Maps.flipKeyValue( HTML_ESCAPE ) );
	}

	public static EscapeTranslator ISO8859_1_ESCAPE()
	{
		return new EscapeTranslator( ISO8859_1_ESCAPE );
	}

	public static EscapeTranslator ISO8859_1_UNESCAPE()
	{
		return new EscapeTranslator( Maps.flipKeyValue( ISO8859_1_ESCAPE ) );
	}

	public static EscapeTranslator JAVA_CTRL_CHARS_ESCAPE()
	{
		return new EscapeTranslator( JAVA_CTRL_CHARS_ESCAPE );
	}

	public static EscapeTranslator JAVA_CTRL_CHARS_UNESCAPE()
	{
		return new EscapeTranslator( Maps.flipKeyValue( JAVA_CTRL_CHARS_ESCAPE ) );
	}

	private final Map<String, CharSequence> escapes = new TreeMap<>( Comparator.comparingInt( s -> Math.abs( ( s ).length() ) ) );
	private final int longest;

	private EscapeTranslator( Map<String, String>... escapeArray )
	{
		for ( Map<String, String> map : escapeArray )
			escapes.putAll( map );

		longest = Maps.first( escapes ).map( CharSequence::length ).orElse( 0 );
	}

	public final String translate( final CharSequence input )
	{
		if ( input == null )
			return null;

		StringBuilder output = new StringBuilder();

		int pos = 0;
		final int len = input.length();

		while ( pos < len )
		{
			int consumed = 0;
			int max = pos + longest > input.length() ? input.length() - pos : longest;
			for ( int i = max; i >= 0; i-- )
			{
				final CharSequence subSeq = input.subSequence( pos, pos + i );
				final CharSequence result = escapes.get( subSeq );
				if ( result != null )
				{
					output.append( result );
					consumed = i;
					break;
				}
			}

			if ( consumed == 0 )
			{
				final char[] c = Character.toChars( Character.codePointAt( input, pos ) );
				output.append( c );
				pos += c.length;
			}
			else
				for ( int pt = 0; pt < consumed; pt++ )
					pos += Character.charCount( Character.codePointAt( input, pos ) );
		}

		return output.toString();
	}
}
