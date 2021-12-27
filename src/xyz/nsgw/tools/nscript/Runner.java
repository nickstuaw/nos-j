package xyz.nsgw.tools.nscript;

import xyz.nsgw.tools.Reader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Runner {

    private String[] lines;

    private boolean ready = false;

    private VariableHandler variables;

    private List<String> values;

    private int nextValueIndex;

    private int tokenIndex;

    private String lastCompletedValue;

    public Runner() {
        resetVariables();
    }

    public Runner(final File script) {
        resetVariables();
        safelyInterpret(script);
    }

    public void resetVariables() {
        variables = new VariableHandler();
    }

    private void interpret(final File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> ls = new ArrayList<>();
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            ls.add(line);
        }
        lines = ls.toArray(String[]::new);
        ready = true;
    }

    public boolean safelyInterpret(final File script) {
        if(script.exists()) {
            if(script.isFile()) {
                if(script.getName().endsWith(".ns")) {
                    try {
                        interpret(script);
                        return true;
                    } catch(IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public void run(final File script) {
        if(!ready) {
            if(!safelyInterpret(script))
                return;
        }
        run();
    }

    public void run() {
        for(String line : lines) {
            runLine(line);
        }
    }

    private void runLine(final String line) {
        String[] spaced = line.split(" ");
        values = new ArrayList<>();
        String value;
        boolean readingValue = false;
        char[] chars = line.toCharArray();
        Reference reference = null;
        StringBuilder builder = new StringBuilder();
        boolean reading = false;
        for(int i = 0; i < chars.length; i++) {
            switch(chars[i]) {
                case '#':
                    if(i == 0)
                        i = chars.length;
                    break;
                case '(':
                    if(!builder.isEmpty()) {
                        reference = new Reference(builder.toString());
                        builder = new StringBuilder();
                        reading = true;
                    }
                    break;
                case ')':
                    if(!builder.isEmpty()) {
                        assert reference != null;
                        reference.setValue(builder.toString());
                        //...
                        builder = new StringBuilder();
                        reading = false;
                    }
                    break;
                case ' ':
                    if(reading)
                        break;
                    if(!builder.isEmpty()) {
                        reference = new Reference(builder.toString(), line.substring(i + 1));
                        switch (reference.getIdentifier()) {
                            case "target":
                                variables.setTarget(reference.getValue());
                                break;
                            case "initiate_target":
                                variables.initiateTarget(reference.getValue());
                                break;
                            case "set":
                                String id = reference.getValue().split(" ")[0];
                                if(id.endsWith("\\")) {
                                    variables.setVariable(id.substring(0,id.length() - 1), reference.getValue().length() > id.length() + 1 ? reference.getValue().substring(id.length() + 1) : "");
                                } else {
                                    variables.setLocal(id, reference.getValue().substring(id.length() + 1));
                                }
                                break;
                            case "show":
                                System.out.println(variables.fillIn(reference.getValue()));
                                break;
                            case "take":
                                String[] remaining = reference.getValue().split(" ");
                                if(remaining.length > 2) {
                                    if(Objects.equals(remaining[1], "with")) {
                                        System.out.print(remaining[2]);
                                    }
                                }
                                Reader reader = new Reader();
                                variables.setLocal(remaining[0], reader.str());
                                break;
                        }
                        i = chars.length;
                    }
                    break;
                case '=':
                    if(!builder.isEmpty()) {
                        reference = new Reference(builder.toString(), line.substring(i + 1));
                        i = chars.length;
                    }
                    break;
                default:
                    builder.append(chars[i]);
                    continue;
            }
        }
    }
}
