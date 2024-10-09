package team4s;

public class Employee {
    private int employee_id;
    private String first_name;
    private String last_name;
    private String role;

    // constructor
    public Employee(int employee_id, String first_name, String last_name, String role) {
        this.employee_id = employee_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.role = role;
    }

    // getters
    public int getEmployeeID() {
        return employee_id;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getRole() {
        return role;
    }

    // setters
    public void setEmployeeID(int employee_id) {
        this.employee_id = employee_id;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
