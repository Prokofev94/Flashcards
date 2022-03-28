package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static Map<String, Info> cardsInMemory = new TreeMap<>();
    private static SortedSet<String> hardestCards = new TreeSet<>();
    private static StringBuilder log = new StringBuilder();

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            if ("-import".equals(args[i])) {
                importToFile(args[i + 1]);
            }
        }
        start();
        for (int i = 0; i < args.length; i += 2) {
            if ("-export".equals(args[i])) {
                exportFromFile(args[i + 1]);
            }
        }
    }

    public static void start() {
        while (true) {
            String fileName;
            System.out.println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            log.append("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):\n");
            String act = scanner.nextLine();
            log.append(act);
            log.append("\n");
            switch (act) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    removeCard();
                    break;
                case "import":
                    System.out.println("File name:");
                    log.append("File name:\n");
                    fileName = scanner.nextLine();
                    log.append(fileName);
                    log.append("\n");
                    importToFile(fileName);
                    break;
                case "export":
                    System.out.println("File name:");
                    log.append("File name:\n");
                    fileName = scanner.nextLine();
                    log.append(fileName);
                    exportFromFile(fileName);
                    break;
                case "ask":
                    askCards();
                    break;
                case "exit":
                    System.out.println("Bye bye!");
                    log.append("Bye bye!\n");
                    return;
                case "log":
                    saveLog();
                    break;
                case "hardest card":
                    printHardestCards();
                    break;
                case "reset stats":
                    resetStats();
                    break;
                default:
                    break;
            }
            System.out.println();
            log.append("\n");
        }
    }

    public static boolean containsDefinition(Map<String, Info> cards, String definition) {
        for (var c : cards.entrySet()) {
            if (c.getValue().getDefinition().equals(definition)) {
                return true;
            }
        }
        return false;
    }

    public static void addCard() {
        System.out.println("The card:");
        log.append("The card:\n");
        String card = scanner.nextLine();
        log.append(card);
        log.append("\n");
        if (cardsInMemory.containsKey(card)) {
            String cardExist = String.format("The card \"%s\" already exists.\n", card);
            System.out.print(cardExist);
            log.append(cardExist);
            return;
        }
        System.out.println("The definition of the card:");
        log.append("The definition of the card:\n");
        String definition = scanner.nextLine();
        log.append(definition);
        log.append("\n");
        if (containsDefinition(cardsInMemory, definition)) {
            String definitionExist = String.format("The definition \"%s\" already exists.\n", definition);
            System.out.print(definitionExist);
            log.append(definitionExist);
            return;
        }
        cardsInMemory.put(card, new Info(definition, 0));
        String addition = String.format("The pair (\"%s\":\"%s\") has been added.\n", card, definition);
        System.out.print(addition);
        log.append(addition);
    }

    public static void removeCard() {
        System.out.println("Which card?");
        log.append("Which card?\n");
        String card = scanner.nextLine();
        log.append(card);
        log.append("\n");
        if (cardsInMemory.containsKey(card)) {
            cardsInMemory.remove(card);
            System.out.println("The card has been removed.");
            log.append("The card has been removed.\n");
        } else {
            String notExist = String.format("Can't remove \"%s\": there is no such card.\n", card);
            System.out.print(notExist);
            log.append(notExist);
        }
    }

    public static void importToFile(String fileName) {
        File file = new File(fileName);
        int count = 0;
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNext()) {
                String[] line = sc.nextLine().split(":");
                if (!cardsInMemory.containsKey(line[0])) {
                    count++;
                }
                int cardErrors = cardsInMemory.containsKey(line[0]) ? cardsInMemory.get(line[0]).getErrors() : 0;
                cardsInMemory.put(line[0], new Info(line[1], Integer.parseInt(line[2]) + cardErrors));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            log.append("File not found.\n");
            return;
        }
        String loaded = String.format("%d cards have been loaded.\n", count);
        System.out.print(loaded);
        log.append(loaded);
    }

    public static void exportFromFile(String fileName) {
        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file, true)) {
            for (var entry : cardsInMemory.entrySet()) {
                String line = String.format("%s:%s:%s\n",
                        entry.getKey(), entry.getValue().getDefinition(), entry.getValue().getErrors());
                writer.append(line);
            }
        } catch (IOException e) {
            System.out.println("File not found.");
            log.append("File not found.\n");
            return;
        }
        String saved = String.format("%d cards have been saved.\n", cardsInMemory.size());
        System.out.print(saved);
        log.append(saved);
    }

    public static void askCards() {
        System.out.println("How many times to ask?");
        log.append("How many times to ask?\n");
        int n = Integer.parseInt(scanner.nextLine());
        log.append(n);
        log.append("\n");
        for (String card : cardsInMemory.keySet()) {
            if (n-- == 0) {
                break;
            }
            String print = String.format("Print the definition of \"%s\":\n", card);
            System.out.print(print);
            log.append(print);
            String answer = scanner.nextLine();
            log.append(answer);
            log.append("\n");
            if (answer.equals(cardsInMemory.get(card).getDefinition())) {
                System.out.println("Correct!");
                log.append("Correct!\n");
            } else if (containsDefinition(cardsInMemory, answer)) {
                cardsInMemory.get(card).setErrors(1);
                String correctCard = null;
                for (var e : cardsInMemory.entrySet()) {
                    if (answer.equals(e.getValue().getDefinition())) {
                        correctCard = e.getKey();
                        break;
                    }
                }
                String wrongBut = String.format("Wrong. The right answer is \"%s\", but your definition is correct for \"%s\".\n",
                        cardsInMemory.get(card).getDefinition(), correctCard);
                System.out.print(wrongBut);
                log.append(wrongBut);

            } else {
                cardsInMemory.get(card).setErrors(1);
                String wrong = String.format("Wrong. The right answer is \"%s\".\n", cardsInMemory.get(card).getDefinition());
                System.out.print(wrong);
                log.append(wrong);
            }
        }
    }

    public static void printHardestCards() {
        int maxErrors = 1;
        for (String card : cardsInMemory.keySet()) {
            if (cardsInMemory.get(card).getErrors() == maxErrors) {
                hardestCards.add(card);
            } else if (cardsInMemory.get(card).getErrors() > maxErrors) {
                maxErrors = cardsInMemory.get(card).getErrors();
                hardestCards.clear();
                hardestCards.add(card);
            }
        }
        if (hardestCards.size() == 1) {
            String hardestCardIs = String.format("The hardest card is %s. You have %d errors answering it.\n",
                    stringHardestCards(), maxErrors);
            System.out.print(hardestCardIs);
            log.append(hardestCardIs);
        } else  if (hardestCards.size() > 0) {
            String hardestCardsAre = String.format("The hardest cards are %s. You have %d errors answering them.\n",
                    stringHardestCards(), maxErrors);
            System.out.print(hardestCardsAre);
            log.append(hardestCardsAre);
        } else {
            System.out.println("There are no cards with errors.");
            log.append("There are no cards with errors.\n");
        }
    }

    public static String stringHardestCards() {
        StringBuilder result = new StringBuilder();
        for (String card : hardestCards) {
            result.append(String.format("\"%s\", ", card));
        }
        return result.substring(0, result.length() - 2);
    }

    public static void resetStats() {
        for (var c : cardsInMemory.entrySet()) {
            c.getValue().resetErrors();
        }
        hardestCards.clear();
        System.out.println("Card statistics have been reset.");
        log.append("Card statistics have been reset.\n");
    }

    public static void saveLog() {
        System.out.println("File name:");
        String fileName = scanner.nextLine();
        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append(log);
        } catch (IOException e) {
            System.out.println("File not found.");
            return;
        }
        System.out.println("The log has been saved.");
    }
}

class Info {
    private String definition;
    private int errors;

    public Info(String definition, int errors) {
        this.definition = definition;
        this.errors = errors;
    }

    public String getDefinition() {
        return this.definition;
    }

    public int getErrors() {
        return this.errors;
    }

    public void setErrors(int errors) {
        this.errors = this.errors + errors;
    }

    public void resetErrors() {
        this.errors = 0;
    }
}