/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nsu.ui;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dave Syer
 */
public class InMemoryStudentRespository implements StudentRepository {

	private static AtomicLong counterStudent = new AtomicLong();

	private static AtomicLong counterGroups = new AtomicLong();

	private final ConcurrentMap<Long, Student> students = new ConcurrentHashMap<Long, Student>();

	private final ConcurrentMap<Long, Group> groups = new ConcurrentHashMap<Long, Group>();

	@Override
	public Iterable<Student> findAllStudents() {
		if (this.students.isEmpty()) {
			getStudentFromDB();
		}
		return this.students.values();
	}

	@Override
	public Iterable<Group> findAllGroups() {
		if (this.groups.isEmpty()) {
			getStudentFromDB();
		}
		return this.groups.values();
	}

	@Override
	public Student save(Student student, Long groupId) {
		Long studentId = student.getId();
		if (studentId == null) {
			studentId = counterStudent.incrementAndGet();
			student.setId(studentId);
		} else {
			if (counterStudent.longValue() < studentId) {
				counterStudent.set(studentId);
			}
		}
		this.students.put(studentId, student);

		if (groups.containsKey(groupId)) {
			groups.get(groupId).addStudent(student);
			return student;
		}
		Group group = new Group(groupId, student.getGroupName());
		group.addStudent(student);
		this.groups.put(groupId, group);
		return student;
	}

	@Override
	public Student findStudent(Long id) {
		return this.students.get(id);
	}

	public void getStudentFromDB() {
		String url = "jdbc:mysql://localhost:3306/mydb2";
		String user = "root";
		String password = "password";

		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			Statement statement = connection.createStatement();

			String query = "SELECT * FROM student;";
			ResultSet studentsDB = statement.executeQuery(query);
			while (studentsDB.next()) {
				Student student = new Student();
				student.setId(studentsDB.getLong("id"));
				student.setLastName(studentsDB.getString("last_name"));
				student.setFirstName(studentsDB.getString("first_name"));
				student.setSecondName(studentsDB.getString("second_name"));
				student.setBirthdayDate(studentsDB.getString("birthday_date"));

				String groupName = "";
				Long groupId = studentsDB.getLong("group_id");

				Statement statement2 = connection.createStatement();
				query = "SELECT * FROM `group` WHERE (`id` = '" + groupId + "');";
				ResultSet groups = statement2.executeQuery(query);
				while (groups.next()) {
					groupName = groups.getString("group_name");
				}

				groups.close();
				statement2.close();

				student.setGroupName(groupName);
				save(student, groupId);
			}

			studentsDB.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateDB(Student student) {
		String url = "jdbc:mysql://localhost:3306/mydb2";
		String user = "root";
		String password = "password";

		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			Statement statement = connection.createStatement();

			// поиск id текущей группы
			Long groupId = null;
			String query = "SELECT * FROM `group` WHERE (`group_name` = '" + student.getGroupName() + "');";
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				groupId = resultSet.getLong("id");
			}
			// если группы нет в бд
			if (groupId == null) {
				query = "INSERT INTO `group` (`group_name`) " + "VALUES ('" + student.getGroupName() + "');";
				statement.executeUpdate(query);
				// находим id новой группы
				query = "SELECT * FROM `group` WHERE (`group_name` = '" + student.getGroupName() + "');";
				resultSet = statement.executeQuery(query);
				while (resultSet.next()) {
					groupId = resultSet.getLong("id");
				}
			}

			// проверяем, есть ли студент в бд
			boolean isStudentExist = false;
			query = "SELECT * FROM student WHERE (`last_name` = '" + student.getLastName() + "') AND (`first_name` = '" + student.getFirstName() + "') AND (`second_name` = '" + student.getSecondName() + "') AND (`birthday_date` = '" + student.getBirthdayDate() + "');";
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				isStudentExist = true;
			}
			if (!isStudentExist) {
				// добавляем студента в бд
				query = "INSERT INTO student (`group_id`, `last_name`, `first_name`, `second_name`, `birthday_date`) VALUES ('" + groupId + "', '" + student.getLastName() + "', '" + student.getFirstName() + "', '" + student.getSecondName() + "', '" + student.getBirthdayDate() + "');";
				statement.executeUpdate(query);
			}

			save(student, groupId);

			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
