/**
 * Copyright (c) 2025 Sami Menik, PhD. All rights reserved.
 *
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
 * This software is provided "as is," without warranty of any kind.
 */
package uga.csx370.mydbimpl;

import java.util.List;

import uga.csx370.mydb.RA;
import uga.csx370.mydb.Relation;
import uga.csx370.mydb.RelationBuilder;
import uga.csx370.mydb.Type;

public class Driver {

    public static void main(String[] args) {
        System.out.println("Myid: 811059368");

        // Load all relations (uni_in_class database schema)
        Relation instructor = new RelationBuilder()
                .attributeNames(List.of("Inst_ID", "Inst_Name", "Inst_Department", "Salary"))
                .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE))
                .build();
        instructor.loadData("instructor_export.csv");

        Relation student = new RelationBuilder()
                .attributeNames(List.of("Stu_ID", "Stu_Name", "Stu_Department", "Grade"))
                .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER))
                .build();
        student.loadData("student_export.csv");

        Relation course = new RelationBuilder()
                .attributeNames(List.of("Course_ID", "Course_Name", "Department", "Credits"))
                .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER))
                .build();
        course.loadData("course_export.csv");

        Relation teaches = new RelationBuilder()
                .attributeNames(List.of("Inst_ID", "Course_ID"))
                .attributeTypes(List.of(Type.INTEGER, Type.INTEGER))
                .build();
        teaches.loadData("teaches_export.csv");

        Relation takes = new RelationBuilder()
                .attributeNames(List.of("Stu_ID", "Course_ID", "Score"))
                .attributeTypes(List.of(Type.INTEGER, Type.INTEGER, Type.INTEGER))
                .build();
        takes.loadData("takes_export.csv");

        RA ra = new RAImpl();

        // ========== QUERY 1 ==========
        // English: Find instructors who teach Database Systems, along with their name,
        // department, and salary.
        // RA: project(select(join(join(instructor, teaches), course), Course_Name="Database Systems"),
        //      [Inst_Name, Inst_Department, Salary, Course_Name])
        System.out.println("\n========== QUERY 1 ==========");
        System.out.println("Find instructors who teach Database Systems, along with their name, department, and salary.");
        Relation instTeaches = ra.join(instructor, teaches);
        Relation instTeachesCourse = ra.join(instTeaches, course);
        Relation dbInstructors = ra.select(instTeachesCourse, row ->
                row.get(instTeachesCourse.getAttrIndex("Course_Name")).getAsString().equals("Database Systems"));
        Relation q1 = ra.project(dbInstructors, List.of("Inst_Name", "Inst_Department", "Salary", "Course_Name"));
        q1.print();

        // ========== QUERY 2 ==========
        // English: Find Comp. Sci. students who took a course, with their name, course name, and score.
        // RA: project(select(join(join(student, takes), course), Stu_Department="Comp. Sci."),
        //      [Stu_Name, Course_Name, Score])
        System.out.println("\n========== QUERY 2 ==========");
        System.out.println("Find Comp. Sci. students who took a course, with their name, course name, and score.");
        Relation studentTakes = ra.join(student, takes);
        Relation studentTakesCourse = ra.join(studentTakes, course);
        Relation csStudents = ra.select(studentTakesCourse, row ->
                row.get(studentTakesCourse.getAttrIndex("Stu_Department")).getAsString().equals("Comp. Sci."));
        Relation q2 = ra.project(csStudents, List.of("Stu_Name", "Course_Name", "Score"));
        q2.print();

        // ========== QUERY 3 ==========
        // English: Find Physics department students and the instructors in Physics department
        // (students and instructors in the same department - Physics only to limit rows).
        // RA: project(theta_join(student, instructor, Stu_Department=Inst_Department AND Stu_Department="Physics"),
        //      [Stu_Name, Inst_Name, Stu_Department])
        System.out.println("\n========== QUERY 3 ==========");
        System.out.println("Find Physics department students paired with Physics department instructors.");
        Relation sameDept = ra.join(student, instructor, row -> {
            String stuDept = row.get(student.getAttrIndex("Stu_Department")).getAsString();
            String instDept = row.get(student.getAttrs().size() + instructor.getAttrIndex("Inst_Department")).getAsString();
            return stuDept.equals("Physics") && stuDept.equals(instDept);
        });
        Relation q3 = ra.project(sameDept, List.of("Stu_Name", "Inst_Name", "Stu_Department"));
        q3.print();

        // ========== QUERY 4 ==========
        // English: Find instructors who earn more than 80000 and teach at least one course,
        // with their name, salary, and course name.
        // RA: project(select(join(select(instructor, Salary>80000), join(teaches, course)), true),
        //      [Inst_Name, Salary, Course_Name])
        System.out.println("\n========== QUERY 4 ==========");
        System.out.println("Find high-earning instructors (Salary > 80000) who teach, with their name, salary, and course.");
        Relation highSal = ra.select(instructor, row ->
                row.get(instructor.getAttrIndex("Salary")).getAsDouble() > 80000);
        Relation teachesCourse = ra.join(teaches, course);
        Relation highSalTeaches = ra.join(highSal, teachesCourse);
        Relation q4 = ra.project(highSalTeaches, List.of("Inst_Name", "Salary", "Course_Name"));
        q4.print();

        // ========== QUERY 5 ==========
        // English: Find students who scored 90 or above in any course, with student name, course name, and score.
        // RA: project(join(join(select(takes, Score>=90), student), course),
        //      [Stu_Name, Course_Name, Score])
        System.out.println("\n========== QUERY 5 ==========");
        System.out.println("Find students who scored 90 or above in any course.");
        Relation highScores = ra.select(takes, row ->
                row.get(takes.getAttrIndex("Score")).getAsInt() >= 90);
        Relation highScoresStudent = ra.join(highScores, student);
        Relation highScoresFull = ra.join(highScoresStudent, course);
        Relation q5 = ra.project(highScoresFull, List.of("Stu_Name", "Course_Name", "Score"));
        q5.print();
    }
}
