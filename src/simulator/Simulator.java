package simulator;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import simulator.transducer.*;


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

    public String readEncoding(String path) throws Exception{
        String modelDesc= "";
        int data;

        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            data = fileInputStream.read();
            while(data != -1) {
                // ignore tab, space and return characters
                if ((char) data == ' ' || data == 9 || data == 10 || data == 13) {
                    data = fileInputStream.read();
                } else {
                    modelDesc+=(char) data;
                    data = fileInputStream.read();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return modelDesc;
    }

    private void run(){

        // initialise a scanner for user input
        Scanner sc = new Scanner(System.in);

        // initialise a decoder
        decoder = new Decoder();

        // model choice (-1 for invalid input)
        int choice = -1;
        
        //ask user for model choice
        System.out.println("Please choose the type of the transducer model or type of translation function to enter:\n" +
                            "1: 2DFT\n" +
                            "2: MSOT\n" +
                            "3: SST\n" +
                            "4: 2DFT -> SST\n" +
                            "5: SST -> 2DFT\n" +
                            "0: exit");
        try {
            choice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid choice number. Please enter again.");
            choice = -1;
        }

        String modelDesc="";
        String path;
        String inputString;
        //construct model instance according to model input
        while (choice != 0) {
            if (choice >= 1 && choice <= 5) {
                // initialise different transducer according to model choice
                switch (choice) {
                    case 1:
                        //ask user for file address of model encoding
                        System.out.println("Please enter file address of the encoding of 2DFT:");
                        path = sc.nextLine();
                        if (path.equals("q")) {
                            break;
                        }
                        try {
                            modelDesc = readEncoding(path);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        while (!decoder.vaildEncoding(modelDesc) && !path.equals("q")) {
                            System.err.println("Encoding invalid.");
                            //ask user for file address of model encoding again
                            System.out.println("Please enter file address of the encoding of 2DFT:");
                            path = sc.nextLine();
                            if (path.equals("q")) {
                                break;
                            }
                            try {
                                modelDesc = readEncoding(path);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (path.equals("q")) {
                            break;
                        }
                        TDFT tdft;
                        try {
                            // initialise a 2DFT instance
                            tdft = decoder.decodeTDFT(modelDesc);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("Construction Error. Please check encoding of 2DFT.");
                            break;
                        }
                        
                        // ask for input string to simulator on this instance
                        System.out.println("Please enter the input string:");
                        inputString = sc.nextLine();
                        while (!inputString.equals("q")) {
                            if (tdft.vaildInput(inputString)) {
                                String output = tdft.run(inputString);
                                System.out.println("Output:");
                                System.out.println(output);
                                System.out.println("Please enter the input string:");
                                inputString = sc.nextLine();
                            } else {
                                System.err.println("Invaild input.");
                                System.out.println("Please enter the input string:");
                                inputString = sc.nextLine();
                            }
                        }
                        break;
                    case 2:
                        //ask user for file address of model encoding
                        System.out.println("Please enter file address of the encoding of MSOT:");
                        path = sc.nextLine();
                        if (path.equals("q")) {
                            break;
                        }
                        try {
                            modelDesc = readEncoding(path);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        while (!decoder.vaildEncoding(modelDesc) && !path.equals("q")) {
                            System.err.println("Encoding invalid.");
                            //ask user for file address of model encoding again
                            System.out.println("Please enter file address of the encoding of MSOT:");
                            path = sc.nextLine();
                            if (path.equals("q")) {
                                break;
                            }
                            try {
                                modelDesc = readEncoding(path);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (path.equals("q")) {
                            break;
                        }
                        MSOT msot;
                        try {
                            // initialise a MSOT instance
                            msot = decoder.decodeMSOT(modelDesc);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("Construction Error. Please check encoding of MSOT.");
                            break;
                        }
                        
                        // ask for input string to simulator on this instance
                        System.out.println("Please enter the input string:");
                        inputString = sc.nextLine();
                        while (!inputString.equals("q")) {
                            if (msot.vaildInput(inputString)) {
                                String output = msot.run(inputString);
                                System.out.println("Output:");
                                System.out.println(output);
                                System.out.println("Please enter the input string:");
                                inputString = sc.nextLine();
                            } else {
                                System.err.println("Invaild input.");
                                System.out.println("Please enter the input string:");
                                inputString = sc.nextLine();
                            }
                        }
                        break;
                    case 3:
                        //ask user for file address of model encoding
                        System.out.println("Please enter file address of the encoding of SST:");
                        path = sc.nextLine();
                        if (path.equals("q")) {
                            break;
                        }
                        try {
                            modelDesc = readEncoding(path);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        while (!decoder.vaildEncoding(modelDesc) && !path.equals("q")) {
                            System.err.println("Encoding invalid.");
                            //ask user for file address of model encoding again
                            System.out.println("Please enter file address of the encoding of SST:");
                            path = sc.nextLine();
                            if (path.equals("q")) {
                                break;
                            }
                            try {
                                modelDesc = readEncoding(path);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (path.equals("q")) {
                            break;
                        }
                        SST sst;
                        try {
                            // initialise a SST instance
                            sst = decoder.decodeSST(modelDesc);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("Construction Error. Please check encoding of SST.");
                            break;
                        }
                        
                        // ask for input string to simulator on this instance
                        System.out.println("Please enter the input string:");
                        inputString = sc.nextLine();
                        while (!inputString.equals("q")) {
                            if (sst.vaildInput(inputString)) {
                                String output = sst.run(inputString);
                                System.out.println("Output:");
                                System.out.println(output);
                                System.out.println("Please enter the input string:");
                                inputString = sc.nextLine();
                            } else {
                                System.err.println("Invaild input.");
                                System.out.println("Please enter the input string:");
                                inputString = sc.nextLine();
                            }
                        }
                        break;
                    case 4:
                        //ask user for file address of model encoding
                        System.out.println("Please enter file address of the encoding of 2DFT:");
                        path = sc.nextLine();
                        if (path.equals("q")) {
                            break;
                        }
                        try {
                            modelDesc = readEncoding(path);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        while (!decoder.vaildTDFT(modelDesc) && !path.equals("q")) {
                            System.err.println("Encoding invalid.");
                            //ask user for file address of model encoding again
                            System.out.println("Please enter file address of the encoding of 2DFT:");
                            path = sc.nextLine();
                            if (path.equals("q")) {
                                break;
                            }
                            try {
                                modelDesc = readEncoding(path);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (path.equals("q")) {
                            break;
                        }
                        String SSTDesc = "";
                        try {
                            SSTDesc = decoder.fromTDFTtoSST(modelDesc);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                            System.err.println("Construction Error. Please check encoding of 2DFT.");
                            break;
                        }
                        // System.out.println("Encoding of SST:");
                        // System.out.println(SSTDesc);
                        
                        try {
                            decoder.generateSSTGraphPDF(SSTDesc);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("Construction graph representation Error. Please check encoding of SST.");
                            break;
                        }
                        break;
                    case 5:
                        //ask user for file address of model encoding
                        System.out.println("Please enter file address of the encoding of SST:");
                        path = sc.nextLine();
                        if (path.equals("q")) {
                            break;
                        }
                        try {
                            modelDesc = readEncoding(path);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        while (!decoder.vaildSST(modelDesc) && !path.equals("q")) {
                            System.err.println("Encoding invalid.");
                            //ask user for file address of model encoding again
                            System.out.println("Please enter file address of the encoding of SST:");
                            path = sc.nextLine();
                            if (path.equals("q")) {
                                break;
                            }
                            try {
                                modelDesc = readEncoding(path);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (path.equals("q")) {
                            break;
                        }
                        String TDFTDesc = "";
                        try {
                            TDFTDesc = decoder.fromSSTtoTDFT(modelDesc);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("Construction Error. Please check encoding of SST.");
                            break;
                        }
                        // System.out.println("Encoding of 2DFT:");
                        // System.out.println(TDFTDesc);

                        try {
                            decoder.generateTDFTGraphPDF(TDFTDesc);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("Construction graph representation Error. Please check encoding of 2DFT.");
                            break;
                        }
                        break;
                    default:
                        break;
                }
            } else {
                System.err.println("Invalid choice number. Please enter again.");
            }
            
            // ask for model choice again
            System.out.println("Please choose the type of the transducer model or type of translation function to enter:\n" +
                                "1: 2DFT\n" +
                                "2: MSOT\n" +
                                "3: SST\n" +
                                "4: 2DFT -> SST\n" +
                                "5: SST -> 2DFT\n" +
                                "0: exit");
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Invalid choice number. Please enter again.");
                choice = -1;
            }
        }
        
        //close scanner to prevent resource leak
        sc.close();

    }
}