import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class Client {
    public static void main(String[] args) {
        System.out.println("Client är nu redo");

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

            //Initiera Scanner för att skriva i konsol


            while (true) {
                //Anroppar meny för användare, låter dem göra ett val.
                //Valet returneras som ett färdigt JSON string
                String message = userInput();

                //Skicka meddelande till server
                bWriter.write(message);
                bWriter.newLine();
                bWriter.flush();

                //Hämta respnse från server
                String resp = bReader.readLine();

                //Anropa openResponse metod med server response
                openResponse(resp);

                //Avsluta om QUIT
                //if (message.equalsIgnoreCase("quit")) break;
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
        //Steg 1. Skriv ut en meny för användaren
        System.out.println("1. Hämta data");
        System.out.println("2. Lägg till data");

        //Steg 2. Låta användaren göra ett val
        Scanner scan = new Scanner(System.in);
        System.out.print("Skriv in ditt menyval: ");

        String val = scan.nextLine();

        //Steg 3. Bearbeta användarens val
        if (val.equals("1")) {//Skapa JSON objekt för att hämta data om alla personer. Stringifiera objekete och returnera det

            JSONObject jsonReturn = new JSONObject();
            jsonReturn.put("httpURL", "students");
            jsonReturn.put("httpMethod", "get");

            System.out.println(jsonReturn.toJSONString());

            //Returnera JSON objekt
            return jsonReturn.toJSONString();
            //break;
        }
        if (val.equals("2")) {//Skapa JSON objekt för att hämta data om alla personer. Stringifiera objekete och returnera det
            JSONObject jsonReturn = new JSONObject();
            jsonReturn.put("httpURL", "students/1/");
            jsonReturn.put("httpMethod", "get");

            System.out.println(jsonReturn.toJSONString());

            //Returnera JSON objekt
            return jsonReturn.toJSONString();
            //break;
        }


        return "error";
    }

    static void openResponse(String resp) throws ParseException {
        String testReturn = "";
        //Init Parser för att parsa till JSON Objekt
        JSONParser parser = new JSONParser();

        //Skapar ett JSON objekt från server respons
        JSONObject serverResponse = (JSONObject) parser.parse(resp);

        //Kollar om respons lyckas
        if ("200".equals(serverResponse.get("httpStatusCode").toString())) {
            //TODO Kolla vad som har returnerats

            //Bygger upp ett JSONObjekt av den returnerade datan
            JSONObject data = (JSONObject) parser.parse(serverResponse.get("data").toString());

            //Hämtar en lista av alla nycklar attribut i data och loopar sedan igenom dem
            Set<String> keys = data.keySet();
            for (String x : keys) {
                //Hämtar varje person object som finns i data
                JSONObject student = (JSONObject) data.get(x);

                //Skriv ut namnet på person
                System.out.println(student.get("name"));
                testReturn += student.get("name");
            }
        }
    }
}