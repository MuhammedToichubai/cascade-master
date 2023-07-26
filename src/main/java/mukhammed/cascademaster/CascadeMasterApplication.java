package mukhammed.cascademaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    // persists
    @PostMapping("/api/students")
    ResponseEntity<Student> create(@RequestBody StudentRequest studentRequest) {
        return ResponseEntity.ok(studentRepo.save(studentRequest.build()));
    }

    // trash
    @PutMapping("/api/students/{studentId}")
    ResponseEntity<?> updateLaptop(@PathVariable Long studentId,
                                   @RequestBody StudentRequest studentRequest) {
        studentRepo.findById(studentId)
                .ifPresent(student -> {
                    student.setFullName(studentRequest.fullName());
                    student.getLaptop().setModel(studentRequest.laptop().model());
                    student.getLaptop().setPrice(studentRequest.laptop().price());
                    studentRepo.save(student);
                });

        return ResponseEntity.status(200).build();
    }


    // koroche merge ozunor okup alynyzdar
    @PutMapping("/api/students/{studentId}/setLaptop")
    @Transactional
    ResponseEntity<Student> setLaptopToTheStudent(@PathVariable Long studentId,
                                                  @RequestBody Laptop laptop) {
        Student student = studentRepo.findById(studentId)
                .orElse(null);

        if (student != null) {
            student.setLaptop(laptop);
        }

        return ResponseEntity.ok(student);
    }


}

record StudentRequest(
        String fullName,
        LaptopRequest laptop
) {
    public Student build() {
        Student newStudent = new Student();
        newStudent.setFullName(this.fullName);
        newStudent.setLaptop(laptop.build());
        return newStudent;
    }
}

record LaptopRequest(
        String model,
        double price
) {
    public Laptop build() {
        Laptop newLaptop = new Laptop();
        newLaptop.setModel(this.model);
        newLaptop.setPrice(this.price);
        return newLaptop;
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
@Getter
@Setter
class Laptop extends BaseEntity {

    @Column(name = "model")
    private String model;
    private double price;

    @OneToOne(mappedBy = "laptop")
    @JsonIgnore
    private Student laptopOwner;
}

@Entity
class Student extends BaseEntity {
    private String fullName;

    @ManyToMany
    private List<Group> group;

    @OneToOne(cascade = CascadeType.PERSIST)//, CascadeType.MERGE})
    private Laptop laptop;

    @PrePersist
    @PreUpdate
    private void updateLaptop() {
        laptop.setLaptopOwner(this);
    }
}

@Entity
@Table(name = "groups")
@Getter
@Setter
class Group extends BaseEntity {

    private String groupName;
    /*
    We will use BiDirectional Relationship
    Because we don't want to create new table between Group and Student
    How to?
    MappedBy should be in Main Class
     */
    @ManyToMany
    private List<Student> studentList;
}
