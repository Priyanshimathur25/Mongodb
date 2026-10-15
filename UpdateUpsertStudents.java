
package com.college;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import java.util.Scanner;

public class UpdateUpsertStudents {

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
                System.out.println("\n--- Update + Upsert Menu ---");
                System.out.println("1. Update CGPA");
                System.out.println("2. Update Email");
                System.out.println("3. Add Skill");
                System.out.println("4. Update Phone");
                System.out.println("5. Upsert Student");
                System.out.println("6. View All Students");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");

                String input = sc.nextLine().trim();
                int choice;

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Enter a valid number!");
                    continue;
                }

                if (choice >= 1 && choice <= 4) {
                    System.out.print("Enter student ID: ");
                    String id = sc.nextLine().trim();

                    if (id.isEmpty()) {
                        System.out.println("Student ID is required.");
                        continue;
                    }

                    switch (choice) {
                        case 1:
                            System.out.print("Enter new CGPA: ");
                            try {
                                double cgpa = Double.parseDouble(
                                        sc.nextLine().trim());

                                if (cgpa < 0 || cgpa > 10) {
                                    System.out.println(
                                            "CGPA must be between 0 and 10.");
                                    break;
                                }

                                update(students, id,
                                        Updates.set("cgpa", cgpa));
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid CGPA!");
                            }
                            break;

                        case 2:
                            System.out.print("Enter new email: ");
                            String email = sc.nextLine().trim();

                            if (!email.isEmpty()) {
                                update(students, id,
                                        Updates.set("email", email));
                            } else {
                                System.out.println("Email is required.");
                            }
                            break;

                        case 3:
                            System.out.print("Enter skill to add: ");
                            String skill = sc.nextLine().trim();

                            if (!skill.isEmpty()) {
                                update(students, id,
                                        Updates.addToSet("skills", skill));
                            } else {
                                System.out.println("Skill is required.");
                            }
                            break;

                        case 4:
                            System.out.print("Enter new phone: ");
                            String phone = sc.nextLine().trim();

                            if (!phone.isEmpty()) {
                                update(students, id,
                                        Updates.set("phone", phone));
                            } else {
                                System.out.println("Phone is required.");
                            }
                            break;
                    }

                } else if (choice == 5) {
                    System.out.print("Enter student ID: ");
                    String id = sc.nextLine().trim();

                    if (id.isEmpty()) {
                        System.out.println("Student ID is required.");
                        continue;
                    }

                    System.out.print("Enter name: ");
                    String name = sc.nextLine().trim();

                    System.out.print("Enter department: ");
                    String department = sc.nextLine().trim();

                    System.out.print("Enter CGPA: ");
                    double cgpa;

                    try {
                        cgpa = Double.parseDouble(
                                sc.nextLine().trim());

                        if (cgpa < 0 || cgpa > 10) {
                            System.out.println("Invalid CGPA!");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid CGPA!");
                        continue;
                    }

                    System.out.print("Enter email: ");
                    String email = sc.nextLine().trim();

                    System.out.print("Enter phone: ");
                    String phone = sc.nextLine().trim();

                    Document fields = new Document("name", name)
                            .append("department", department)
                            .append("cgpa", cgpa)
                            .append("email", email)
                            .append("phone", phone);

                    var result = students.updateOne(
                            Filters.eq("studentId", id),
                            new Document("$set", fields),
                            new UpdateOptions().upsert(true)
                    );

                    if (result.getUpsertedId() != null) {
                        System.out.println("New student inserted!");
                    } else if (result.getMatchedCount() > 0) {
                        System.out.println("Existing student updated!");
                    } else {
                        System.out.println("No changes required.");
                    }

                } else if (choice == 6) {
                    boolean found = false;

                    for (Document student : students.find()) {
                        System.out.println(student.toJson());
                        found = true;
                    }

                    if (!found) {
                        System.out.println("No students found.");
                    }

                } else if (choice == 7) {
                    System.out.println("Exiting program...");
                    return;

                } else {
                    System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void update(
            MongoCollection<Document> students,
            String id,
            org.bson.conversions.Bson operation) {

        var result = students.updateOne(
                Filters.eq("studentId", id), operation);

        if (result.getMatchedCount() == 0) {
            System.out.println("Student not found.");
        } else if (result.getModifiedCount() == 0) {
            System.out.println("Student found; no changes needed.");
        } else {
            System.out.println("Student updated successfully!");
        }
    }
}
