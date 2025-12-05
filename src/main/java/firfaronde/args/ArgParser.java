package firfaronde.args;

import firfaronde.commands.CommandData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static firfaronde.Utils.canParseInt;

/**
 * chaos of bad code.
 * parses positional args
 * */
public class ArgParser {
    public static final Logger logger = LoggerFactory.getLogger(ArgParser.class);

    /*public static ParseResult parseString(String argss, String argsPos) {
        String[] args = argss.strip().trim().split("\\s+");
        if(argsPos == null || argsPos.isBlank()) {
            logger.debug("ArgsPos is empty, returning ALL args");
            return new ParseResult(args);
        }
        String[] apos = argsPos.strip().trim().split("\\s+");

        int needArgs = 0;
        // int optArgs = 0;

        List<String> args2 = new ArrayList<>();

        for(String a : apos) {
            if(a.startsWith("<") && a.endsWith(">"))
                needArgs += 1;
        }

        logger.debug("Args provided {}\nArgs need {}\nArgs {}\nArgsPos {}", args.length, needArgs, argss, argsPos);

        if(needArgs > args.length || (args.length == 1 && args[0].isEmpty()))
            return new ParseResult("Слишком мало аргумнетов. Требуется:\n`"+argsPos+"`");

        for(int i = 0; i<apos.length; i += 1) {
            String a = apos[i];

            if(a.startsWith("<") && a.endsWith("...>")) {
                args2.add(String.join(" ", Arrays.copyOfRange(args, i, args.length)));
                break;
            } else if(a.startsWith("<") && a.endsWith(">")) {
                try {
                    args2.add(args[i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    return new ParseResult("Аргумент `"+a+"` не приведен.");
                }
            } else {
                logger.info("Unkown arg type provided {}", a);
            }
        }

        return new ParseResult(args2.toArray(new String[0]));
    }*/

    public static ParseResult parseString(String argss, Arg... neededArgs) {
        /*if(neededArgs == null || neededArgs.length == 0)
            return new ParseResult(new ArgResult[0]);*/
        argss = argss.strip();
        String[] args = argss.isEmpty() ? new String[0] : argss.split("\\s+");
        int argsNeed = 0;
        for(Arg a : neededArgs)
            if(!a.opt) argsNeed += 1;

        logger.debug("Args provided {}", argss);

        if (args.length < argsNeed)
            return new ParseResult("Слишком мало аргументов. " + args.length + "/" + argsNeed);

        List<ArgResult<?>> argsResult = new ArrayList<>();

        String tmp1;

        for(int i = 0; i<argsNeed; i+= 1) {
            Arg a = neededArgs[i];
            if(a.opt) {
                if(a.greedy) {
                    tmp1 = String.join(" ", Arrays.copyOfRange(args, i, args.length));
                    try {
                        argsResult.add(new ArgResult<>(parse(tmp1, a.clazz)));
                    } catch (Exception e) {
                        logger.debug("Unable to parse arg {}", a.name, e);
                        return new ParseResult("Невозможно привести аргумент `"+a.name+"` к типу "+a.clazz.getSimpleName());
                    }
                    break;
                } else {
                    try {
                        tmp1 = args[i];
                        argsResult.add(new ArgResult<>(parse(tmp1, a.clazz)));
                    } catch (ArrayIndexOutOfBoundsException ignore) {
                        continue;
                    } catch (Exception e) {
                        logger.debug("Unable to parse arg {}", a.name, e);
                        return new ParseResult("Невозможно привести аргумент `"+a.name+"` к типу "+a.clazz.getSimpleName());
                    }
                }
            } else if(a.greedy) {
                tmp1 = String.join(" ", Arrays.copyOfRange(args, i, args.length));
                try {
                    argsResult.add(new ArgResult<>(parse(tmp1, a.clazz)));
                } catch (Exception e) {
                    logger.debug("Unable to parse arg {}", a.name, e);
                    return new ParseResult("Невозможно привести аргумент `"+a.name+"` к типу "+a.clazz.getSimpleName());
                }
                break;
            } else {
                try {
                    tmp1 = args[i];
                    argsResult.add(new ArgResult<>(parse(tmp1, a.clazz)));
                } catch (ArrayIndexOutOfBoundsException ignore) {
                    return new ParseResult("Аргумент `"+a.name+"` не приведен.");
                } catch (Exception e) {
                    logger.debug("Unable to parse arg {}", a.name, e);
                    return new ParseResult("Невозможно привести аргумент `"+a.name+"` к типу "+a.clazz.getSimpleName());
                }
            }
        }

        return new ParseResult(argsResult);
    }

    public static <T> T parse(String from, Class<T> to) throws Exception {
        if (to == Integer.class) {
            if (!canParseInt(from))
                throw new IllegalArgumentException("Cannot parse Integer: " + from);
            return to.cast(Integer.parseInt(from));
        } else if (to == String.class) {
            return to.cast(from);
        } else if (to == Boolean.class) {
            if (!from.equalsIgnoreCase("true") && !from.equalsIgnoreCase("false"))
                throw new IllegalArgumentException("Cannot parse Boolean: " + from);
            return to.cast(Boolean.parseBoolean(from));
        } else {
            throw new IllegalArgumentException("Unsupported type: " + to);
        }
    }


    /*public static void processArgsPos(List<CommandData> cd) {
        for (CommandData c : cd) {
            if (c.args == null || c.args.isBlank()) continue;

            String[] args = c.args.split(" ");
            if (args.length > 1 && (args[0].endsWith("...>") || args[0].endsWith("...]"))) {
                logger.warn("Args in command {} after arg {} will be ignored", c.name, args[0]);
            }
        }
    }*/

    @Getter
    public static class ParseResult {
        ArgResult<?>[] args;
        boolean failed;
        String failedMessage;

        public ParseResult(ArgResult<?>... a) {
            this.args = a;
        }

        public ParseResult(List<ArgResult<?>> list) {
            this.args = list.toArray(new ArgResult[0]);
        }

        ParseResult(String failedMessage) {
            this.failedMessage = failedMessage;
            this.failed = true;
        }
    }

    @Data
    @AllArgsConstructor
    public static class Arg {
        public String name;
        public boolean greedy, opt;
        public Class<?> clazz;
    }

    @Getter
    public static class ArgResult<T> {
        T value;

        ArgResult(T value) {
            this.value = value;
        }

        public boolean getBool() {
            if (value instanceof Boolean b) {
                return b;
            }
            throw new IllegalStateException("ArgResult does not contain a Boolean");
        }

        public int getInt() {
            if (value instanceof Integer i) {
                return i;
            }
            throw new IllegalStateException("ArgResult does not contain an Integer");
        }

        public String getString() {
            if(value instanceof String s) {
                return s;
            }
            throw new IllegalStateException("ArgResult does not contain an String");
        }
    }
}
