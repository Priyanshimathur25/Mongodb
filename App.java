
package com.college;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        String uri = System.getenv("MONGODB_URI");

        if (uri == null || uri.isBlank()) {
            System.out.println("Please set MONGODB_URI first.");
            return;
        }

        try (
            MongoClient client = MongoClients.create(uri);
            Scanner sc = new Scanner(System.in)
        ) {
            MongoDatabase database = client.getDatabase("college");

            MongoCollection<Document> students =
                    database.getCollection("students");

            database.runCommand(new Document("ping", 1));
            System.out.println("MongoDB connected successfully!");

            while (true) {
                System.out.println("\n--- Student Search Menu ---");
                System.out.println("1. Search by ID");
                System.out.println("2. Search by Department");
                System.out.println("3. Search by CGPA");
                System.out.println("4. Search by Skill");
                System.out.println("5. View All Students");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                if (!sc.hasNextInt()) {
                    System.out.println("Invalid choice!");
                    sc.nextLine();
                    continue;
                }

                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter student ID: ");
                        String id = sc.nextLine();

                        Document byId = students.find(
                                Filters.eq("studentId", id)).first();

                        if (byId != null) {
                            System.out.println(byId.toJson());
                        } else {
                            System.out.println("Student not found.");
                        }
                        break;

                    case 2:
                        System.out.print("Enter department: ");
                        String departmentName = sc.nextLine();

                        showStudents(students,
                                Filters.eq("department", departmentName));
                        break;

                    case 3:
                        System.out.print("Enter minimum CGPA: ");

                        if (!sc.hasNextDouble()) {
                            System.out.println("Invalid CGPA!");
                            sc.nextLine();
                            break;
                        }

                        double cgpa = sc.nextDouble();
                        sc.nextLine();

                        showStudents(students, Filters.gte("cgpa", cgpa));
                        break;

                    case 4:
                        System.out.print("Enter skill: ");
                        String skill = sc.nextLine();

                        showStudents(students, Filters.eq("skills", skill));
                        break;

                    case 5:
                        showStudents(students, new Document());
                        break;

                    case 6:
                        System.out.println("Exiting program...");
                        return;

                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showStudents(
            MongoCollection<Document> students,
            org.bson.conversions.Bson filter) {

        boolean found = false;

        for (Document student : students.find(filter)) {
            System.out.println(student.toJson());
            found = true;
        }

        if (!found) {
            System.out.println("No matching students found.");
        }
    }
}
