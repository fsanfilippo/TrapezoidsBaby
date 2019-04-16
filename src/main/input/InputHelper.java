package main.input;

public class InputHelper {

    public static String nonDigitsToBlanks(final CharSequence input){
        final StringBuilder sb = new StringBuilder( input.length() );
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
            else{
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
