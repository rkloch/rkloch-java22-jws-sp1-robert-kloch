import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("Server är nu Redo");
        //Init stuff
        ServerSocket serverSocket;
        Socket socket;
        InputStreamReader inputSR;
        OutputStreamWriter outputSW;
        BufferedReader bReader;
        BufferedWriter bWriter;
        JSONParser parser = new JSONParser();
        // Hämtar JSONfil för att användning senare, TODO skriva till fil
        JSONObject jsonData = (JSONObject) parser.parse(new FileReader("data/data.json"));

        //Starta Servern
        try {
            //Kontrollera att Socket nummer är ledig. Avbryt om socket är upptagen
            serverSocket = new ServerSocket(4321);
            System.out.println(serverSocket.getInetAddress());
            System.out.println(serverSocket.getLocalSocketAddress());
        } catch (IOException e) {
            System.out.println(e);
            return;
        }
        try {
            //Väntar på specifik socket efter trafik
            socket = serverSocket.accept();

            //Initiera Reader och Writer och koppla dem till socket
            inputSR = new InputStreamReader(socket.getInputStream());
            outputSW = new OutputStreamWriter(socket.getOutputStream());
            bReader = new BufferedReader(inputSR);
            bWriter = new BufferedWriter(outputSW);

            while (true) {
                //Hämta klientens meddelande och skicka den till openUpData()
                //Returnerar ett färdigt JSON objekt som String som skall tillbaka till klienten
                String message = bReader.readLine();
                String returnData = openUpData(message, jsonData);
                System.out.println("Message Recieved and sent back");

                //Skicka acknoledgement eller svar tillbaka
                bWriter.write(returnData);
                bWriter.newLine();
                bWriter.flush();

                //Avsluta om QUIT
                if (message.equalsIgnoreCase("quit")) break;
            }
            //Stäng kopplingar
            socket.close();
            inputSR.close();
            outputSW.close();
            bReader.close();
            bWriter.close();

        } catch (IOException e) {
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        } finally {
            System.out.println("Server avslutas");
        }
    }

    static String openUpData(String message, JSONObject jsonData) throws ParseException, IOException {
        //Bygger upp JSON Object baserat på inkommande string
        JSONParser parser = new JSONParser();
        JSONObject jsonOb = (JSONObject) parser.parse(message);

        //Läs av URL och HTTP-metod för att veta vad klienten vill
        String url = jsonOb.containsKey("httpURL") ? jsonOb.get("httpURL").toString() : "";
        String method = jsonOb.get("httpMethod").toString();

        //Dela upp URL med .split() metod
        String[] urls = url.split("/");

        //Logik för metod, hur informationen ska bearbetas och returneras
        if(method.equals("post")){
            JSONObject newStudentData = (JSONObject) jsonOb.get("data");
            //Ger student idnummer, stigande nummer
            //lägger till i jsonData för att kunnas hämtas senare
            newStudentData.put("id", (jsonData.size() + 1));
            jsonData.put("student" + (jsonData.size() + 1), newStudentData);
            JSONObject studentJson = new JSONObject();
            studentJson.put("student", newStudentData);
            JSONObject jsonReturn = new JSONObject();
            //Inkluderar HTTP status code och data och returnerar
            jsonReturn.put("httpStatusCode", 200);
            jsonReturn.put("data", studentJson);
            return jsonReturn.toJSONString();

        }else if(method.equals("get")){
                //Ifall urlen har argument för id så returneras bara en student
                if(urls.length == 2){
                    JSONObject jsonStudent = new JSONObject();
                    String studentWithId = "student" + urls[1];
                    //Kollar om idet inte existerar
                    if(!jsonData.containsKey(studentWithId)){
                        JSONObject jsonReturn = new JSONObject();
                        //Inkluderar 400 HTTP status code ifall id inte existerar
                        jsonReturn.put("httpStatusCode", 400);
                        return jsonReturn.toJSONString();
                    }

                    jsonStudent.put("student", (JSONObject) jsonData.get(studentWithId));
                    System.out.println(jsonStudent.toJSONString());
                    JSONObject jsonReturn = new JSONObject();
                    jsonReturn.put("data", jsonStudent);

                    //Inkluderar HTTP status code
                    jsonReturn.put("httpStatusCode", 200);
                    System.out.println(jsonReturn.toJSONString());
                    return jsonReturn.toJSONString();
                }else {
                    //returnerar alla studenter
                    //Skapa JSONReturn objektet
                    JSONObject jsonReturn = new JSONObject();

                    //Hämta data från JSON fil
                    jsonReturn.put("data", jsonData);

                    //Inkluderat HTTP status code
                    jsonReturn.put("httpStatusCode", 200);

                    //Return
                    return jsonReturn.toJSONString();

            }

        }
        return "Message Recieved";
    }
}