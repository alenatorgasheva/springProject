
package nsu.ui;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Rob Winch
 */
public class Group {

    private Long id;

    private final ConcurrentMap<Long, Student> students = new ConcurrentHashMap<Long, Student>();

    private String groupName;

    public Group(Long id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Iterable<Student> getStudents() {
        return students.values();
    }

    public void addStudent(Student student) {
        students.put(student.getId(), student);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}

