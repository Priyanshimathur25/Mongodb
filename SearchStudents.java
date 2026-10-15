package com.college;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.Scanner;

public class SearchStudents {

    public static void main(String[] args) {
        String uri = System.getenv("MONGODB_URI");

        if (uri == null || uri.isBlank()) {
            System.out.println("Please set MONGODB_URI first.");
            return;
        }

        try (MongoClient client = MongoClients.create(uri);
             Scanner sc = new Scanner(System.in)) {

            MongoDatabase db = client.getDatabase("college");
            MongoCollection<Document> students =
                    db.getCollection("students");

            db.runCommand(new Document("ping", 1));
            System.out.println("MongoDB connected successfully!");

            while (true) {
                System.out.println("\n--- Java Query Operations ---");
                System.out.println("1. Search by ID");
                System.out.println("2. Search by Department");
                System.out.println("3. Search by Minimum CGPA");
                System.out.println("4. Search by Skill");
                System.out.println("5. View All Students");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                String input = sc.nextLine().trim();
                int choice;

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Enter a valid number!");
                    continue;
                }

                switch (choice) {
                    case 1:
                        System.out.print("Enter student ID: ");
                        String id = sc.nextLine().trim();

                        show(students,
                                Filters.eq("studentId", id));
                        break;

                    case 2:
                        System.out.print("Enter department: ");
                        String dept = sc.nextLine().trim();

                        show(students,
                                Filters.eq("department", dept));
                        break;

                    case 3:
                        System.out.print("Enter minimum CGPA: ");
                        try {
                            double cgpa = Double.parseDouble(
                                    sc.nextLine().trim());

                            if (cgpa < 0 || cgpa > 10) {
                                System.out.println(
                                        "CGPA must be between 0 and 10.");
                                break;
                            }

                            show(students,
                                    Filters.gte("cgpa", cgpa));

                        } catch (NumberFormatException e) {
                            System.out.println("Invalid CGPA!");
                        }
                        break;

                    case 4:
                        System.out.print("Enter skill: ");
                        String skill = sc.nextLine().trim();

                        show(students,
                                Filters.eq("skills", skill));
                        break;

                    case 5:
                        show(students, new Document());
                        break;

                    case 6:
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void show(
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
