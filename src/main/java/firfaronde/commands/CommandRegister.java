package firfaronde.commands;

import firfaronde.Vars;

public class CommandRegister {
    public static void load() {
        CommandData d = new CommandData(
                "test",
                "ого",
                (e, a)->{
                    System.out.println(a);
                }
        );
        Vars.handler.commands.add(d);
    }
}
