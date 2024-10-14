package team4s;

public class Employee {
    private int employee_id;
    private String first_name;
    private String last_name;
    private String role;

    /**
     * Constructs a new Employee with the specified details.
     *
     * @param employee_id the unique ID of the employee
     * @param first_name  the first name of the employee
     * @param last_name   the last name of the employee
     * @param role        the role of the employee
     */
    public Employee(int employee_id, String first_name, String last_name, String role) {
        this.employee_id = employee_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.role = role;
    }

    /**
     * Gets the employee ID.
     *
     * @return employee_id the employee ID
     */
    public int getEmployeeID() {
        return employee_id;
    }

    /**
     * Gets the first name of the employee.
     *
     * @return first_name the first name of the employee
     */
    public String getFirstName() {
        return first_name;
    }

    /**
     * Gets the last name of the employee.
     *
     * @return last_name the last name of the employee
     */
    public String getLastName() {
        return last_name;
    }

    /**
     * Gets the role of the employee.
     *
     * @return role the role of the employee
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the employee ID.
     *
     * @param employee_id the new employee ID
     */
    public void setEmployeeID(int employee_id) {
        this.employee_id = employee_id;
    }

    /**
     * Sets the first name of the employee.
     *
     * @param first_name the new first name of the employee
     */
    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    /**
     * Sets the last name of the employee.
     *
     * @param last_name the new last name of the employee
     */
    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    /**
     * Sets the role of the employee.
     *
     * @param role the new role of the employee
     */
    public void setRole(String role) {
        this.role = role;
    }
}
