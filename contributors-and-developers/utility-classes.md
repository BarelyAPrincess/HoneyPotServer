# Utility Classes

## io.amelia.support.Strs

```java
// Convert array of bytes to ASCII string
String bytesToStringASCII( byte[] bytes )
// Convert array of bytes to UTF String
String bytesToStringUTF( byte[] bytes )
// Capitalize words using space as a delimiter
String capitalizeWords( String str )
// Capitalize words using the specified delimiter
String capitalizeWords( String str, char delimiter )
// Same as capitalizeWords except forces lower case first
String capitalizeWordsFully( String str )
// Same as capitalizeWords except forces lower case first
String capitalizeWordsFully( String str, char delimiter )
// Returns character code for char at specified index
int codePointAt( char[] chars, int index )
// Compares two arrays of objects and returns true if any are contained in the other
boolean comparable( Object[] arrayLeft, Object[] arrayRight )
// Copies all elements from the iterable collection of originals to the collection provided
T extends Collection<String> copyPartialMatches( final String token, final Iterable<String> originals, final T collection ) throws UnsupportedOperationException, IllegalArgumentException
// Returns the number of times char was found in string
int countMatches( String str, char chr )
// Convert string to bytes using default charset
byte[] decodeDefault( String str )
// Convert string to bytes using UTF-8 charset
byte[] decodeUtf8( String str )
// Convert bytes to string using default charset
String encodeDefault( byte[] bytes )
// Convert bytes to string using UTF-8 charset
String encodeUtf8( byte[] bytes )
// Checks if two strings match. Returns false if either is null.
boolean equals( @Nullable String left, @Nullable String right, boolean caseSensitive )
// Escape characters for HTML rendering, e.g., & -> &amp;
String escapeHtml( String str )
// Deprecated
String fixQuotes( String var )
// If string is null, return empty instead
String ifNullReturnEmpty( @Nullable String str )
// Does string start lowercase and contain no more than one uppercase char per word
boolean isCamelCase( String var )
// Does string match the result of capitalizeWordsFully( str );
boolean isCapitalizedWordsFully( String str )
// Does string match the result of capitalizeWords( str );
boolean isCapitalizedWords( String str )
// Does string length equal zero
boolean isEmpty( String str )
// Is string all lowercase
boolean isLowercase( String str )
// Is string all uppercase
boolean isUppercase( String str )
// Join map into a string using specified glue, result: key1=value1,key2=value2,...
String join( @Nonnull Map<String, ?> args, @Nonnull String glue )
// Join map into a string, result: key1=value1,key2=value2,...
String join( @Nonnull Map<String, ?> args )
// Join map into a string using specified glue and separator, result: key1=value1,key2=value2,...
String join( @Nonnull Map<String, ?> args, @Nonnull String glue, @Nonnull String keyValueSeparator )
// Join map into a string using specified glue and separator, as well as, how to treat null values, result: key1=value1,key2=value2,...
String join( @Nonnull Map<String, ?> args, @Nonnull String glue, @Nonnull String keyValueSeparator, @Nonnull String nullValue )
// Join set/list into a string, result: value1,value2,value3,...
String join( @Nonnull Collection<String> args )
// Join set/list into a string using specified glue, result: value1,value2,value3,...
String join( @Nonnull Collection<String> args, @Nonnull String glue )
// Join set/list into a string but use a lambda to convert each element into a string
String join( @Nonnull Collection<T> args, @Nonnull Function<T, String> function )
// Join set/list into a string but use a lambda to convert each element into a string with specified glue
String join( @Nonnull Collection<T> args, @Nonnull Function<T, String> function, @Nonnull String glue )
// Join array into string
String join( @Nonnull String[] args )
// Join array into string with specified glue
String join( @Nonnull String[] args, @Nonnull String glue )
// Join int array into string
String join( @Nonnull int[] args )
// Join int array into string with specified glue
String join( @Nonnull int[] args, @Nonnull String glue )
// Make first string character lowercase
String lcFirst( String value )
// Throws IllegalArgumentException if string does not match the specified length
void lengthMustEqual( @Nonnull String str, @Nonnegative int len )
// Trim string to max length, only if it exceeds spedified length
String limitLength( String str, int max )
// Trim string to max length and apply specified ellipsis, only if it exceeds specified length
String limitLength( String str, int max, boolean appendEllipsis )
// Parse string to Java Color
Color parseColor( String color )
// Parse HTML GET query string to a map, e.g., key1=value1&key2=value2&...
Map<String, String> queryToMap( String query ) throws UnsupportedEncodingException
// Produces a random string of characters contained in seed to the specified length
String randomChars( String seed, int length )
// Returns the first capture group from the specified regex
String regexCapture( @Nonnull String var, @Nonnull String regex )
// Returns the specified capture group from the specified regex
String regexCapture( @Nonnull String var, @Nonnull String regex, int group )
// Converts a regex string into a literal string
String regexQuote( String str )
// Removes special unicode characters, allowed: a-zA-Z0-9!#$%&'*+-/=?^_`{|}~@\. and space
String removeInvalidChars( String ref )
// Remove all letters from string
String removeLetters( String input )
// Remove all lowercase letters from string
String removeLettersLower( String input )
// Remove all uppercase letters from string
String removeLettersUpper( String input )
// Remove all numbers from string
String removeNumbers( String input )
// Remove all special characters from string, i.e., using regex \W
String removeSpecial( String input )
// Remove all whitespace from string, i.e., using regex \s
String removeWhitespace( String input )
// Repeat string the number of specified times
String repeat( @Nonnull String string, int count )
// Repeat string into a list of strings
List<String> repeatToList( @Nonnull String str, int length )
// Replace a single character at the specified position
String replaceAt( String par, int at, char replacement )
// Convert string to a slug string
String slugify( String str )
// Convert string to a slug string but use the specified glue, e.g., -
String slugify( String str, String glue )
// Split string using specified delimiter and limit
Stream<String> split( @Nonnull String str, @Nonnull String delimiter, int limit )
// Split string using specified delimiter
Stream<String> split( @Nonnull String str, @Nonnull String delimiter )
// Split string using specified regex Pattern
Stream<String> split( @Nonnull String str, @Nonnull Pattern delimiter )
// Split string using specified litteral regex string
Stream<String> splitLiteral( @Nonnull String str, @Nonnull String delimiter )
// Checks if string starts with specified string and ignores case
boolean startsWithIgnoreCase( @Nonnull final String string, @Nonnull final String... prefixes ) throws NullPointerException
// Convert string to bytes using ASCII charset
byte[] stringToBytesASCII( String str )
// Convert string to bytes using UTF-8 charset
byte[] stringToBytesUTF( String str )
// Convert string to ASCII string
String toAscii( String str )
// Convert string to camcel case, e.g., thIs is a teSt stRing -> thisIsATestString
String toCamelCase( String value )
// Convert a list of strings to lowercase
List<String> toLowerCase( List<String> strings )
// Convert a set of strings to lowercase
Set<String> toLowerCase( Set<String> strings )
// Convert an array of strings to lowercase
String[] toLowerCase( String[] strings )
// Convert a char array to lowercase
char[] toLowerCase( char[] chars )
// Convert a char array to lowercase using specified locale
char[] toLowerCase( char[] chars, Locale locale )
// Convert string to studly case, e.g., thIs is a teSt stRing -> ThisIsATestString
String toStudlyCase( String value )
// Convert string to unicode string
String toUnicode( String str )
// Trim whitespace, newline, tab, and return cartridge from start and end
String trimAll( @Nullable String text )
// Trim specified characters from start and end
String trimAll( @Nullable String text, char... characters )
// Trim specified characters from end
String trimEnd( @Nullable String text, char... characters )
// Trim specified substr from end
String trimEnd( @Nullable String text, String substr )
// Trim specified characters from start
String trimStart( @Nullable String text, char... characters )
// Trim specified substr from start
String trimStart( @Nullable String text, String substr )
// Returns string length or zero if null
int length( @Nullable String str )
// Returns string length or negative-one if null
int lengthOrNeg( @Nullable String str )
// Trim start of string using specified regex
String trimStartRegex( @Nullable String text, String regex )
// Trim end of string using specified regex
String trimEndRegex( @Nullable String text, String regex )
// Trim start and end of string using specified regex
String trimAllRegex( @Nullable String text, String regex )
// Wrap each entry with '`', e.g., `value1`,`value2`,`value3`
<T extends Collection<String>> T wrap( T list )
// Wrap each entry with specified wrapChar
<T extends Collection<String>> T wrap( T list, char wrapChar )
// Wrap each map element with ` and ', e.g., `key1`='value1', ideal for sql queries
<T extends Map<String, String>> T wrap( T map )
// Wrap each map element with specified keyChar and valueChar
<T extends Map<String, String>> T wrap( T map, char keyChar, char valueChar )
// Wrap string with specified wrapChar
String wrap( String str, char wrap )
// Returns true if string is not empty
boolean isNotEmpty( String str )
```

If you need to make multiple manipulations to a string, consider using the `StringChain` feature.

```java
Strs.StringChain chain = Strs.stringChain( "This is a string!" );

