package mukhammed.cascademaster;

import jakarta.persistence.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static jakarta.persistence.CascadeType. *;

@RepositoryRestResource(path = "groups")
interface GroupRepo extends JpaRepository<Group, Long> {
}

@RepositoryRestResource(path = "students")
interface StudentRepo extends JpaRepository<Student, Long> {
}

@RepositoryRestResource(path = "laptops")
interface LaptopRepo extends JpaRepository<Laptop, Long> {
}

@SpringBootApplication
public class CascadeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CascadeMasterApplication.class, args);
    }

}

// cascade types
// PERSIST, MERGE, REFRESH, DETACH, REMOVE

/*
PERSIST
errors*
org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : mukhammed.cascademaster.Student.laptop -> mukhammed.cascademaster.Laptop
 */

@RestController
class StudentHttpController {

    private final StudentRepo studentRepo;
    private final LaptopRepo laptopRepo;

    StudentHttpController(StudentRepo studentRepo,
                          LaptopRepo laptopRepo) {
        this.studentRepo = studentRepo;
        this.laptopRepo = laptopRepo;
    }
}

@MappedSuperclass
class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

@Entity
class Laptop extends BaseEntity {

    @Column(name = "model")
    private String model;
    private double price;

    @OneToOne(mappedBy = "laptop")
    private Student laptopOwner;

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Student getLaptopOwner() {
        return laptopOwner;
    }

    public void setLaptopOwner(Student laptopOwner) {
        this.laptopOwner = laptopOwner;
    }
}

@Entity
class Student extends BaseEntity {
    private String fullName;

    @ManyToMany(cascade = {DETACH, REFRESH, PERSIST}, mappedBy = "studentList")
    private List<Group> group;

    @OneToOne
    private Laptop laptop;

    @PrePersist
    @PreUpdate
    private void updateLaptop() {
        if (this.laptop != null) {
            laptop.setLaptopOwner(this);
        }
    }

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Group> getGroup() {
        return group;
    }

    public void setGroup(List<Group> group) {
        this.group = group;
    }

    public Laptop getLaptop() {
        return laptop;
    }

    public void setLaptop(Laptop laptop) {
        this.laptop = laptop;
    }
}

@Entity
@Table(name = "groups")
class Group extends BaseEntity {

    private String groupName;
    /*
    We will use BiDirectional Relationship
    Because we don't want to create new table between Group and Student
    How to?
    MappedBy should be in Main Class
     */
    @ManyToMany(cascade = {DETACH, REFRESH, PERSIST})
    @JoinTable(name = "groups_students",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> studentList;

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }
}


