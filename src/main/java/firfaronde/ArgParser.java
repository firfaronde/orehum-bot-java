package firfaronde;

import firfaronde.commands.CommandData;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// govno
public class ArgParser {
    public static final Logger logger = LoggerFactory.getLogger(ArgParser.class);
    public static ParseResult parseString(String argss, String argsPos) {
        String[] args = argss.trim().split("\\s+");
        if(argsPos == null || argsPos.isBlank()) {
            return new ParseResult(args);
        }
        String[] apos = argsPos.trim().split("\\s+");

        int needArgs = 0;
        // int optArgs = 0;

        List<String> args2 = new ArrayList<>();

        for(String a : apos) {
            if(a.startsWith("<") && a.endsWith(">"))
                needArgs += 1;
            /*if(a.startsWith("[") && a.endsWith("]"))
                optArgs += 1;*/
        }

        logger.info("Args provided {}\nArgs need {}\nArgs {}\nArgsPos {}", args.length, needArgs, argss, argsPos)

        if(needArgs > args.length)
            return new ParseResult("Слишком мало аргумнетов.");

        for(int i = 0; i<apos.length; i += 1) {
            String a = apos[i];

            if(a.startsWith("<") && a.endsWith("...>")) {
                args2.add(String.join(" ", Arrays.copyOfRange(args, i, args.length)));
                break;
            } else if(a.startsWith("<") && a.endsWith(">")) {
                try {
                    args2.add(args[i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    return new ParseResult("Аргумент "+a+" не приведен.");
                }
            } else {
                logger.info("Unkown arg type provided {}", a);
            }
        }

        return new ParseResult(args2.toArray(new String[0]));
    }

    @Getter
    public static class ParseResult {
        String[] args;
        boolean failed;
        String failedMessage;

        ParseResult(String[] args) {
            this.failed = false;
            this.args = args;
        }

        ParseResult(String failedMessage) {
            this.failedMessage = failedMessage;
            this.failed = true;
        }
    }
}