StringChain capitalizeWords( char delimiter )
StringChain capitalizeWords()
StringChain capitalizeWordsFully( char delimiter )
StringChain capitalizeWordsFully()
StringChain escape()
StringChain lcFirst()
StringChain removeInvalidChars()
StringChain removeLetters()
StringChain removeLettersLower()
StringChain removeLettersUpper()
StringChain removeNumbers()
StringChain removeSpecial()
StringChain removeWhitespace()
StringChain repeat( int cnt )
StringChain replace( String orig, String replace )
StringChain replaceInvalidChars( String replace )
StringChain replaceLetters( String replace )
StringChain replaceLettersLower( String replace )
StringChain replaceLettersUpper( String replace )
StringChain replaceNumbers( String replace )
StringChain replaceRegex( String orig, String replace )
StringChain replaceSpecial( String replace )
StringChain replaceWhitespace( String replace )
StringChain slugify()
StringChain toCamelCase()
StringChain toLowercase()
StringChain toStudlyCase()
StringChain toUppercase()
StringChain trimAll( char character )
StringChain trimAll()
StringChain trimEnd( char character )
StringChain trimFront( char character )
StringChain trimRegex( String regex )
StringChain wrap( char wrap )

chain.get();
```

`Strs` implements a public version of `java.lang.ConditionalSpecialCasing` via the `Strs.ConditionalSpecialCasing` subclass via reflections. `See java.lang.ConditionalSpecialCasing` for JavaDoc.

```java
char[] toLowerCaseCharArray( String src, int index, Locale locale )
int toLowerCaseEx( String src, int index, Locale locale )
char[] toUpperCaseCharArray( String src, int index, Locale locale )
int toUpperCaseEx( String src, int index, Locale locale )
```

`Strs` implements a public version of `java.lang.StringCoding` via the `Strs.StringCoding` subclass via reflections. See `java.lang.StringCoding` for JavaDoc.

```java
char[] decode( byte[] ba, int off, int len )
char[] decode( Charset cs, byte[] ba, int off, int len )
char[] decode( String charsetName, byte[] ba, int off, int len )
byte[] encode( char[] ca, int off, int len )
byte[] encode( Charset cs, char[] ca, int off, int len )
byte[] encode( String charsetName, char[] ca, int off, int len )
```

