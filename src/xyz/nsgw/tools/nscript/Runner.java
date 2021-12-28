package xyz.nsgw.tools.nscript;

import xyz.nsgw.Main;
import xyz.nsgw.objects.user.PermissibleUser;
import xyz.nsgw.tools.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Runner extends Thread {

    private final ThreadLocal<Integer> lockIdLocal;

    private final ThreadLocal<Boolean> cancelLocal;

    private final ThreadLocal<File> scriptLocal;

    private final ThreadLocal<VariableHandler> variablesLocal;

    public Runner(final File script, final int lockId) {
        // Use ThreadLocal for thread safety (to reduce cache contention between CPUs)
        scriptLocal = ThreadLocal.withInitial(()->script);
        cancelLocal = ThreadLocal.withInitial(()->false);
        lockIdLocal = ThreadLocal.withInitial(()->lockId);
        variablesLocal = ThreadLocal.withInitial(VariableHandler::new);
    }

    @Override
    public void run() {
        String[] lines = safelyInterpret(scriptLocal.get());
        for(String line : lines) {
            if(cancelLocal.get()) break;
            if(!executeLine(line)) {
                System.out.println("An error occurred!");
                break;
            }
        }
        Main.getScriptHandler().unlock(lockIdLocal.get());
    }

    private String[] interpret(final File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> ls = new ArrayList<>();
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            ls.add(line);
        }
        return ls.toArray(String[]::new);
    }

    public String[] safelyInterpret(final File script) {
        if(script.exists()) {
            if(script.isFile()) {
                if(script.getName().endsWith(".ns")) {
                    try {
                        return interpret(script);
                    } catch(IOException ignored) {}
                }
            }
        }
        return new String[]{};
    }

    private boolean executeLine(final String line) {
        VariableHandler variables = variablesLocal.get();
        char[] chars = line.toCharArray();
        Reference reference = null;
        StringBuilder builder = new StringBuilder();
        boolean reading = false;
        for(int i = 0; i < chars.length; i++) {
            switch(chars[i]) {
                case '#' -> {
                    if (i == 0)
                        i = chars.length;
                }
                case '(' -> {
                    if (!builder.isEmpty()) {
                        reference = new Reference(builder.toString());
                        builder = new StringBuilder();
                        reading = true;
                    }
                }
                case ')' -> {
                    if (!builder.isEmpty()) {
                        assert reference != null;
                        reference.setValue(builder.toString());
                        //...
                        builder = new StringBuilder();
                        reading = false;
                    }
                }
                case ' ' -> {
                    if (!reading && !builder.isEmpty()) {
                        String[] remaining;
                        reference = new Reference(builder.toString(), line.substring(i + 1));
                        remaining = line.substring(i + 1).split(" ");
                        switch (reference.getIdentifier()) {
                            case "target" -> variables.setTarget(reference.getValue());
                            case "initiate_target" -> {
                                if (remaining.length > 2) {
                                    if (Objects.equals(remaining[1], "with")) {
                                        variables.initiateTarget(remaining[2]);
                                    }
                                }
                            }
                            case "set" -> {
                                String id = remaining[0];
                                if (id.endsWith("\\")) {
                                    return variables.setVariable(id.substring(0, id.length() - 1), reference.getValue().length() > id.length() + 1 ? reference.getValue().substring(id.length() + 1) : "");
                                } else {
                                    variables.setLocal(id, reference.getValue().substring(id.length() + 1));
                                }
                            }
                            case "reset" -> {
                                if(remaining.length > 0) {
                                    if(remaining[0].equals("variables")) {
                                        variables.reset();
                                    }
                                }
                            }
                            case "show" -> System.out.println(variables.fillIn(reference.getValue()));
                            case "take" -> {
                                if (remaining.length > 2) {
                                    if (Objects.equals(remaining[1], "with")) {
                                        String suffix = "";
                                        if(remaining.length > 3) {
                                            if(Objects.equals(remaining[3], "space")) {
                                                suffix = " ";
                                            }
                                        }
                                        System.out.print(remaining[2] + suffix);
                                    }
                                }
                                Reader reader = new Reader();
                                variables.setLocal(remaining[0], reader.str());
                            }
                            case "make" -> {
                                if (remaining.length > 2) {
                                    if(remaining[1].equals("user")) {
                                        if(remaining[2].equals("with")) {
                                            if(remaining.length > 3) {
                                                PermissibleUser user = new PermissibleUser(variables.fillIn(remaining[3]));
                                                //todo: if(remaining[4].equals("regular")) {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                case '=' -> {
                    if (!builder.isEmpty()) {
                        reference = new Reference(builder.toString(), line.substring(i + 1));
                        variables.setLocal(reference);
                        return true;
                    }
                }
                default -> builder.append(chars[i]);
            }
        }
        switch(builder.toString()) {
            case "shutdown" -> Main.getScriptHandler().endQueue();
            case "cancel" -> cancelLocal.set(true);
            case "clear_screen" -> {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        }
        return true;
    }
}
