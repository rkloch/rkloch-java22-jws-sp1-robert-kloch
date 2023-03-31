import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Set;


public class Client {
    public static void main(String[] args) {
        System.out.println("Client startas");
        //Init stuff. Set as null to be initialized as "something"
        Socket socket = null;
        InputStreamReader inputSR = null;
        OutputStreamWriter outputSW = null;
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;

        //Starta Klienten
        try {
            //Init Socket med specifik port
            socket = new Socket("localhost", 4321);

            //Initiera Reader och Writer och koppla dem till socket
            inputSR = new InputStreamReader(socket.getInputStream());
            outputSW = new OutputStreamWriter(socket.getOutputStream());
            bReader = new BufferedReader(inputSR);
            bWriter = new BufferedWriter(outputSW);

            while (true) {
                //Anropar meny för användare, låter dem göra ett val.
                String message = userInput();
                //Ifall message är error pga inte korrekt input så körs userInput() igen
                while(message.equals("error")){
                    message = userInput();
                }

                //Skicka meddelande till server
                bWriter.write(message);
                bWriter.newLine();
                bWriter.flush();

                //Hämta respnse från server
                String resp = bReader.readLine();

                //Anropa openResponse metod med server response
                openResponse(resp);
            }
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        } finally {
            try {
                //Stäng kopplingar
                if (socket != null) socket.close();
                if (inputSR != null) inputSR.close();
                if (outputSW != null) outputSW.close();
                if (bWriter != null) bWriter.close();
                if (bReader != null) bReader.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Client Avslutas");
        }
    }

    static String userInput() {
        //Skriver ut en meny för användaren
        System.out.println("1. Hämta data");
        System.out.println("2. Hämta data om specifik student med id");
        System.out.println("3. Lägg till student");

        //Användaren gör val
        Scanner scan = new Scanner(System.in);
        System.out.print("Ditt val: ");
        String val = scan.nextLine();
        JSONObject jsonReturn = new JSONObject();

        switch (val) {
            case "1" -> {
                // Hämtar all data
                jsonReturn.put("httpURL", "students");
                jsonReturn.put("httpMethod", "get");
                return jsonReturn.toJSONString();
            }
            case "2" -> {
                //Hämtar specifik student med idnummer
                System.out.println("ID-nummer: ");
                String idNumber = scan.nextLine();
                jsonReturn.put("httpURL", "students/" + idNumber + "/");
                jsonReturn.put("httpMethod", "get");

                //Returnera JSON objekt
                return jsonReturn.toJSONString();
            }
            case "3" -> {
                //Skapar ny student och skickar postmethod med data
                System.out.println("Skriv in informationen om studenten");
                System.out.println("Namn: ");
                String name = scan.nextLine();
                System.out.println("Ålder: ");
                String age = scan.nextLine();
                System.out.println("Betyg: ");
                String grade = scan.nextLine();
                jsonReturn.put("httpMethod", "post");
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("name", name);
                bodyJson.put("age", age);
                bodyJson.put("grade", grade);
                jsonReturn.put("data", bodyJson);
                return jsonReturn.toJSONString();
            }
            default -> {
                System.out.println("Felaktig input, prova igen");
                return "error";
            }
        }
    }

    static void openResponse(String resp) throws ParseException {
        //Init Parser för att parsa till JSON Objekt
        JSONParser parser = new JSONParser();

        //Skapar ett JSON objekt från server respons
        JSONObject serverResponse = (JSONObject) parser.parse(resp);

        //Kollar om respons lyckas
        if ("200".equals(serverResponse.get("httpStatusCode").toString())) {

            //Bygger upp ett JSONObjekt av den returnerade datan
            JSONObject data = (JSONObject) parser.parse(serverResponse.get("data").toString());
            //Hämtar en lista av alla nycklar attribut i data och loopar sedan igenom dem
            Set<String> keys = data.keySet();
            for (String x : keys) {
                //Hämtar varje person object som finns i data
                JSONObject student = (JSONObject) data.get(x);

                //Skriv ut information om studenten
                System.out.println("---------------------------------------");
                System.out.println("Id: " + student.get("id"));
                System.out.println("Namn: " + student.get("name"));
                System.out.println("Ålder: " + student.get("age"));
                System.out.println("Betyg: " + student.get("grade"));
                System.out.println("---------------------------------------");
            }
        }else{
            //Ifall idnumret inte finns
            if("400".equals(serverResponse.get("httpStatusCode").toString())){
                System.out.println("Felaktigt idnummer, försök igen");
            }
        }
    }
}