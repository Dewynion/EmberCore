package dev.blufantasyonline.embercore.util;

import dev.blufantasyonline.embercore.math.MathUtil;
import dev.blufantasyonline.embercore.reflection.ReflectionUtil;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {
    private final static int CENTER_PX = 154;

    /**
     * Produces a string with a {@link ChatColor} applied per-character to create a gradient according to the
     * provided array of {@link Color}s.
     */
    public static String gradientText(String text, Color... colors) {
        StringBuilder sb = new StringBuilder();
        text = ChatColor.stripColor(text);

        // Don't bother if no colors are provided.
        if (colors.length == 0)
            return text;
        // if it's just one, don't bother with a gradient
        else if (colors.length == 1)
            return ChatColor.of(colors[0]) + text;

        // calculate segment length. a 3-color gradient will have 2 segments (color 0 to color 1 and color 1 to
        // color 2), while a 2-color gradient will only have 1 (color 0 to color 1).
        int segmentLength = text.length() / (colors.length - 1);
        // leftover characters. distributed from the first segment to the last until no additional characters remain.
        // for example, a string of length 11 split into 2 segments will have one segment of length 6
        // and one of length 5.
        int remainder = text.length() % (colors.length - 1);
        // position in the string
        int pos = 0;

        // TODO: move the following into a "gradient" method that just outputs n Colors
        //       between current/target colors and invoke that instead.

        // for each color,
        for (int colorIndex = 1; colorIndex < colors.length; colorIndex++) {
            Color target = colors[colorIndex];
            Color current = colors[colorIndex - 1];

            // calculate segment length, including remainder if applicable
            int len = segmentLength;
            if (remainder > 0) {
                len++;
                remainder--;
            }

            // generate gradient between current and target
            Color[] gradient = ColorUtil.gradient(current, target, len);
            for (int i = 0; i < len; i++) {
                // append color + character
                sb.append(ChatColor.of(gradient[i])).append(text.charAt(pos));
                pos++;
            }
        }

        return sb.toString();
    }

    /**
     * See {@link #fString(String, Object, Map, ChatColor, ChatColor)}.
     */
    public static String fString(String s, Object object, Map<String, Object> data) {
        return fString(s, object, data, ChatColor.GRAY, ChatColor.GRAY);
    }

    /**
     * Python-style string formatter. Replaces anything in curly braces with the string representation of whatever
     * object is mapped to that string in the provided {@link Map}. This overload allows for the inclusion of an
     * Object, which will have its fields read into a map via {@link ReflectionUtil#toMap(Object)}.
     *
     * <br/><br/>
     *
     * <b>Please note that keys in the provided map will override keys generated from this object.</b>
     *
     * <br/><br/>
     *
     * For example, <code>"this cage contains {hamsters} hamsters"</code> with the dictionary <code>{"hamsters" : 2}</code>
     * would produce the string <code>"this cage contains 2 hamsters"</code>.
     *
     * @param s The string to format.
     * @param object An object to attempt to insert to
     * @param data A {@link Map} containing string -> object mappings for objects to insert.
     * @param textColor The {@link ChatColor} of regular message text.
     * @param elementColor The {@link ChatColor} to use for formatted elements, in case you want to highlight them.
     * @return A formatted string where any substrings enclosed in curly braces have been replaced by the appropriate
     *  string representation of the object they map to, if applicable.
     */
    public static String fString(String s, Object object, Map<String, Object> data, ChatColor textColor, ChatColor elementColor) {
        Map<String, Object> allData = ReflectionUtil.toMap(object);
        allData.putAll(data);
        return fString(s, data, textColor, elementColor);
    }

    /** See {@link #fString(String, Object, ChatColor, ChatColor)}. Uses {@link ChatColor#GRAY} by default. */
    public static String fString(String s, Object obj) {
        return fString(s, obj, ChatColor.GRAY, ChatColor.GRAY);
    }

    /**
     * Python-style string formatter. Replaces anything in curly braces with the string representation of any
     * matching-named fields in the provided object. The map is generated using {@link ReflectionUtil#toMap(Object)},
     * then this method calls {@link #fString(String, Map, ChatColor, ChatColor)}.
     *
     * <br/><br/>
     *
     * For example, <code>"this cage contains {hamsters} hamsters"</code> mapped to an object of type <code>Cage</code>
     * with an integer field <code>int hamsters = 2;</code> would produce <code>"this cage contains 2 hamsters"</code>.
     *
     * @param s The string to format.
     * @param obj The object to read data from.
     * @param textColor The {@link ChatColor} of regular message text.
     * @param elementColor The {@link ChatColor} to use for formatted elements, in case you want to highlight them.
     * @return A formatted string where any substrings enclosed in curly braces have been replaced by the appropriate
     *  string representation of the object they map to, if applicable.
     */
    public static String fString(String s, Object obj, ChatColor textColor, ChatColor elementColor) {
        return fString(s, ReflectionUtil.toMap(obj), textColor, elementColor);
    }

    /**
     * See {@link #fString(String, Map, ChatColor, ChatColor)}. Uses {@link ChatColor#GRAY} by default.
     */
    public static String fString(String s, Map<String, Object> data) {
        return fString(s, data, ChatColor.GRAY, ChatColor.GRAY);
    }

    /**
     * Python-style string formatter. Replaces anything in curly braces with the string representation of whatever
     * object is mapped to that string in the provided {@link Map}.
     *
     * <br/><br/>
     *
     * For example, <code>"this cage contains {hamsters} hamsters"</code> with the dictionary <code>{"hamsters" : 2}</code>
     * would produce the string <code>"this cage contains 2 hamsters"</code>.
     *
     * @param s The string to format.
     * @param data A {@link Map} containing string -> object mappings for objects to insert.
     * @param textColor The {@link ChatColor} of regular message text.
     * @param elementColor The {@link ChatColor} to use for formatted elements, in case you want to highlight them.
     * @return A formatted string where any substrings enclosed in curly braces have been replaced by the appropriate
     *  string representation of the object they map to, if applicable.
     */
    public static String fString(String s, Map<String, Object> data, ChatColor textColor, ChatColor elementColor) {
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(s);

        while (matcher.find()) {
            Object value = data.get(matcher.group(1));
            if (value != null) {

                s = s.replace(matcher.group(0), elementColor + value.toString() + textColor);
            }
        }

        return s;
    }

    /**
     * See {@link DefaultFontInfo} for source. Pads the provided string with spaces
     * to produce a message that is centered in chat.
     */
    public static String centeredMessage(String message) {
        if(message == null || message.equals(""))
            return "";
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        sb.append(message);
        return sb.toString();
    }

    /**
     * Courtesy of
     * <a href="https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/">
     *     a kind individual on the Spigot forums</a>. Used for sending centered chat messages.
     */
    public enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private char character;
        private int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }
}
