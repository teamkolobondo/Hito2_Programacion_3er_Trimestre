package com.empresa.hito_programacion_daniel_jimenez;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBHandler {
    private static final String CONNECTION_STRING = "mongodb+srv://administrador:Abc123456@teamkolobondo.zj7al6y.mongodb.net/";
    private static final String DATABASE_NAME = "hito";
    private static final String COLLECTION_NAME = "clientes";

    public List<Document> getRegistros() {
        List<Document> registros = new ArrayList<>();
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            for (Document doc : collection.find()) {
                registros.add(doc);
            }
            System.out.println("Registros obtenidos: " + registros.size());
        } catch (Exception e) {
            System.err.println("Error al obtener registros: " + e.getMessage());
            e.printStackTrace();
        }
        return registros;
    }

    public void addRegistro(HelloController.Registro registro) {
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            Document doc = new Document("nombre", registro.getNombre())
                    .append("correo", registro.getCorreo())
                    .append("factura", registro.getFactura())
                    .append("pagado", registro.isPagado());
            collection.insertOne(doc);
            System.out.println("Registro añadido: " + doc);
        } catch (Exception e) {
            System.err.println("Error al añadir registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteRegistro(HelloController.Registro registro) {
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            collection.deleteOne(new Document("nombre", registro.getNombre()));
            System.out.println("Registro eliminado: " + registro.getNombre());
        } catch (Exception e) {
            System.err.println("Error al eliminar registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateRegistro(HelloController.Registro oldRegistro, HelloController.Registro newRegistro) {
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            Document filter = new Document("nombre", oldRegistro.getNombre());
            Document update = new Document("$set", new Document("nombre", newRegistro.getNombre())
                    .append("correo", newRegistro.getCorreo())
                    .append("factura", newRegistro.getFactura())
                    .append("pagado", newRegistro.isPagado()));
            collection.updateOne(filter, update);
            System.out.println("Registro actualizado: " + newRegistro.getNombre());
        } catch (Exception e) {
            System.err.println("Error al actualizar registro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}