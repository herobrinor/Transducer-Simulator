import java.util.Scanner;

public class Simulator {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose the type of the transducer model:\n" +
                            "1: 2DFT\n" +
                            "2: SST\n");
        int modelInt = sc.nextInt();

        System.out.println("Please enter the encoding of transducer:\n");
        String modelDesc = sc.nextLine();
    }
}