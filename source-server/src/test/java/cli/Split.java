package cli;


import java.util.Scanner;

public class Split {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            System.out.println(scanner.nextLine().split(args[0])[Integer.parseInt(args[1])]);
        }

    }
}
