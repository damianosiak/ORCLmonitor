package com.damianosiak;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**This class is responsible for all operations on collection of connections objects*/
public class Connections {
    /**This list consist of all connections objects*/
    private List<Connection> connectionList = new ArrayList<>();

    /**This method returns connections collection*/
    public List<Connection> getConnectionList() {
        return connectionList;
    }

    /**This method is responsible for checking if new connection object already exists in collection*/
    public Boolean connectionWithThisConnectionNameAlreadyExists(String connectionName){
        try{
            for(Connection actualConnectionFromConnectionList : connectionList){
                if(actualConnectionFromConnectionList.getConnectionName().equals(connectionName)){
                    return true;
                }
            }return false;
        }catch (Exception e){
            System.out.println("(connectionWithThisConnectionNameAlreadyExists): "+e);
            return true;
        }
    }

    /**This method is responsible for adding connection object to collection*/
    public Boolean addConnectionToConnectionList(Connection connection){
        try{
            if(!connectionWithThisConnectionNameAlreadyExists(connection.getConnectionName())) {
                connectionList.add(connection);
                return true;
            }else{
                System.out.println("Error (addConnectionToConnectionList): connection with this name already exists in connectionList");
                return false;
            }
        }catch (Exception e){
            System.out.println("(addConnectionToConnectionList): "+e);
            return false;
        }
    }

    /**This method return connection object from collection by connection name*/
    public Connection getConnectionFromConnectionListByConnectionName(String connectionName){
        try{
            for(Connection actualConnectionFromConnectionList : connectionList){
                if(actualConnectionFromConnectionList.getConnectionName().equals(connectionName)){
                    return actualConnectionFromConnectionList;
                }
            }return null;
        }catch (Exception e){
            System.out.println("(getConnectionFromConnectionListByConnectionName): "+e);
            return null;
        }
    }

    /**This method is responsible for modify connection object from collection*/
    public Boolean editConnectionFromConnectionListByConnectionName(String connectionNameFromConnectionList, String connectionName, String connectionAddress, String connectionPort, String connectionServiceNameOrSID, String connectionUser, String connectionPassword, String connectionRefreshRate){
        try{
            for(Connection actualConnectionFromConnectionList : connectionList){
                if(actualConnectionFromConnectionList.getConnectionName().equals(connectionNameFromConnectionList)){
                    actualConnectionFromConnectionList.setConnectionName(connectionName);
                    actualConnectionFromConnectionList.setConnectionAddress(connectionAddress);
                    actualConnectionFromConnectionList.setConnectionPort(connectionPort);
                    actualConnectionFromConnectionList.setConnectionServiceNameOrSID(connectionServiceNameOrSID);
                    actualConnectionFromConnectionList.setConnectionUser(connectionUser);
                    actualConnectionFromConnectionList.setConnectionPassword(connectionPassword);
                    actualConnectionFromConnectionList.setConnectionRefreshRate(connectionRefreshRate);
                    return true;
                }
            }return false;
        }catch (Exception e){
            System.out.println("(editConnectionFromConnectionListByConnectionName): "+e);
            return false;
        }
    }

    /**This method is responsible for deleting connection object from collection*/
    public Boolean deleteConnectionFromConnectionListByConnectionName(String connectionName){
        try{
            for(Connection actualConnectionFromConnectionList : connectionList){
                if(actualConnectionFromConnectionList.getConnectionName().equals(connectionName)){
                    connectionList.remove(actualConnectionFromConnectionList);
                    return true;
                }
            }return false;
        }catch (Exception e){
            System.out.println("(deleteConnectionFromConnectionListByConnectionName): "+e);
            return false;
        }
    }

    /**This method is responsible for export collection of connections objects to file to default location and create all required catalogs if these not exists*/
    public Boolean exportConnectionListToFile(){
        try{
            String path = System.getenv("APPDATA").replace("\\", "\\\\");
            String filePath=path+"\\\\ORCLmonitor\\\\ORCLmonitor.json";
            path+="\\\\ORCLmonitor\\\\";
            System.out.println("Path: "+path);
            Gson gson = new Gson();
            String jsonString = gson.toJson(connectionList);
            System.out.println(connectionList);
            System.out.println("JSON: "+jsonString);
            File directory = new File(path);
            if(!directory.exists()){
                directory.mkdirs();
            }
            PrintWriter writer = new PrintWriter(filePath);
            writer.print("");
            writer.print(jsonString);
            writer.close();
            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**This method is responsible for export collection of connections objects to file to specific by user location, and create all required defaylt catalogs in default path if not exists*/
    public Boolean exportConnectionListToFileAs(String filePath){
        try{
            filePath=filePath.replace("\\", "\\\\");
            if(filePath.equals(System.getenv("APPDATA").replace("\\", "\\\\")+"\\\\ORCLmonitor\\\\ORCLmonitor.json")){
                String roamingPath = System.getenv("APPDATA").replace("\\", "\\\\")+"\\\\ORCLmonitor\\\\";
                Gson gson = new Gson();
                String jsonString = gson.toJson(connectionList);
                File directory = new File(roamingPath);
                if(!directory.exists()){
                    directory.mkdirs();
                }
                PrintWriter writer = new PrintWriter(filePath);
                writer.print("");
                writer.print(jsonString);
                writer.close();
            }else{
                Gson gson = new Gson();
                String jsonString = gson.toJson(connectionList);
                PrintWriter writer = new PrintWriter(filePath);
                writer.print("");
                writer.print(jsonString);
                writer.close();
            }
            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**This method is responsible for import connections from file from default location*/
    public Boolean importConnectionListFromDefaultLocation(){
        String pathToConnectionListJsonFile = System.getenv("APPDATA").replace("\\", "\\\\");
        pathToConnectionListJsonFile+="\\\\ORCLmonitor\\\\ORCLmonitor.json";
        connectionList.clear();
        try{
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(pathToConnectionListJsonFile));
            connectionList = new Gson().fromJson(reader, new TypeToken<List<Connection>>(){}.getType());
            //System.out.println("Startup connections import was successfully\nConnections list are with values: "+connectionList);
            return true;
        }catch (Exception e){
            //System.out.println("Error on startup import connections file\nFile is missing or have unexpected values");
            return false;
        }
    }

    /**This method is responsible for import connections from file from specific by user location*/
    public Boolean importConnectionListFromSpecificLocation(String pathToConnectionListJsonFile){
        pathToConnectionListJsonFile = pathToConnectionListJsonFile.replace("\\", "\\\\");
        connectionList.clear();
        try{
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(pathToConnectionListJsonFile));
            connectionList = new Gson().fromJson(reader, new TypeToken<List<Connection>>(){}.getType());
            //System.out.println("Startup connections import was successfully\nConnections list are with values: "+connectionList);
            return true;
        }catch (Exception e){
            //System.out.println("Error on startup import connections file\nFile is missing or have unexpected values");
            return false;
        }
    }
}
