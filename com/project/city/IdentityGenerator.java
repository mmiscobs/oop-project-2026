package com.project.city;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.project.utils.SerializedBlob;

public final class IdentityGenerator {
    private static final List<String> FIRST_NAMES_MALE = List.of(
            "James", "John", "Robert", "Michael", "William", "David", "Richard",
            "Joseph", "Thomas", "Charles", "Christopher", "Daniel", "Matthew",
            "Anthony", "Mark", "Donald", "Steven", "Paul", "Andrew", "Joshua",
            "Kenneth", "Kevin", "Brian", "George", "Edward", "Ronald", "Timothy",
            "Jason", "Jeffrey", "Ryan", "Jacob", "Gary", "Nicholas", "Eric",
            "Jonathan", "Stephen", "Larry", "Justin", "Scott", "Brandon", "Frank",
            "Benjamin", "Gregory", "Samuel", "Raymond", "Patrick", "Alexander",
            "Jack", "Dennis", "Jerry", "Tyler", "Aaron", "Henry", "Douglas");

    private static final List<String> FIRST_NAMES_FEMALE = List.of(
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara",
            "Susan", "Jessica", "Sarah", "Karen", "Lisa", "Nancy", "Betty",
            "Sandra", "Margaret", "Ashley", "Kimberly", "Emily", "Donna",
            "Michelle", "Carol", "Amanda", "Melissa", "Deborah", "Stephanie",
            "Rebecca", "Laura", "Sharon", "Cynthia", "Amy", "Kathleen", "Angela",
            "Shirley", "Brenda", "Emma", "Anna", "Pamela", "Nicole", "Samantha",
            "Katherine", "Christine", "Helen", "Debra", "Rachel", "Carolyn",
            "Janet", "Maria", "Catherine", "Heather", "Diane", "Olivia", "Julie",
            "Joyce", "Victoria", "Ruth", "Virginia", "Lauren", "Kelly");

    private static final List<String> LAST_NAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
            "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
            "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark",
            "Ramirez", "Lewis", "Robinson", "Walker", "Young", "Allen", "King",
            "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green",
            "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
            "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz",
            "Parker", "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris",
            "Morales", "Murphy", "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan");

    public static final int DEFAULT_MIN_YEAR = 1940;
    public static final int DEFAULT_MAX_YEAR = Year.now().getValue() - 18;

    private final Random random;
    private final int minYear;
    private final int maxYear;

    public IdentityGenerator() {
        this(new Random(), DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR);
    }

    public IdentityGenerator(long seed) {
        this(new Random(seed), DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR);
    }

    public IdentityGenerator(Random random, int minYear, int maxYear) {
        if (minYear > maxYear) {
            throw new IllegalArgumentException(
                    "minYear (" + minYear + ") must be <= maxYear (" + maxYear + ")");
        }
        this.random = java.util.Objects.requireNonNull(random, "random");
        this.minYear = minYear;
        this.maxYear = maxYear;
    }

    public record Identity(String firstName, String lastName, int birthYear) {
        public String fullName() {
            return firstName + " " + lastName;
        }

        @Override
        public String toString() {
            return fullName() + " (b. " + birthYear + ")";
        }

        Identity(SerializedBlob blob) {
            this(blob.map().get("firstName").string(), blob.map().get("lastName").string(),
                    blob.map().get("birthYear").intValue());
        }

        public SerializedBlob toBlob() {
            return SerializedBlob.fromMap(Map.of(
                    "firstName", SerializedBlob.string(firstName),
                    "lastName", SerializedBlob.string(lastName),
                    "birthYear", SerializedBlob.intValue(birthYear)));
        }
    }

    public Identity nextIdentity() {
        List<String> firsts = random.nextBoolean() ? FIRST_NAMES_MALE : FIRST_NAMES_FEMALE;
        String first = firsts.get(random.nextInt(firsts.size()));
        String last = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
        int year = minYear + random.nextInt(maxYear - minYear + 1);
        return new Identity(first, last, year);
    }

    public static IdentityGenerator generator = new IdentityGenerator();
}