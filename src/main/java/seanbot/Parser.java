package seanbot;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import seanbot.tasks.Deadline;
import seanbot.tasks.Event;
import seanbot.tasks.Task;
import seanbot.tasks.Todo;

/**
 * The Parser class processes user input and triggers corresponding commands.
 */
public class Parser {

     /**
     * Parses the user input and executes the appropriate command.
     * 
     * @param userInput The command entered by the user.
     * @param tasks The task list being operated on.
     * @param ui The user interface for interaction.
     * @param storage The storage system for saving tasks.
     */
    public void parse(String userInput, TaskList tasks, Ui ui, Storage storage) throws SeanBotException, IOException {
        assert userInput != null : "User Input cannot be null";
        assert tasks != null : "Tasks cannot be null";
        assert ui != null : "Ui object cannot be null";
        assert storage != null : "Storage object cannot be null";

        String[] part = userInput.split(" ", 2);
        String first = part[0];

        switch (first) {
            case "b":
                ui.showExitMessage();
                break;
            case "l":
                ui.showTaskList(tasks);
                break;
            case "m":
                if (part.length < 2) {
                    throw new SeanBotException("The task number of mark cannot be empty");
                }
                int markIndex = Integer.parseInt(part[1]) - 1;
                if (markIndex < 0 || markIndex >= tasks.size()) {
                    throw new SeanBotException("The task number must be valid.");
                }
                tasks.getTask(markIndex).markAsDone();
                storage.save(tasks.getTasks());
                System.out.println("Nice! I've marked this task as done:");
                System.out.println("  " + tasks.getTask(markIndex));
                break;
            case "um":
                if (part.length < 2) {
                    throw new SeanBotException("The task number of mark cannot be empty");
                }
                int unmarkIndex = Integer.parseInt(part[1]) - 1;
                if (unmarkIndex < 0 || unmarkIndex >= tasks.size()) {
                    throw new SeanBotException("The task number must be valid.");
                }
                tasks.getTask(unmarkIndex).markAsUndone();
                storage.save(tasks.getTasks());
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println("  " + tasks.getTask(unmarkIndex));
                break;
            case "t":
                if (part.length < 2 || part[1].trim().isEmpty()) {
                    throw new SeanBotException("The description of a todo cannot be empty.");
                }
                Task todo = new Todo(part[1]);
                tasks.addTask(todo);
                storage.save(tasks.getTasks());
                System.out.println("Got it. I've added this task:");
                System.out.println("  " + todo);
                System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                break;
            case "dl":
                String[] specifications = part[1].split(" /by ");
                if (specifications.length < 2) {
                    throw new SeanBotException("The description or deadline cannot be empty.");
                }
                try {
                    LocalDate by = LocalDate.parse(specifications[1].trim());
                    Task deadline = new Deadline(specifications[0], by.toString());
                    tasks.addTask(deadline);
                    storage.save(tasks.getTasks());
                    System.out.println("Got it. I've added this task:");
                    System.out.println("  " + deadline);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                } catch (DateTimeParseException e) {
                    throw new SeanBotException("Invalid date format. Please use the format yyyy-MM-dd.");
                }
                break;
            case "e":
                String[] firstPart = part[1].split(" /from ", 2);
                if (firstPart.length < 2) {
                    throw new SeanBotException("The description of an event cannot be empty.");
                }
                String[] secondPart = firstPart[1].split(" /to ", 2);
                if (secondPart.length < 2) {
                    throw new SeanBotException("The end time of an event cannot be empty.");
                }

                String description = firstPart[0].trim();
                String from = secondPart[0].trim();
                String to = secondPart[1].trim();

                try {
                    LocalDateTime startTime = LocalDateTime.parse(from);
                    LocalDateTime endTime = LocalDateTime.parse(to);
                    Task event = new Event(description, startTime.toString(), endTime.toString());
                    tasks.addTask(event);
                    storage.save(tasks.getTasks());
                    System.out.println("Got it. I've added this task:");
                    System.out.println("  " + event);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                } catch (DateTimeParseException e) {
                    throw new SeanBotException("Invalid date and time format. Please use the format yyyy-MM-ddTHH:mm.");
                }
                break;
            case "del":
                int deleteIndex = Integer.parseInt(part[1]) - 1;
                if (deleteIndex < 0 || deleteIndex >= tasks.size()) {
                    throw new SeanBotException("The task number to delete must be valid.");
                }
                Task removedTask = tasks.getTask(deleteIndex);
                tasks.deleteTask(deleteIndex);
                storage.save(tasks.getTasks());
                System.out.println("Noted. I've removed this task:");
                System.out.println("  " + removedTask);
                System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                break;
            case "f":
                List<Task> foundTasks = tasks.findTasks(part[1]);
                ui.showFoundTasks(foundTasks);
                break;
            default:
                throw new SeanBotException("I'm sorry, but I don't know what that means :-(");
        }
    }
}
