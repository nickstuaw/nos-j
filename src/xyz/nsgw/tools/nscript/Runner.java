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
// Parse and run an nscript file.
// Todo: Add an object in place of java.io.File to allow nscript execution from a simpler source.
public class Runner extends Thread {
    // Initialize ThreadLocal variables to increase thread safety
    // and reduce cache contention between CPUs.
    
    // lockIdLocal holds the ID of the lock the Runner instance is paired with.
    // (The lock ID is the position of the instance's Service in the list of
    // services within the ScriptRunHandler.
    private final ThreadLocal<Integer> lockIdLocal;
    // cancelLocal is a local variable used to cancel the iteration through lines
    // (to cancel the script).
    private final ThreadLocal<Boolean> cancelLocal;
    // scriptLocal contains the script file itself.
    private final ThreadLocal<File> scriptLocal;
    // variablesLocal contains the VariableHandler for the running script.
    private final ThreadLocal<VariableHandler> variablesLocal;
    // Constructor - requires the script file and the lock ID.
    public Runner(final File script, final int lockId) {
        /// Set the initial value for each local variable.
        scriptLocal = ThreadLocal.withInitial(()->script);
        // Don't cancel immediately.
        cancelLocal = ThreadLocal.withInitial(()->false);
        // Pass the lock ID of the used service to the thread.
        lockIdLocal = ThreadLocal.withInitial(()->lockId);
        // Use a fresh instance of VariableHandler for script-local variables.
        variablesLocal = ThreadLocal.withInitial(VariableHandler::new);
    }
    // The run method which runs when the thread is started.
    @Override
    public void run() {
        // Validate the file then read & store the lines to the lines array.
        String[] lines = safelyInterpret(scriptLocal.get());
        // Iterate through each line.
        for(String line : lines) {
            // If the script is being canceled, stop the script by breaking the loop.
            if(cancelLocal.get()) break;
            // Execute the line. If it fails, output an error message and stop the script.
            if(!executeLine(line)) {
                // Error message.
                System.out.println("An error occurred!");
                // Break the loop.
                break;
            }
        }
        // Unlock the service this thread has been attached to.
        // Doing so signifies that this service is no longer in use and can be used again.
        Main.getScriptHandler().unlock(lockIdLocal.get());
    }
    // Read the file line by line and export them to a String array.
    private String[] interpret(final File file) throws IOException {
        // Make a buffered reader through which to read lines from the file.
        BufferedReader reader = new BufferedReader(new FileReader(file));
        // Create an empty list to store the lines.
        List<String> ls = new ArrayList<>();
        // Add the next line until the EOF is reached.
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            // Add the current line. The line number read by reader.readLine() is incremental.
            ls.add(line);
        }
        // Convert the List to a string array and return that.
        return ls.toArray(String[]::new);
    }
    // Read and convert the lines of the script once the validity of the file has been checked.
    public String[] safelyInterpret(final File script) {
        // Check whether the file exists.
        if(script.exists()) {
            // Make sure that the file is not a directory.
            if(script.isFile()) {
                // Make sure that the file has the .ns extension.
                if(script.getName().endsWith(".ns")) {
                    // Read the lines to an array and catch any errors thrown.
                    try {
                        return interpret(script);
                    } catch(IOException ignored) {}
                }
            }
        }
        // Return an empty string of lines if the file is invalid.
        return new String[]{};
    }
    // Parse and execute a specific line of nscript.
    private boolean executeLine(final String line) {
        // Get the thraed-local VariableHandler instance and store it inside the current local scope.
        VariableHandler variables = variablesLocal.get();
        // Retrieve the line as a character array to use for the switch-case in the next for loop.
        char[] chars = line.toCharArray();
        // Initialize a reference variable to be used in case a
        // variable is being assigned or a method is being called.
        Reference reference = null;
        // Initialize and set a StringBuilder to build specific portions of the line.
        StringBuilder builder = new StringBuilder();
        // Initialize and set reading to false.
        // While reading is true, each char is appended to builder.
        boolean reading = false;
        // Run a for loop for every element in the chars array.
        for(int i = 0; i < chars.length; i++) {
            // Look for a matching character that has specific meaning.
            // #() =
            // These (apart from ')') may have to be ignored when reading is true to allow these symbols in strings.
            switch(chars[i]) {
                case '#' -> {
                    /* Todo: # is a comment so it should ignore the rest of the line
                    but this checks if i is 0 (why?)*/
                    if (i == 0)
                        // Break the for loop by setting i to its maximum.
                        i = chars.length;
                }
                case '(' -> {
                    // ( indicates the start of a value.
                    // The builder should not be empty because otherwise I wouldn't know what to do here.
                    if (!builder.isEmpty()) {
                        // Make a new reference using builder as it is so far as an identifier.
                        reference = new Reference(builder.toString());
                        // Reset builder to an empty string.
                        builder = new StringBuilder();
                        // Set reading to true to build the value and wait for ')'.
                        reading = true;
                    }
                }
                case ')' -> {
                    // ( indicates the end of a significant value.
                    // The builder should not be empty because otherwise I wouldn't know what to do here.
                    if (!builder.isEmpty()) {
                        // Assume that a reference with an identifier already exists.
                        assert reference != null;
                        // Set the value of the reference to the builder as it is at this point.
                        reference.setValue(builder.toString());
                        //...
                        // Reset builder.
                        builder = new StringBuilder();
                        // Set reading to false.
                        reading = false;
                    }
                    // Todo: If the builder is empty, why not make the reference and set reading to false?
                }
                case ' ' -> {
                    // A space may indicate the end of an instruction.
                    // If the loop is not reading a new string and the most recent string has been used or
                    // doesn't exist, look for instructions.
                    if (!reading && !builder.isEmpty()) {
                        // Initialize remaining. This stores the instructions that follow the first.
                        String[] remaining;
                        // Make a new reference using the first instruction as the identifier and the next
                        // instructions as the value.
                        // First instruction , rest of the line.
                        reference = new Reference(builder.toString(), line.substring(i + 1));
                        // Set remaining to an array of the following items.
                        remaining = line.substring(i + 1).split(" ");
                        // Look for a matching instruction for the first instruction.
                        switch (reference.getIdentifier()) {
                            // Set the target to the remaining text.
                            // Syntax: target boot OR target shutdown
                            case "target" -> variables.setTarget(reference.getValue());
                            // Effectively trigger the target.
                            // Syntax: initiate_target boot OR initiate_target boot with TypeMachineFolderNameOrPathHere
                            case "initiate_target" -> {
                                // Check that no error will occur if index 1 is accessed.
                                if (remaining.length > 2) {
                                    // Compare the second element of remaining with "with".
                                    // Todo: decrement 1 from the specified indexes.
                                    // Todo: (remaining[1] to remaining[0]
                                    if (Objects.equals(remaining[1], "with")) {
                                        // Todo: remaining[2] to remaining[1]
                                        // Initiate the target using the value specified.
                                        variables.initiateTarget(remaining[2]);
                                    }
                                }
                            }
                            // Syntax: set identifier value
                            case "set" -> {
                                String id = remaining[0];
                                if (id.endsWith("\\")) {
                                    return variables.setVariable(id.substring(0, id.length() - 1), reference.getValue().length() > id.length() + 1 ? reference.getValue().substring(id.length() + 1) : "");
                                } else {
                                    // Set a local variable specified with id and set to the remainder of the line.
                                    // reference.getValue().substring(id.length() + 1) removes the identifier and the space after it from reference.getValue().
                                    // Visualisation:
                                    // line = "set identifier This is a value"
                                    // variables.setLocal("identifier", "identifier This is a value" - "identifier ")
                                    // variables.setLocal("identifier", "This is a value")
                                    variables.setLocal(id, reference.getValue().substring(id.length() + 1));
                                }
                            }
                            // Clear every variable of this script from memory.
                            // Syntax: reset variables
                            case "reset" -> {
                                // Make sure remaining is not empty to avoid errors.
                                if(remaining.length > 0) {
                                    // Check if the first element equals "variables"
                                    // (line would be "reset variables")
                                    if(remaining[0].equals("variables")) {
                                        // Remove all variables.
                                        variables.reset();
                                    }
                                }
                            }
                            // Display text
                            // Syntax: show Hello! This line is being displayed on the screen using show.
                            // Output: Hello! This line is being displayed on the screen using show.
                            // Use VariableHandler#fillIn to fill placeholders for variables.
                            // Output with System.out#println
                            case "show" -> System.out.println(variables.fillIn(reference.getValue()));
                            // Take user input through the console.
                            // Syntax: take variableIdentifier with Type something: space
                            //         take [var identifier] with [Prompt] space
                            // space is optional. It adds a trailing space.
                            case "take" -> {
                                // Check that remaining is the right length.
                                if (remaining.length > 2) {
                                    // Check that remaining[1] equals "with".
                                    if (Objects.equals(remaining[1], "with")) {
                                        // Initialize and set suffix to an empty string.
                                        String suffix = "";
                                        // Allow spaces in the prompt by checking if the last word is "space".
                                        if(Objects.equals(remaining[remaining.length - 1], "space")) {
                                            // Add a trailing space.
                                            suffix = " ";
                                        }
                                        // Output the string with the suffix attached. This is the prompt
                                        System.out.print(Arrays.copyOfRange(remaining, 2, remaining.length - 1) + suffix);
                                    }
                                }
                                // Create a new reader instance for user input.
                                Reader reader = new Reader();
                                // Set the variable with the specified identifier to the user input.
                                variables.setLocal(remaining[0], reader.str());
                            }
                            // Make different objects (currently users only).
                            // Syntax: make regular user with TheirUsername
                            case "make" -> {
                                // Check that remaining is a good length to proceed.
                                if (remaining.length > 2) {
                                    // Check if remaining[1] is "user"
                                    if(remaining[1].equals("user")) {
                                        // Check if remaining[2] is "with"
                                        if(remaining[2].equals("with")) {
                                            // Check that remaining is a good length to proceed.
                                            if(remaining.length > 3) {
                                                // Create a new user.
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
                // = is for variable assignment.
                // Syntax: variableIdentifier=the value
                case '=' -> {
                    // If the builder is not empty, proceed.
                    if (!builder.isEmpty()) {
                        // Make a new reference, using the remaining text in the line for the value.
                        reference = new Reference(builder.toString(), line.substring(i + 1));
                        // Set the local variable with the reference.
                        variables.setLocal(reference);
                        // Return true because nothing else needs to be done and no errors were encountered.
                        return true;
                    }
                }
                // If no match was found, append the character to the builder.
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
