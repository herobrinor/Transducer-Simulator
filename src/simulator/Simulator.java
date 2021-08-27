package simulator;
import java.util.Scanner;

import simulator.transducer.TDFT;

public class Simulator {

    // stores the singleton instance of this class
    private static Simulator simulator;

    // stores the decoder instance
    private Decoder decoder;

    /**
     * gets the singleton instance of this class
     * 
     * @return the singleton instance of this class
     */
    public static Simulator getInstance() {
        return simulator;
    }

    /**
     * gets the decoder instance
     * 
     * @return the decoder instance
     */
    public Decoder getDecoder() {
        return this.decoder;
    }

    // mark constructor as private to prevent external instances
    private Simulator() {
    }

    /**
     * main entry point for the simulator
     * 
     * @param args command-line arguments supplied by the OS
     * @throws SQLException
     */
    public static void main(String[] args){
        // initialize and run the program
        simulator = new Simulator();
        simulator.run();
    }

    private void run(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose the type of the transducer model:\n" +
                            "1: 2DFT\n" +
                            "2: SST");
        int modelInt = Integer.parseInt(sc.nextLine());

        System.out.println("Please enter the encoding of transducer:");
        String modelDesc = sc.nextLine();

        switch (modelInt) {
            case 1:
                TDFT tdft = decoder.decodeTDFT(modelDesc);
                break;
        
            default:
                break;
        }

        System.out.println("Please enter the input string:\n");
        String inpuString = sc.nextLine();
    }
}