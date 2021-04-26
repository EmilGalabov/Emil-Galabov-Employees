

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Engine implements Runnable {

    private static final String OUTPUT_MESSAGE = "The two employees that spent the most days working together are with IDs %s and %s.%n" +
            "They worked together for %d days.";


    private File fileReader;
    private StringBuilder errors;

    public Engine() {
        this.errors = new StringBuilder();
        this.fileReader = new FileImpl();
    }

    @Override
    public void run() {
        try {
            Map<Long, List<Employee>> employeesWithSameProject = employeesWithSameProject(this.fileReader.readFile("resources/employees"));
            System.out.println(errors.toString());
            System.out.println(findLongestPair(employeesWithSameProject));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }



    // The key of the HashMap is the ProjectId and the value is the List of Employees, who worked on the same project

    private Map<Long, List<Employee>> employeesWithSameProject(List<String> lines) {
        Map<Long, List<Employee>> projectsEmployees = new HashMap<>();
        Arrays
                .stream(lines.toArray())
                .forEach(line -> {
                    String[] data = line.toString().split("\\s+");
                    Long employeeId = Long.parseLong(data[0]);
                    Long projectId = Long.parseLong(data[1]);
                    Employee employee = new Employee(employeeId);

                    String from = data[2];
                    String to = data[3];


                    //Validate dates
                    LocalDate[] localDates = validateDates(from, to, employeeId);
                    if (localDates[0] == null || localDates[1] == null) {
                        errors.append(String.format("Employee with ID %d has incorrect start/end date!", employeeId));
                        errors.append(System.lineSeparator());
                        return;
                    }
                    LocalDate dateFrom = localDates[0];
                    LocalDate dateTo = localDates[1];
                    employee.setDateFrom(dateFrom);
                    employee.setDateTo(dateTo);

                    if (!projectsEmployees.containsKey(projectId)) {
                        List<Employee> employees = new ArrayList<>();
                        employees.add(employee);
                        projectsEmployees.put(projectId, employees);
                    } else {
                        projectsEmployees.get(projectId).add(employee);
                    }
                });
        return projectsEmployees;
    }


     // Validate dates. DateFrom cant be NULL

    private LocalDate[] validateDates(String from, String to, Long employeeId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate[] dates = new LocalDate[2];
        LocalDate dateFrom = null;
        LocalDate dateTo;
        boolean valid = true;

        if (to.equals("NULL")) {
            dateTo = LocalDate.parse(LocalDate.now().toString(), formatter);
        } else {
            dateTo = LocalDate.parse(to, formatter);
        }

        if (from.equals("NULL")) {
            valid = false;
        } else {
            dateFrom = LocalDate.parse(from, formatter);
            int compareDates = dateFrom.compareTo(dateTo);
            if (compareDates > 0) {
                valid = false;
            }
        }
        if (valid) {
            dates[0] = dateFrom;
            dates[1] = dateTo;
        }
        return dates;
    }

    private String findLongestPair(Map<Long, List<Employee>> employeeMap) {
        // String  -> combination of employees IDs for example 1-2, 4-3
        // Long    -> days together
        Map<String, Long> teamsDays = new HashMap<>();


        //Place employees in teams based on projects  and the time they worked on these projects
        for (Map.Entry<Long, List<Employee>> projectEmployees : employeeMap.entrySet()) {
            for (int i = 0; i < projectEmployees.getValue().size(); i++) {
                Employee employee1 = projectEmployees.getValue().get(i);

                for (int j = i + 1; j <= projectEmployees.getValue().size() - 1; j++) {
                    Employee employee2 = projectEmployees.getValue().get(j);
                    if (employee1.getId().equals(employee2.getId())) {
                        continue;
                    }
                    boolean haveWorkedTogether = checkIfWorkedTogether(employee1, employee2);//checks if employees worked together
                    if (!haveWorkedTogether) {
                        String team = employee1.getId() + "-" + employee2.getId();// place employees in a team
                        Long days = getCountOfDaysTogether(employee1, employee2);// gets the count of days they worked together on a project

                        if (teamsDays.containsKey(team)) {
                            teamsDays.put(team, teamsDays.get(team) + days); // adds extra days if they worked on more than one project
                        } else {
                            teamsDays.put(team, days);
                        }
                    }
                }
            }
        }
        reformatData(teamsDays);
        String longestLastingTeam = calculateTeamWithMostDays(teamsDays);
        if (longestLastingTeam.equals("")) {
            return "No pair of employees worked together on any project.";
        }
        String[] couple = longestLastingTeam.split("-");
        String employeeOneId = couple[0];
        String employeeTwoId = couple[1];
        Long countDays = teamsDays.get(longestLastingTeam);
        return String.format(OUTPUT_MESSAGE, employeeOneId, employeeTwoId, countDays);
    }


    // Calculates the team with most days working on projects
    private String calculateTeamWithMostDays(Map<String, Long> teamsDays) {
        AtomicReference<Long> countDaysMax = new AtomicReference<>(0L);
        AtomicReference<String> longestPair = new AtomicReference<>("");
        teamsDays.forEach((key, value) -> {
            if (countDaysMax.get() < value) {
                countDaysMax.set(value);
                longestPair.set(key);
            }
        });
        return longestPair.toString();
    }

    // Check if 2 employees have worked on a project at the same time
    private boolean checkIfWorkedTogether(Employee employee1, Employee employee2) {
        return employee1.getDateTo().isBefore(employee2.getDateFrom()) || employee2.getDateTo().isBefore(employee1.getDateFrom());
    }

    // Sum the days 2 employees worked together on a project
    private Long getCountOfDaysTogether(Employee employee1, Employee employee2) {
        LocalDate startDate;
        LocalDate endDate;

        int startDateComparison = employee1.getDateFrom().compareTo(employee2.getDateFrom());
        if (startDateComparison >= 0) {
            startDate = employee1.getDateFrom();
        } else {
            startDate = employee2.getDateFrom();
        }

        int endDateComparison = employee1.getDateTo().compareTo(employee2.getDateTo());
        if (endDateComparison < 0) {
            endDate = employee1.getDateTo();
        } else {
            endDate = employee2.getDateTo();
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }


    // Reformatting the data.Team 1-10 is the same as team 10-1



    private void reformatData(Map<String, Long> teamsDays) {
        for (Map.Entry<String, Long> entry : teamsDays.entrySet()) {
            if (teamsDays.containsKey(reverseKey(entry.getKey())) && entry.getKey().equals(reverseKey(entry.getKey()))) {
                Long daysToAdd = teamsDays.get(reverseKey(entry.getKey()));
                teamsDays.put(entry.getKey(), teamsDays.get(entry.getKey()) + daysToAdd);
            }
        }
    }

    // Reverses a key. The key 10-1 becomes 1-10.


    private String reverseKey(String key) {
        String[] keyArr = key.split("-");
        return keyArr[keyArr.length - 1] + "-" + keyArr[0];
    }


}

